package client;
import utils.FileRequest;
import utils.crypto.asymmetric.AsymmetricEncryptionUtils;
import utils.crypto.symmetric.SymmetricEncryptionUtils;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientConnection extends Thread {
    private final Socket socket;
    private final Client client;


    private final BufferedReader reader;
    private final PrintWriter writer;

    private boolean shouldRun = true;
    private boolean isSocketClosed = false;

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private HashMap<String, SecretKey> sessionKeys = new HashMap<>();





    public ClientConnection(Socket socket, Client client) throws Exception {
        this.socket = socket;
        this.client = client;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        KeyPair keyPair = AsymmetricEncryptionUtils.generateRSAKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    @Override
    public void run() {
        while (shouldRun) {
            try {
                String reply = reader.readLine();
                String[] arrayFromReply=sortResponseFromServer(reply);
                String header=arrayFromReply[0];
                String body=arrayFromReply[1];
                processResponse(reply,header,body);
            } catch (Exception e) {
                System.out.println("The server has been closed unexpectedly"); //new
                shouldRun=false;
            }
        }
    }

    public void processResponse(String reply,String header,String body) throws Exception {
        switch (header) {
            case "OK" -> {
                String[] array = body.split(" ");
                switch (array[0]) {
                    case "USERNAME":
                        System.out.println("WELCOME TO THE SYSTEM!");
                        break;

                    case "BCST":
                        System.out.println("Your message has been successfully broadcasted");
                        break;

                    case "PRIVATE":
                        System.out.println("Your message has been successfully sent privately");
                        break;

                    case "LIST":
                        String message2 = getConnectedUsers(array, 1);
                        System.out.println("There are the following currently connected users to the system: " + message2);
                        break;

                    case "LOGOUT":
                        System.out.println("You have successfully logged out from the system");
                        break;

                    case "LOGIN":
                        System.out.println("Welcome back:)");
                        break;

                    case "CREATE":
                        System.out.println("The group with the given name was successfully created");
                        break;

                    case "PUBKEY":
                        System.out.println("Your public key has been saved to the server!");
                        break;

                    case "FILE":
                        System.out.println("Your file transfer request has been sent to the receiver");
                        break;

                    case "SSKEY":
                        String username = array[1];
                        String encodedSessionKey = array[2];
                        byte[] decodedKey = Base64.getDecoder().decode(encodedSessionKey);
                        sessionKeys.put(username, new SecretKeySpec(decodedKey, 0, decodedKey.length, SymmetricEncryptionUtils.AES));
                        System.out.println("Secure channel established to " + username);
                        break;

                    case "JOIN":
                        System.out.println("You have successfully joined the desired group");
                        break;

                    case "LISTGr":
                        String message3 = getMessageOfUser(array, 1);
                        System.out.println("There are the following groups that exist in the system: " + message3);
                        break;

                    case "GROUP":
                        String message4 = getMessageOfUser(array, 1);
                        System.out.println("Your message <" + message4 + "> has been successfully sent to the group");
                        break;

                    case "LEAVE":
                        String groupName = getMessageOfUser(array, 1);
                        System.out.println("You have successfully left the group called " + groupName);
                        break;

                    case "QUIT":
                        System.out.println("Goodbye");
                        isSocketClosed = true;
                        break;

                    case "HELP1":
                        System.out.println("\n"+"Commands: Client " + "\n" +
                                """
                                        1 - use this number to connect to the chat application with specific username 
                                        ? - Help
                                        4 - use this number to quit
                                        """
                        );
                        break;

                    case "HELP2":
                        System.out.println("\n"+"Help menu: " + '\n' +
                                "0 - show all the existing commands" + '\n' +
                                "2 - use this number to log out from your account" + '\n' +
                                "3 - use this number to broadcast your message to all clients" + '\n' +
                                "4 - use this number to disconnect from the server" + '\n' +
                                "5 - list all the connected (logged-in) users" + '\n' +
                                "6 - list all existing groups in the system" + '\n' +
                                "7 - send the direct message to the given user" + '\n' +
                                "8 - create a group with the given name" + '\n' +
                                "9 - join a created group with the given name" + '\n' +
                                "10 - send the message to all participants of the given group" + '\n' +
                                "11 - leave the group" + '\n' +
                                "12 - use this command to authenticate yourself" + '\n' +
                                "13 - use this command to establish secure messaging channel with a remote client" + '\n' +
                                "14 - use this command to send file to remote client" + '\n' +
                                "15 - use this command to accept a file request from remote client" + '\n' +
                                "16 - use this command to reject a file request from remote client" + '\n' +
                                "17 - show all your file requests"
                        );
                        break;

                    case "AUTH":
                        System.out.println("Congratulations! You have successfully authenticated yourself");
                        break;

                    case "FORCEDLEAVE":
                        String groupName1 = getMessageOfUser(array, 1);
                        System.out.println("You have not sent any messages to the group called " + groupName1 + " for 2 minutes. That is why you have been forced to leave it");
                        break;

                    case "FILE_REQ":
                        for (var requests: client.getFileRequests()) {
                            System.out.println(requests);
                        };
                }
            }
            case "PRIVATE" -> {
                String[] array1 = body.split(" ");
                String username = "";
                if(array1[1].equals("1")){
                    username="*"+array1[0];
                } else {
                    username=array1[0];
                }
                if (isSessionKey(array1[0])) {
                    String message = getMessageOfUser(array1, 3);
                    String iv = array1[2];
                    SecretKey secretKey = findSecretKey(array1[0]);
                    byte[] ivBytes = Base64.getDecoder().decode(iv);
                    String decryptedMessage = SymmetricEncryptionUtils.decrypt(SymmetricEncryptionUtils.AES_CIPHER_ALGORITHM, message, secretKey, new IvParameterSpec(ivBytes));
                    System.out.println(username + " says to you privately: " + decryptedMessage);

                } else {
                    String message = getMessageOfUser(array1, 2);
                    System.out.println(username + " says to you privately: " + message);
                }
            }
            case "FILEACK" -> {
                String[] array2 = body.split(" ");
                String username = array2[0];
                String path = array2[1];
                System.out.println("A new file request from " + username);

                this.client.getFileRequests().add(new FileRequest(username, path));
            }
            case "FILEACK_OK" -> {
                String[] array2 = body.split(" ");
                String username = array2[2];
                String path = array2[1];
                System.out.println(username + " has accepted your file request");
                client.fileConnection.sendMsgToServer("FILESEND " + username + " " + path);
            }
            case "FILEACK_REJECT" -> {
                String[] array = body.split(" ");
                String username = array[2];

                System.out.println(username + " has rejected your file transfer request.");
            }
             case "JOIN" -> {
                String[] array3 = body.split(" ");
                String username2="";
                if(array3[1].equals("1")) {
                     username2 ="*"+array3[0];
                } else{
                    username2 = array3[0];
                }
                String groupName2 = array3[2];
                System.out.println(username2 + " has joined group called " + groupName2);
            }
            case "GROUP" -> {
                String[] array4 = body.split(" ");
                String username3 = array4[0];
                if(array4[1].equals("1")){
                    username3="*"+username3;
                }
                String groupName3 = array4[2];
                String message2 = getMessageOfUser(array4);
                System.out.println(username3 + " says to group chat(" + groupName3 + "): " + message2);
            }
            case "SSKEY" -> {
                String[] array5 = body.split(" ");
                String username4 = array5[0];
                String encodedSessionKey = array5[1];
                // decrypt the message to an encoded AES key, then decode the encoded symmetric key
                byte[] decodedKey = Base64.getDecoder().decode(AsymmetricEncryptionUtils.performRSADecryption(encodedSessionKey, this.privateKey));
                sessionKeys.put(username4, new SecretKeySpec(decodedKey, 0, decodedKey.length, SymmetricEncryptionUtils.AES));
                System.out.println("Open secure channel between " + username4);
            }
            case "BCST" -> {
                String[] array2 = body.split(" ");
                String username1 = "";
                if(array2[1].equals("1")){
                    username1="*"+array2[0];
                } else {
                    username1 = array2[0];
                }
                String message1 = getMessageOfUser(array2, 2);
                System.out.println(username1 + " broadcasts: " + message1);
            }
            case "ER01" -> System.out.println("User already logged in");
            case "ER02" -> System.out.println("Username has an invalid format (only characters, numbers and underscores are allowed " +
                    "as well as username should be less than 15 characters and more than 2 characters). Please,try again");
            case "ER03" -> System.out.println("You entered the wrong password. Please, try again");
            case "ER04" -> System.out.println("Please log in first");
            case "ER05" -> System.out.println("You cannot log out from the system if you did not log in yet");
            case "ER06" -> System.out.println("You cannot log in while you are already in the system");
            case "ER07" -> System.out.println("The given username does not exist in the system. Please, try again");
            case "ER08" -> System.out.println("You entered the wrong username! Please, try again"); //new
            case "ER09" -> System.out.println("The provided name of the group does already exist.");
            case "ER10" -> System.out.println("The name of group should not contain <,>");
            case "ER11" -> System.out.println("You filled in non-existing group name");
            case "ER12" -> System.out.println("You have already joined the desired group");
            case "ER13" -> System.out.println("You have not joined the group yet");
            case "ER14" -> System.out.println("You have to join the group first to leave it");
            case "ER15" -> System.out.println("You entered the wrong password! Please, try again");
            case "ER16" -> System.out.println("You have already authenticated");
            case "ER17" -> System.out.println("The provided password is incorrect for authentication");
            case "ER18" -> System.out.println("Only valid users are allowed to authenticate themselves");
            case "ER00" -> System.out.println("Unfortunately, the given command does not exist. If you do not know the existing commands, you can press: ?"); //new
            case "PING" -> writer.println("PONG");
            default -> System.out.println(reply);
        }
    }

    public void sendMsgToServer(String text) {
        if(!isSocketClosed) {
            if (text.startsWith("PUBKEY")) {
                String message = text + " " + AsymmetricEncryptionUtils.convertPublicKeyToString(publicKey);
                writer.println(message);
            }
            else if (text.startsWith("SSKEY")) {
                try {
                    SecretKey sessionKey = SymmetricEncryptionUtils.createAESKey();
                    String encodedSessionKey = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
                    String message = text + " " + encodedSessionKey;
                    writer.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (text.startsWith("PRIVATE")) {
                try {
                    if (isSessionKey(text.split(" ")[1])) {
                        sendPrivateMessage(text);
                    } else {
                        writer.println(text);
                    }
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
            else {
                writer.println(text);
            }
        }else {
            System.out.println("Unfortunately, your socket was closed, so now you are not able to send the messages to the server anymore");
        }
    }

    public boolean isSessionKey(String username) {
        for (Map.Entry mapElement: sessionKeys.entrySet()) {
            String key = (String) mapElement.getKey();
            if(key.equals(username)) {
                return true;
            }
        }
        return false;
    }
    public void sendPrivateMessage(String text) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String[] array = text.split(" ");
        String username= array[1];
        String message = getMessageOfUser(array, 2);
        SecretKey secretKey = findSecretKey(username);
        byte[] iv = SymmetricEncryptionUtils.createInitializationVector();
        String ivString = Base64.getEncoder().encodeToString(iv);
        String encryptedMessage = SymmetricEncryptionUtils.encrypt(SymmetricEncryptionUtils.AES_CIPHER_ALGORITHM, message, secretKey, new IvParameterSpec(iv));
        String messageToSend = "PRIVATE " + username + " " + ivString + " " + encryptedMessage;
        writer.println(messageToSend);
    }

    public SecretKey findSecretKey(String username) {
        SecretKey result = null;
        for (Map.Entry mapElement: sessionKeys.entrySet()) {
            String key = (String) mapElement.getKey();
            if(key.equals(username)) {
                result = (SecretKey) mapElement.getValue();
            }
        }
        return result;
    }
    public String[] sortResponseFromServer(String reply){
        String [] array = new String[2];
        StringBuilder firstPartOfResponse= new StringBuilder();
        StringBuilder secondPartOfResponse= new StringBuilder();
        char[] chars= reply.toCharArray();
        for (char aChar : chars) {
            if (aChar == ' ') {
                break;
            }
            firstPartOfResponse.append(aChar);
        }
        boolean isSecondPartOfResponse=false;
        for (char aChar : chars) {
            if (isSecondPartOfResponse) {
                secondPartOfResponse.append(aChar);
            }
            if (aChar == ' ') {
                isSecondPartOfResponse = true;
            }
        }
        array[0]= firstPartOfResponse.toString();
        array[1]= secondPartOfResponse.toString();
        return array;
    }

    public String getConnectedUsers(String[] array, int fromIndex) {
        StringBuilder message = new StringBuilder();
        for (int i = fromIndex; i < array.length; i++) {
            boolean isUserAuthenticated = false;
            if (i + 1 != array.length) {
                if (array[i + 1].equals("1")) {
                    isUserAuthenticated = true;
                }
                if (isUserAuthenticated) {
                    message.append("*" + array[i]);
                } else {
                    if (!array[i].equals("1") && !array[i].equals("0") && !array[i].equals("1,") && !array[i].equals("0,")) {
                        message.append(array[i]);
                    }
                }
                message.append(" ");
            }
        }
        return message.toString();
    }


    public String getMessageOfUser(String[] array, int fromIndex){
        StringBuilder message= new StringBuilder();
        for(int i = fromIndex;i<array.length;i++){
            message.append(array[i]);
            if(i+1!=array.length){
                message.append(" ");
            }
        }
        return message.toString();
    }



    public String getMessageOfUser(String[] array){
        StringBuilder message= new StringBuilder();
        for(int i=3;i<array.length;i++){
            message.append(array[i]);
            if(i+1!=array.length){
                message.append(" ");
            }
        }
        return message.toString();
    }

}
