package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;

public class LeavingGroupManager {
    public static void processLeavingFromTheGroup(String group, Server server,ServerConnection serverConnection, PrintWriter writer){
        boolean hasUserJoinedGroup = false;
        for (String userGroup : serverConnection.userGroups) {
            if (group.equals(userGroup)) {
                hasUserJoinedGroup = true;
                break;
            }
        }
        if (!hasUserJoinedGroup) {
            writer.println("ER14");
        } else {
            server.groups.get(group).remove(serverConnection);
            serverConnection.userGroups.remove(group);
            serverConnection.wereMessagesReceivedDuringLastTwoMinutes.remove(group);
            serverConnection.currentGroupNameOfUser = "";
            writer.println("OK LEAVE " + group);
        }
    }
}
