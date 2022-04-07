package model;

import server.Server;
import server.ServerConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public class ProcessQuitManager {
    public static void processQuiting(Server server, ServerConnection serverConnection, PrintWriter writer) throws IOException {
        server.currentlyConnectedUsers.remove(serverConnection.username);
        writer.println("OK QUIT");
        String username2;
        username2 = Objects.requireNonNullElse(serverConnection.username, "Client");
        System.out.println(username2 + " has been disconnected");
        serverConnection.shouldRun = false;
        serverConnection.socket.close();
    }
}
