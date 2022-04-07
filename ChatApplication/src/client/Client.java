package client;

import org.springframework.security.core.parameters.P;
import utils.EnvConfig;
import utils.FileRequest;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private ClientConnection clientConnection;
    public FileConnection fileConnection;

    private List<FileRequest> fileRequests = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String help = "\n"+"Commands: Client " + "\n" +
                """
                         1 - use this number to connect to the chat application with specific username 
                         0 - Help
                         4 - use this number to quit
                        """
                ;
        System.out.println(help);
        new Client();
    }

    public Client() throws Exception {
        try {
            Socket socket = new Socket(EnvConfig.HOST, EnvConfig.CLIENT_PORT);
            Socket fileSocket = new Socket(EnvConfig.HOST, EnvConfig.FILE_PORT);

            clientConnection = new ClientConnection(socket, this);
            clientConnection.start();

            fileConnection = new FileConnection(fileSocket);
            fileConnection.start();
            listenForInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForInput() {
        Scanner scanner = new Scanner(System.in);


        String command = "";

        while (true) {

            int input = -1;

            while (scanner.hasNext()) {
                try {
                    String userInput = scanner.nextLine();
                    input = Integer.parseInt(userInput);
                    break;
                } catch (NumberFormatException e) {
                    System.err.println("Please enter a valid input! (number only)");
                }
            }

            if(input >= 0 && input <= 20) {
                if(input == 1){
                    System.out.println("Please, enter the username which you want to use to connect to chat application with:");
                    String username = scanner.nextLine();
                    command = "CONN "+username;
                }else if(input == 2){
                    command="LOGOUT";

                } else if(input == 3){
                    System.out.println("Please, enter the message you want to broadcast: ");
                    String message = scanner.nextLine();
                    command = "BCST "+message;
                }else if(input == 4){
                    command = "QUIT";
                }else if(input == 5){
                    command = "LIST";
                }else if(input == 6){
                    command = "LISTGr";
                } else if(input == 7){
                    System.out.println("Please, enter the username of person you want to send message to:");
                    String username = scanner.nextLine();

                    System.out.println("Please, enter your message:");
                    String message = scanner.nextLine();

                    command="PRIVATE "+username+" "+message;
                } else if(input == 8){
                    System.out.println("Please, enter the name of group you want to create:");
                    String groupName = scanner.nextLine();

                    command="CREATE "+groupName;
                }else if(input == 9){
                    System.out.println("Please, enter the name of group you want to join:");
                    String groupName = scanner.nextLine();

                    command="JOIN "+groupName;
                }else if(input == 10){
                    System.out.println("Please, enter the name of group you want to send message to:");
                    String groupName = scanner.nextLine();

                    System.out.println("Please, enter your message:");
                    String message = scanner.nextLine();

                    command="GROUP "+groupName+" "+message;
                }else if(input == 11){
                    System.out.println("Please, enter the name of group you want leave:");
                    String groupName = scanner.nextLine();

                    command="LEAVE "+groupName;
                }else if(input == 12){
                    System.out.println("Please, enter your password to authenticate:");
                    String password = scanner.nextLine();

                    command="AUTH "+password;
                }else if (input == 13){
                    System.out.println("Enter username: ");
                    String username = scanner.nextLine();

                    if (!clientConnection.isSessionKey(username)) {
                        command = "SSKEY " + username;
                    } else {
                        System.out.println("A secure channel has been established with " + username);
                    }
                } else if (input == 14) {
                    System.out.println("Enter receiver username: ");
                    String username = scanner.nextLine();
                    while (username.length() == 0) {
                        System.out.println("Username cannot be empty!");
                        username = scanner.nextLine();
                    }

                    System.out.println("Enter file path to transfer: ");
                    String path = scanner.nextLine();
                    while (path.length() == 0) {
                        System.out.println("Path cannot be empty!");
                        path = scanner.nextLine();
                    }

                    command = "FILE " + username + " " + path;
                } else if (input == 15) {
                    System.out.println("Enter the username of the file request you want to accept: ");
                    String username = scanner.nextLine();
                    while (username.length() == 0) {
                        System.out.println("Username cannot be empty");
                        username = scanner.nextLine();
                    }

                    boolean userFound = false;
                    while (!userFound) {
                        for (var request: fileRequests) {
                            if (request.getUsername().equals(username)) {
                                userFound = true;
                                command = "FILEACK_OK " + request.getUsername() + " " + request.getPath();
                            }

                            if (userFound) {
                                System.out.println("File request accepted");
                                fileRequests.remove(request);
                                break;
                            }
                        }

                        if (!userFound) {
                            System.err.println("No request from this user!");
                        }
                    }

                } else if (input == 16) {
                    System.out.println("Enter the username of the file request you want to reject: ");
                    String username = scanner.nextLine();
                    while (username.length() == 0) {
                        System.out.println("Username cannot be empty");
                        username = scanner.nextLine();
                    }

                    boolean userFound = false;
                    while (!userFound) {
                        for (var request: fileRequests) {
                            if (request.getUsername().equals(username)) {
                                userFound = true;
                                command = "FILEACK_REJECT " + request.getUsername() + " " + request.getPath();
                            }

                            if (userFound) {
                                System.out.println("File request rejected!");
                                fileRequests.remove(request);
                                break;
                            }
                        }
                        if (!userFound) {
                            System.err.println("No request from this user!");
                        }
                    }
                } else if (input == 17) {
                    command = "FILEREQ";
                } else if (input == 0) {
                    command = "HELP";
                }
                clientConnection.sendMsgToServer(command);

                if (command.startsWith("CONN")) {
                    clientConnection.sendMsgToServer("PUBKEY");
                }
            }

        }
    }

    public List<FileRequest> getFileRequests() { return fileRequests; }
}
