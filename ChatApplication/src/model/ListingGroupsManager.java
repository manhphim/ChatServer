package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class ListingGroupsManager {
    public static void processListingGroups(Server server,PrintWriter writer){
        StringBuilder message = new StringBuilder();
        int counter = 0;
        for (Map.Entry<String, ArrayList<ServerConnection>> set :
                server.groups.entrySet()) {
            counter++;
            message.append(set.getKey());
            if (counter != server.groups.size()) {
                message.append(",");
            }
        }
        sendMsgToClient("OK LISTGr " + message,writer);
    }

    public static void sendMsgToClient(String text, PrintWriter writer) {
        writer.println(text);
    }
}
