package server;

import model.*;
import utils.crypto.asymmetric.AsymmetricEncryptionUtils;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ServerConnection extends Thread {
   public final Socket socket;
   public final Socket fileSocket;

   private final Server server;

    private BufferedReader reader;
    public PrintWriter writer;

    private ServerConnection serverConnection;

    public ArrayList<String> userGroups = new ArrayList<>();
    public static HashMap<String, PublicKey> publicKeys = new HashMap<>();
    public HashMap<String, Boolean> wereMessagesReceivedDuringLastTwoMinutes = new HashMap<>(); //store a group name as key and wasMessagedReceivedWithinTwoMinutesFromUser as value
    public String username;
    private ServerConnection privateServerConnection = null;
    public String currentGroupNameOfUser = "";
    public boolean shouldRun = true;
    public boolean receivedPong = false;
    public  boolean isClientLoggedIn = false;
    public  boolean isClientSuccessfullyLoggedIn = false;
    public boolean isClientLoggedOut = false;
    public boolean isUsernameCorrect;
    public boolean isClientJustLoggedOut = false;
    public boolean didUserJoinTheGroup = false;
    public  boolean wasNameOfGroupWrittenCorrectly = false;
    public boolean isUserAuthenticated = false;

    public ServerConnection(Socket socket, Socket fileSocket, Server server) {
        super("ServerConnectionThread");
        this.socket = socket;
        this.fileSocket = fileSocket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
            serverConnection=this;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (shouldRun) {
                String msg = reader.readLine();

                isClientJustLoggedOut = false;
                String header = findHeader(msg);
                String body = findBody(msg);

                if (msg.equals("HELP") && !isClientLoggedIn) {  //new
                    sendMsgToClient("OK HELP1");
                } else if(msg.equals("HELP") && isClientLoggedIn) {
                    sendMsgToClient("OK HELP2");
                } else if (!header.equals("PONG")&&!header.equals("QUIT")&&!header.equals("?")&&!header.equals("CONN") && !header.equals("LOGIN") && !isClientSuccessfullyLoggedIn) {
                    writer.println("ER04");
                } else if (header.equals("LOGIN") && isClientSuccessfullyLoggedIn) {
                    writer.println("ER06");
                } else if (header.equals("CONN") && !isClientSuccessfullyLoggedIn && !isClientLoggedOut) {
                    processRegistering(body);
                } else {
                    switch (header) {
                        case "AUTH" -> AuthenticationManager.processAuthentication(server,body,this,writer);
                        case "LIST" -> ListingUsersManager.processListingUsers(server,this);
                        case "CREATE" -> CreatingGroupManager.processCreatingGroup(body,server,writer);
                        case "JOIN" -> JoiningGroupManager.processJoiningGroup(body,server,this,writer);
                        case "LISTGr" -> ListingGroupsManager.processListingGroups(server,writer);
                        case "GROUP" -> SendingGroupMessageManager.processSendingGroupMessage(body,server,this,writer);
                        case "PUBKEY" -> processAddPubkey(body);
                        case "SSKEY" -> processSendSessionKey(body);
                        case "PRIVATE" -> SendingPrivateMessageManager.processSendingPrivateMessage(body,server,this,writer);
                        case "LOGOUT" -> ProcessLogoutManager.processLogout(serverConnection,server,writer);
                        case "BCST" -> ProcessBroadcastingMessageManager.processBroadcasting(body,writer,this,server);
                        case "LEAVE" -> LeavingGroupManager.processLeavingFromTheGroup(body,server,serverConnection,writer);
                        case "QUIT" -> ProcessQuitManager.processQuiting(server,serverConnection,writer);
                        case "FILE" -> SendingPrivateMessageManager.processFileTransfer(body,server,serverConnection,writer);
                        case "FILEREQ" -> {
                            System.out.println(body);
                            writer.println("OK FILE_REQ");
                        }
                        case "FILEACK", "FILEACK_OK", "FILEACK_REJECT" -> SendingPrivateMessageManager.processFile(msg,server,serverConnection,writer,header);
                        case "PONG" -> receivedPong = true;
                        default -> writer.println("ER00");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client has been disconnected");
        }
    }

    private void processSendSessionKey(String body) {
        String[] array = body.split(" ");
        String username = array[0];
        publicKeys.forEach((key, value) -> {
            if (username.equals(key)) {
                for (int i = 0; i < server.connections.size(); i++) {
                    if (server.connections.get(i).username.equals(username)) {
                        privateServerConnection = server.connections.get(i);
                        String message = getMessageOfUser(array);
                        String encodedMessage = null;
                        try {
                            encodedMessage = Base64.getEncoder().encodeToString(AsymmetricEncryptionUtils.performRSAEncryption(message, value));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendSessionKeyToClient(encodedMessage, privateServerConnection);
                        writer.println("OK SSKEY " + username + " " + message);
                    }
                }
            }
        });
    }

    public void sendMsgToClient(String text) {
        writer.println(text);
    }

    public void sendSessionKeyToClient(String text, ServerConnection serverConnection) {
        serverConnection.writer.println("SSKEY " + username + " " + text);

    }

    private void heartbeat() {
        System.out.printf("~~ %s Heartbeat initiated\n", username);

        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            writer.println("PING");
            receivedPong = false;

            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
                if (receivedPong) {
                    System.out.printf("~~ %s Heartbeat expired - SUCCESS\n", username);
                    heartbeat();
                } else {
                    System.out.printf("~~ %s Heartbeat expired - FAILED\n", username);
                }
            });
        });
    }

    public void processAddPubkey(String body) throws NoSuchAlgorithmException, InvalidKeySpecException {
        publicKeys.put(this.username, AsymmetricEncryptionUtils.getOriginalPublicKey(body));
        writer.println("OK PUBKEY");
    }

    public void processRegistering(String userName){
        if (!isClientLoggedIn) {
            isClientLoggedIn = true;
            for (int i = 0; i < server.usernames.size(); i++) {
                if (server.usernames.get(i).equals(userName)) {
                    isClientLoggedIn = false;
                    writer.println("ER01");
                }
            }
            isUsernameCorrect = checkUsername(userName);
            if (!isUsernameCorrect) {
                isClientLoggedIn = false;
                writer.println("ER02");
            }
            if (isClientLoggedIn) {
                var fileServerConnection = new FileServerConnection(fileSocket, this.server);
                this.server.fileConnections.add(fileServerConnection);
                fileServerConnection.start();
                username = userName;
                fileServerConnection.setUsername(userName);
                server.usernames.add(username);
                server.currentlyConnectedUsers.add(username);
                isClientSuccessfullyLoggedIn = true;
                writer.println("OK USERNAME" + " " + userName);
                writer.println("OK HELP2");
                new Thread(this::heartbeat).start();
            }
        }
    }

    public String findHeader(String msg){
        char[] chars = msg.toCharArray();
        StringBuilder header= new StringBuilder();
        for (char aChar : chars) {
            if (aChar == ' ') {
                break;
            }
            header.append(aChar);
        }
        return header.toString();
    }

    public String findBody(String msg){
        char[] chars = msg.toCharArray();
        StringBuilder body= new StringBuilder();
        boolean isBody = false;
        for (char aChar : chars) {
            if (isBody) {
                body.append(aChar);
            }
            if (aChar == ' ') {
                isBody = true;
            }
        }
        return body.toString();
    }

    public boolean checkUsername(String username) {
        boolean isUsernameCorrect = true;
        char[] chars = username.toCharArray();
        StringBuilder character = new StringBuilder();
        for (char aChar : chars) {
            character.append(aChar);
            if (Pattern.matches("[a-zA-Z]+", character.toString())) {

            } else if (character.toString().matches("[0-9]+")) {

            } else if (character.toString().equals("_")) {

            } else {
                isUsernameCorrect = false;
            }
            character = new StringBuilder();
        }
        if(username.toCharArray().length>=15){
            isUsernameCorrect=false;
        } else if(username.toCharArray().length<=2){
            isUsernameCorrect=false;
        }
        return isUsernameCorrect;
    }

    public String getMessageOfUser(String[] array) {
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < array.length; i++) {
            message.append(array[i]);
            if (i + 1 != array.length) {
                message.append(" ");
            }
        }
        return message.toString();
    }
}