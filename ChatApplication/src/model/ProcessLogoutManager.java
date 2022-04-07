package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;

public class ProcessLogoutManager {
    public static void processLogout(ServerConnection serverConnection, Server server,PrintWriter writer){
        if (!serverConnection.isClientLoggedOut) {
            serverConnection.isClientSuccessfullyLoggedIn = false;
            serverConnection.isClientJustLoggedOut = true;
            serverConnection.isClientLoggedIn = false;
            serverConnection.isUserAuthenticated=false;
            server.currentlyConnectedUsers.remove(serverConnection.username);
            server.usernames.remove(serverConnection.username);
            server.currentlyConnectedUsers.remove(serverConnection.username);
            sendMsgToClient("OK LOGOUT",writer);
            sendMsgToClient("OK HELP1",writer);
        }
    }

    public static void sendMsgToClient(String text, PrintWriter writer) {
        writer.println(text);
    }
}
