package utils;

public class FileRequest {
    private String username;
    private String path;

    public FileRequest(String username, String path) {
        this.username = username;
        this.path = path;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "username='" + username + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getPath() {
        return path;
    }
}
