package utils.crypto.asymmetric;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymmetricEncryptionUtils {
    private static final String RSA = "RSA";

    public static KeyPair generateRSAKeyPair() throws Exception{
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(4096, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] performRSAEncryption(String plainText, PublicKey publicKey) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainText.getBytes());
    }

    public static String performRSADecryption(String cipherText, PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(result);
    }

    public static String convertPublicKeyToString(PublicKey publicKey) {
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return publicKeyString;
    }
    // After receive the string of the public key, convert the string to the original public key
    public static PublicKey getOriginalPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder(). decode(publicKeyString.getBytes());

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey originalPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        return originalPublicKey;
    }
}
