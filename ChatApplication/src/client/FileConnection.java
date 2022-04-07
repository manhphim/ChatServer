package client;

import utils.FileUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class FileConnection extends Thread {
    private final Socket socket;
    private static final String baseDirectory = "downloads";
    private static final int CHUNK_SIZE = 8192;

    private String checksum = "";

    private PrintWriter writer;
    private DataInputStream dis;



    public FileConnection(Socket socket) {
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                var dir = new File(baseDirectory);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                String[] reply = dis.readUTF().split(" ");

                String name = reply[0];

                String path = baseDirectory + "/" + name;

                checksum = reply[1];
                File file = new File(path);
                long fileSize = dis.readLong();
                System.out.println("Saving " + name + " from user... ("
                        + fileSize + " bytes)");
                saveFile(file, this.dis, fileSize);
                System.out.println("Finished downloading " + name + " from user.");
                if (file.length() != fileSize) {
                    System.err.println("Error: file incomplete");
                }
                compareChecksum(file.getPath());
            } catch (Exception e) {

            }
        }
    }

    public String[] sortMessage(String message) {
        return message.split(" ");
    }

    public void sendMsgToServer(String message) {
        this.writer.println(message);
        this.sendFileToServer(message);
    }

    public void sendFileToServer(String message) {
        if(!socket.isClosed()) {
            try {
                String[] sortedMessage = sortMessage(message);
                String path = sortedMessage[2];
                sendFile(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Unfortunately, your socket was closed, so now you are not able to send the messages to the server anymore");
        }
    }

    private void compareChecksum(String path) {
        try {
            String checksumGenerated = FileUtils.createChecksum(path);
            if (checksumGenerated.equals(this.checksum)) {
                System.out.println("File is good!");
            } else {
                System.out.println("File is broken :(");
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String path) {
        if (path == null) {
            throw new NullPointerException("Path is null");
        }

        File file = new File(path);
        try {
            var dos = new DataOutputStream(
                    socket.getOutputStream());
            dos.writeUTF(file.getName() + " " + FileUtils.createChecksum(path));
            dos.writeLong(file.length());
            System.out.println("Sending " + file.getName() + " ("
                    + file.length() + " bytes) to server...");
            writeFile(file, dos);
            System.out.println("Finished sending " + file.getName()
                    + " to server");
            System.out.println("File sent.");
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void writeFile(File file, OutputStream outStream) {
        FileInputStream reader = null;
        try {
            reader = new FileInputStream(file);
            byte[] buffer = new byte[CHUNK_SIZE];
            int pos = 0;
            int bytesRead;
            while ((bytesRead = reader.read(buffer, 0, CHUNK_SIZE)) >= 0) {
                outStream.write(buffer, 0, bytesRead);
                outStream.flush();
                pos += bytesRead;
                System.out.println(pos + " bytes (" + bytesRead + " bytes read)");
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error while reading file");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error while writing " + file + " to output stream");
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveFile(File file, InputStream inStream, long fileSize) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int pos = 0;
            while ((bytesRead = inStream.read(buffer, 0, CHUNK_SIZE)) >= 0) {
                pos += bytesRead;
                System.out.println(pos + " bytes (" + bytesRead + " bytes read)");
                fileOut.write(buffer, 0, bytesRead);
                fileOut.flush();
                if (pos == fileSize) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Finished, filesize = " + file.length());
    }
}


