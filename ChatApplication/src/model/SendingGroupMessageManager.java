package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class SendingGroupMessageManager {

    public static void processSendingGroupMessage(String body, Server server, ServerConnection serverConnection,PrintWriter writer){
        boolean wasNameOfGroupWrittenCorrectly=false;
        boolean hasUserJoinedTheGroup=false;

        String[] array = body.split(" ");
        String message = getMessageOfUser(array);
        String groupName = array[0];

        for (Map.Entry<String, ArrayList<ServerConnection>> set :
                server.groups.entrySet()) {
            if (set.getKey().equals(groupName)) {
                wasNameOfGroupWrittenCorrectly = true;
                break;
            }
        }
        for (String userGroup : serverConnection.userGroups) {
            if (groupName.equals(userGroup)) {
                hasUserJoinedTheGroup = true;
                break;
            }
        }
        if (!wasNameOfGroupWrittenCorrectly) {
            writer.println("ER11");
        } else if (!hasUserJoinedTheGroup) {
            writer.println("ER13");
        } else {
            wasNameOfGroupWrittenCorrectly = false;
            hasUserJoinedTheGroup = false;
            serverConnection.wereMessagesReceivedDuringLastTwoMinutes.put(groupName, true);
            writer.println("OK GROUP " + message);
            sendMsgToGroupParticipants(message, groupName,server,serverConnection);
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

    public static void sendMsgToGroupParticipants(String message, String nameOfGroup,Server server,ServerConnection serverConnection) {
        ArrayList<ServerConnection> group = server.groups.get(nameOfGroup);
        for (ServerConnection connection : group) {
            if (connection != serverConnection) {
                if(serverConnection.isUserAuthenticated) {
                    connection.writer.println("GROUP " + serverConnection.username + " 1" + " "+nameOfGroup + " " + message);
                } else{
                    connection.writer.println("GROUP " + serverConnection.username + " 0"+" " + nameOfGroup + " " + message);
                }
            }
        }

    }

}
