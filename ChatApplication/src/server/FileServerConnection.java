package server;

import java.io.*;
import java.net.Socket;

public class FileServerConnection extends Thread {
    private final Socket socket;
    private final Server server;
    private BufferedReader reader;
    private PrintWriter writer;

    private String username;

    protected ServerConnection serverConnection;

    public FileServerConnection(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = reader.readLine();
                String header = findHeader(message);
                String body = findBody(message);
                System.out.println(header);

                switch (header) {
                    case "FILESEND":
                        String[] bodySplits = body.split(" ");
                        String username = bodySplits[0];

                        System.out.println(username);

                        for (FileServerConnection fsc: server.fileConnections) {
                            if(fsc.getUsername().equals(username)) {
                                forwardFile(fsc);
                            }
                        }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeEverything();
        }

    }

    private String findHeader(String msg){
        char[] chars = msg.toCharArray();
        StringBuilder header= new StringBuilder();
        for (char aChar : chars) {
            if (aChar == ' ') {
                break;
            }
            header.append(aChar);
        }
        return header.toString();
    }
    public String findBody(String msg){
        char[] chars = msg.toCharArray();
        StringBuilder body= new StringBuilder();
        boolean isBody = false;
        for (char aChar : chars) {
            if (isBody) {
                body.append(aChar);
            }
            if (aChar == ' ') {
                isBody = true;
            }
        }
        return body.toString();
    }


    public void forwardFile(FileServerConnection receiver) throws IOException {
        try {
            if (receiver != null) {
                this.socket.getInputStream().transferTo(receiver.socket.getOutputStream());
                receiver.socket.getOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.socket.close();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void closeEverything() {
            try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
