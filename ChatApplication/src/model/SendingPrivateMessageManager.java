package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;
import java.io.Writer;

public class SendingPrivateMessageManager {
    public static void processSendingPrivateMessage(String body, Server server, ServerConnection serverConnection, PrintWriter writer){
        boolean wasPrivateConnectionEstablished=false;
        ServerConnection privateServerConnection=null;
        String[] array = body.split(" ");
        String username = array[0];
        for (int i = 0; i < server.connections.size(); i++) {
            if(server.connections.get(i).username!=null) {
                if (server.connections.get(i).username.equals(username)) {
                    privateServerConnection = server.connections.get(i);
                    wasPrivateConnectionEstablished = true;
                    String message = getMessageOfUser(array);
                    sendMsgToCertainClient(message, serverConnection, privateServerConnection);
                    writer.println("OK PRIVATE " + message);
                }
            }
        }
        if (!wasPrivateConnectionEstablished) {
            writer.println("ER07");
        }
    }

    public static void processFileTransfer(String body, Server server, ServerConnection serverConnection, PrintWriter writer) {
        boolean wasPrivateConnectionEstablished=false;
        ServerConnection privateServerConnection=null;
        String[] array = body.split(" ");
        String username = array[0];
        String path = array[1];
        for (int i = 0; i < server.connections.size(); i++) {
            if(server.connections.get(i).username!=null) {
                if (server.connections.get(i).username.equals(username)) {
                    privateServerConnection = server.connections.get(i);
                    wasPrivateConnectionEstablished = true;
                    String message = getMessageOfUser(array);
                    privateServerConnection.writer.println("FILEACK " + serverConnection.username + " " + path);
                    writer.println("OK FILE " + message);
                }
            }
        }
        if (!wasPrivateConnectionEstablished) {
            writer.println("ER07");
        }
    }

    public static void processFile(String message, Server server, ServerConnection serverConnection, PrintWriter writer, String header) {
        boolean wasPrivateConnectionEstablished=false;
        ServerConnection privateServerConnection=null;
        String[] array = message.split(" ");
        String username = array[1];
        for (int i = 0; i < server.connections.size(); i++) {
            if(server.connections.get(i).username!=null) {
                if (server.connections.get(i).username.equals(username)) {
                    privateServerConnection = server.connections.get(i);
                    wasPrivateConnectionEstablished = true;
                    privateServerConnection.writer.println(message + " " + serverConnection.username);
                    if (header.equals("FILEACK")) writer.println("OK FILEACK");
                    if (header.equals("FILEACK_OK")) writer.println("OK FILEACK_OK");
                    if (header.equals("FILEACK_REJECT")) writer.println("OK FILEACK_REJECT");
                }
            }
        }
        if (!wasPrivateConnectionEstablished) {
            writer.println("ER07");
        }
    }

    public static String getMessageOfUser(String[] array) {
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < array.length; i++) {
            message.append(array[i]);
            if (i + 1 != array.length) {
                message.append(" ");
            }
        }
        return message.toString();
    }

    public static void sendMsgToCertainClient(String text, ServerConnection serverConnection,ServerConnection privateServerConnection) {
        if(serverConnection.isUserAuthenticated) {
            privateServerConnection.writer.println("PRIVATE " + serverConnection.username + " 1 " + text);
        } else{
            privateServerConnection.writer.println("PRIVATE " + serverConnection.username + " 0 " + text);
        }
    }

}
