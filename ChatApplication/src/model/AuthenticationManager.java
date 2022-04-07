package model;

import server.Server;
import server.ServerConnection;
import utils.PasswordHashing;

import java.io.PrintWriter;
import java.util.Map;

public class AuthenticationManager {
    public static boolean processAuthentication(Server server, String password, ServerConnection serverConnection,PrintWriter writer){
        if(serverConnection.isUserAuthenticated){
            writer.println("ER16");
        } else {
            boolean isPasswordCorrect = false;
            boolean isUsernameCorrect = false;
            for (Map.Entry<String, String> set :
                    server.users.entrySet()) {
                if (set.getKey().equals(serverConnection.username)) {
                    isUsernameCorrect = true;
                    if (PasswordHashing.checkPassword(password,set.getValue())) {
                        isPasswordCorrect = true;
                        writer.println("OK AUTH");
                        serverConnection.isUserAuthenticated=true;
                        return true;
                    }
                }
            }
            if (!isUsernameCorrect) {
                writer.println("ER18");
            } else if (!isPasswordCorrect) {
                writer.println("ER17");
            }
        }
        serverConnection.isUserAuthenticated=false;
        return false;
    }
}
