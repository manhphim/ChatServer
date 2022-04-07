package utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordHashing {
    private String hashedPassword;

    public PasswordHashing(String password) {
        this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String password,String hashedPassword) {
        if (BCrypt.checkpw(password, hashedPassword)) {
            return true;
        } else {
            return false;
        }
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
