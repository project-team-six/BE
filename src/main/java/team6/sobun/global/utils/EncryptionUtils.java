package team6.sobun.global.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES";

    @Value("${aes.secret.key}")
    private String secretKey;


    private SecretKeySpec getSecretKeySpec() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            return new SecretKeySpec(decodedKey, AES_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode secret key: " + e.getMessage());
        }
    }

    public String encrypt(String input) throws Exception {
        SecretKeySpec key = getSecretKeySpec();
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedInput) throws Exception {
        SecretKeySpec key = getSecretKeySpec();
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedInput));
        return new String(decryptedBytes);
    }
}
