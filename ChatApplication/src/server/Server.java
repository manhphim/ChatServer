package server;

import utils.EnvConfig;
import utils.PasswordHashing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private ServerSocket clientServerSocket;
    private ServerSocket fileServerSocket;
    public ArrayList<ServerConnection> connections = new ArrayList<>();
    public ArrayList<FileServerConnection> fileConnections = new ArrayList<>();
    public ArrayList<String> usernames=new ArrayList<>();
    public ArrayList<String> currentlyConnectedUsers=new ArrayList<>();
    public HashMap<String,ArrayList<ServerConnection>> groups=new HashMap<>();
    public HashMap<String,String> users=new HashMap<>();
    private boolean shouldRun = true;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        // add dummy data to user list.
        initUserData();
        try {
            clientServerSocket = new ServerSocket(EnvConfig.CLIENT_PORT);
            fileServerSocket = new ServerSocket(EnvConfig.FILE_PORT);
            while (shouldRun) {
                var clientSocket = clientServerSocket.accept();
                var fileSocket = fileServerSocket.accept();

                var serverConnection = new ServerConnection(clientSocket, fileSocket, this);
                serverConnection.start();

                connections.add(serverConnection);
               // serverConnection.sendMsgToClient("INFO <WELCOME TO OUR SYSTEM>");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initUserData(){
        String username="zhvarikkk";
        users.put(username, new PasswordHashing(username).getHashedPassword());

        String username1="pham3000";
        users.put(username1, new PasswordHashing(username1).getHashedPassword());

        String username2="ghost6";
        users.put(username2,new PasswordHashing(username2).getHashedPassword());
    }


}
