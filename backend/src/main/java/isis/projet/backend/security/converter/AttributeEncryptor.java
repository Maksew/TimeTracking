package isis.projet.backend.security.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {
    private static final String SECRET = "MySuperSecretKey"; // 16 bytes for AES-128

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public AttributeEncryptor() {
        try {
            Key aesKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "AES");
            encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            decryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing AttributeEncryptor", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            System.out.println("Encrypting attribute: " + attribute);
            byte[] encryptedBytes = encryptCipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            System.out.println("Encrypted value: " + encrypted);
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            System.out.println("Decrypting dbData: " + dbData);
            byte[] decodedBytes = Base64.getDecoder().decode(dbData);
            String decrypted = new String(decryptCipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
            System.out.println("Decrypted value: " + decrypted);
            return decrypted;
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting attribute", e);
        }
    }
}
