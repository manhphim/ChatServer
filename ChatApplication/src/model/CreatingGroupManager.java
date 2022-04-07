package model;

import server.Server;
import server.ServerConnection;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class CreatingGroupManager {

    public static void processCreatingGroup(String groupName, Server server, PrintWriter writer){
        boolean isGroupNameCorrect=false;
        boolean doesGroupNameAlreadyExist = false;
        for (Map.Entry<String, ArrayList<ServerConnection>> set :
                server.groups.entrySet()) {
            if (set.getKey().equals(groupName)) {
                doesGroupNameAlreadyExist = true;
                break;
            }
        }
        isGroupNameCorrect = checkNameOfGroup(groupName);
        if (!doesGroupNameAlreadyExist && isGroupNameCorrect) {
            ArrayList<ServerConnection> group = new ArrayList<>();
            server.groups.put(groupName, group);
            writer.println("OK CREATE");
        } else if (doesGroupNameAlreadyExist) {
            writer.println("ER09");
        } else {
            writer.println("ER10");
        }
    }

    public static boolean checkNameOfGroup(String name) {
        boolean isGroupNameCorrect = true;
        char[] chars = name.toCharArray();
        StringBuilder character = new StringBuilder();
        for (char aChar : chars) {
            character.append(aChar);
            if (character.toString().equals(",")) {
                System.out.println("Was executed");
                isGroupNameCorrect = false;
            }
            character = new StringBuilder();
        }
        return isGroupNameCorrect;
    }

}
