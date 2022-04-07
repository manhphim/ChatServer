package model;

import server.Server;
import server.ServerConnection;

import java.util.HashMap;
import java.util.Map;

public class ListingUsersManager {

    public static void processListingUsers(Server server,ServerConnection serverConnection){
        HashMap<String,Boolean> connectedUsers=new HashMap<>();
        for(int q=0;q<server.connections.size();q++) {
            if(server.connections.get(q).username!=null) {
                for (int k = 0; k < server.currentlyConnectedUsers.size(); k++) {
                    if (server.connections.get(q).username.equals(server.currentlyConnectedUsers.get(k))) {
                        connectedUsers.put(server.currentlyConnectedUsers.get(k), server.connections.get(q).isUserAuthenticated);
                    }
                }
            }
            }

        StringBuilder message = new StringBuilder();
        int counter=0;
        for (Map.Entry<String, Boolean> set :
                connectedUsers.entrySet()) {
            message.append(set.getKey());
            if (set.getValue()) {
                message.append(" 1");
                if (counter + 1 != connectedUsers.size()) {
                    message.append(", ");
                }
            } else{
                message.append(" 0");
                if (counter + 1 != connectedUsers.size()) {
                    message.append(", ");
                }
            }
            counter++;
        }
        serverConnection.sendMsgToClient("OK LIST " + message);
    }
}
