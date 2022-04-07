package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JoiningGroupManager {
    public static void processJoiningGroup(String groupName, Server server, ServerConnection serverConnection, PrintWriter writer){
        boolean wasNameOfGroupWrittenCorrectly=false;
        boolean didUserJoinTheGroup=false;
        String currentGroupNameOfUser = groupName;
        for (Map.Entry<String, ArrayList<ServerConnection>> set :
                server.groups.entrySet()) {
            if (set.getKey().equals(groupName)) {
                wasNameOfGroupWrittenCorrectly = true;
                break;
            }
        }
        for (String userGroup : serverConnection.userGroups) {
            if (userGroup.equals(currentGroupNameOfUser)) {
                didUserJoinTheGroup = true;
                break;
            }
        }
        if (!wasNameOfGroupWrittenCorrectly) {
            serverConnection.currentGroupNameOfUser = "";
            writer.println("ER11");
        } else if (didUserJoinTheGroup) {
            serverConnection.wasNameOfGroupWrittenCorrectly = false;
            writer.println("ER12");
        } else {
            server.groups.get(groupName).add(serverConnection);
            serverConnection.didUserJoinTheGroup = false;
            serverConnection.wasNameOfGroupWrittenCorrectly = false;
            String messageToAllClients="";
            if(serverConnection.isUserAuthenticated) {
                messageToAllClients = "JOIN " + serverConnection.username + " 1 " + currentGroupNameOfUser;
            } else{
                messageToAllClients="JOIN " + serverConnection.username + " 0 " + currentGroupNameOfUser;
            }            notifyAllUsers(messageToAllClients, serverConnection,server);
            serverConnection.userGroups.add(currentGroupNameOfUser);
            serverConnection.wereMessagesReceivedDuringLastTwoMinutes.put(groupName, false);
            checkUserAvailability(groupName,serverConnection,server,writer);
            writer.println("OK JOIN");
        }


    }

    public static void notifyAllUsers(String text, ServerConnection sender,Server server) {
        for (int i = 0; i < server.connections.size(); i++) {
            if (server.connections.get(i).isClientLoggedIn && sender != server.connections.get(i)) {
                ServerConnection serverConnection = server.connections.get(i);
                serverConnection.sendMsgToClient(text);
            }
        }
    }

    public static void checkUserAvailability(String groupName,ServerConnection serverConnection,Server server,PrintWriter writer) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            boolean hasUserAlreadyLeftGroup = true;
            for (Map.Entry<String, Boolean> set :
                    serverConnection.wereMessagesReceivedDuringLastTwoMinutes.entrySet()) {
                if (set.getKey().equals(groupName)) {
                    serverConnection.wasNameOfGroupWrittenCorrectly = false;
                    break;
                }
            }
            if (hasUserAlreadyLeftGroup) {
                scheduledExecutorService.shutdown();
            }
            if (!serverConnection.wereMessagesReceivedDuringLastTwoMinutes.get(groupName)) {
                server.groups.get(groupName).remove(serverConnection);
                serverConnection.userGroups.remove(groupName);
                serverConnection.wereMessagesReceivedDuringLastTwoMinutes.remove(groupName);
                serverConnection.currentGroupNameOfUser = "";
                writer.println("OK FORCEDLEAVE " + groupName);

                scheduledExecutorService.shutdown();
            } else {
                serverConnection.wereMessagesReceivedDuringLastTwoMinutes.put(groupName, false);
            }
        }, 2, 2, TimeUnit.MINUTES);
    }
}
