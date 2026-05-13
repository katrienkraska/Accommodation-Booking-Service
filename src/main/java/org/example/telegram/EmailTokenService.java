package org.example.telegram;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.example.exception.EmailTokenGeneratorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailTokenService {

    private final SecretKeySpec secretKeySpec;

    public EmailTokenService(@Value("${telegram.secret}") String secret) {
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(), "AES");
    }

    public String encryptEmail(String email) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(email.getBytes());
            return Base64.getUrlEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException e) {
            throw new EmailTokenGeneratorException("Cant encrypt email: " + e.getMessage());
        }
    }

    public String decryptEmail(String token) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(
                Base64.getUrlDecoder().decode(token));
        return new String(decryptedBytes);
    }
}
