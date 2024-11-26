package convertor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StringUUIDConverter {
    private final String salt;
    private final Map<String, String> uuidToStringMap;

    public StringUUIDConverter(String salt) {
        this.salt = salt;
        this.uuidToStringMap = new HashMap<>();
    }

    public UUID convertToUUID(String originalString) {
        try {
            // Combine original string with salt
            String combinedString = originalString + salt;

            // Create SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combinedString.getBytes(StandardCharsets.UTF_8));

            // Use first 16 bytes for UUID (as UUID is 128 bits)
            ByteBuffer bb = ByteBuffer.wrap(hash, 0, 16);
            long mostSigBits = bb.getLong();
            long leastSigBits = bb.getLong();

            UUID uuid = new UUID(mostSigBits, leastSigBits);

            // Store the mapping for reverse lookup
            uuidToStringMap.put(uuid.toString(), originalString);

            return uuid;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public String convertFromUUID(UUID uuid) {
        String originalString = uuidToStringMap.get(uuid.toString());
        if (originalString == null) {
            throw new IllegalArgumentException("No mapping found for this UUID");
        }
        return originalString;
    }



    public static void main(String[] args) {
        // Example usage
        String salt = "mySaltString123";
        StringUUIDConverter converter = new StringUUIDConverter(salt);

        // Convert string to UUID
        String originalString = "Hello, World!";
        UUID uuid = converter.convertToUUID(originalString);
        System.out.println("Original String: " + originalString);
        System.out.println("Generated UUID: " + uuid);

        // Convert back to original string
        String retrievedString = converter.convertFromUUID(uuid);
        System.out.println("Retrieved String: " + retrievedString);
    }
}