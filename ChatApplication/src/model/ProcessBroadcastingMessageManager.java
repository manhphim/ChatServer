package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;

public class ProcessBroadcastingMessageManager {

    public static void processBroadcasting(String body,PrintWriter writer,ServerConnection sender,Server server){
        sendMsgToClient("OK BCST " + body,writer);
        sendMsgToAllClientsExceptSender(body, sender,server);
    }

    public static void sendMsgToClient(String text, PrintWriter writer) {
        writer.println(text);
    }

    public static void sendMsgToAllClientsExceptSender(String message, ServerConnection sender, Server server) {
        for (int i = 0; i < server.connections.size(); i++) {
            if (server.connections.get(i).isClientLoggedIn && sender != server.connections.get(i)) {
                ServerConnection serverConnection = server.connections.get(i);
                if(sender.isUserAuthenticated) {
                    serverConnection.sendMsgToClient("BCST" + " " + sender.username + " 1 " + message);
                } else {
                    serverConnection.sendMsgToClient("BCST" + " " + sender.username + " 0 " + message);
                }
            }
        }
    }

}
