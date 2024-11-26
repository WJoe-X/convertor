package convertor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class ImprovedAESUtils {
    private static final byte[] SALT = new byte[]{(byte) 0x53, (byte) 0x6F, (byte) 0x72, (byte) 0x79,
            (byte) 0x6F, (byte) 0x6F, (byte) 0x6F, (byte) 0x71};
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static SecretKey generateAESKeyFromPassword(String password) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encryptToUUID(String originalData, String password) throws Exception {
        // Generate the AES key from the password
        SecretKey secretKey = generateAESKeyFromPassword(password);

        // Generate a random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // Create GCM parameter spec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(originalData.getBytes());

        // Combine IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        byte[] combined = byteBuffer.array();

        // Convert to Base64 and then create a deterministic UUID
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptFromUUID(String combined, String password) throws Exception {
        // Extract IV and encrypted data
        byte[] combinedBytes = combined.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(combinedBytes);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedData = new byte[combinedBytes.length - GCM_IV_LENGTH];
        byteBuffer.get(encryptedData);

        // Generate the AES key from the password
        SecretKey secretKey = generateAESKeyFromPassword(password);

        // Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData);
    }


    public static void main(String[] args) {
        try {
            String originalData = "Hello, this is a secret message that can be very long!";
            String password = "ThisIsAnArbitraryLengthPassword";

            String encryptedUUID = encryptToUUID(originalData, password);
            System.out.println("Encrypted (UUID format): " + encryptedUUID);

            String decrypted = decryptFromUUID(encryptedUUID, password);
            System.out.println("Decrypted: " + decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
