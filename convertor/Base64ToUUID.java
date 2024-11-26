package convertor;

import java.util.Base64;
import java.util.UUID;

public class Base64ToUUID {

    // Convert a Base64 string to a UUID
    public static UUID base64ToUUID(String base64Str) {
        // Decode the Base64 string into a byte array
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);

        // Ensure the decoded byte array has exactly 16 bytes for the UUID
        if (decodedBytes.length != 16) {
            throw new IllegalArgumentException("Base64 string must decode to exactly 16 bytes for UUID");
        }

        // Create a UUID from the decoded byte array
        long mostSigBits = 0;
        long leastSigBits = 0;

        // Fill the most significant bits (first 8 bytes)
        for (int i = 0; i < 8; i++) {
            mostSigBits |= ((long) decodedBytes[i] & 0xFF) << (8 * (7 - i));
        }

        // Fill the least significant bits (next 8 bytes)
        for (int i = 0; i < 8; i++) {
            leastSigBits |= ((long) decodedBytes[i + 8] & 0xFF) << (8 * (7 - i));
        }

        // Return a new UUID constructed from the most and least significant bits
        return new UUID(mostSigBits, leastSigBits);
    }

    // Convert a UUID to a Base64 string
    public static String uuidToBase64(UUID uuid) {
        // Get the most and least significant bits of the UUID
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        // Create a byte array of length 16 to hold the UUID data
        byte[] uuidBytes = new byte[16];

        // Convert most significant bits to the first 8 bytes of the byte array
        for (int i = 0; i < 8; i++) {
            uuidBytes[i] = (byte) (mostSigBits >> (8 * (7 - i)) & 0xFF);
        }

        // Convert least significant bits to the next 8 bytes of the byte array
        for (int i = 0; i < 8; i++) {
            uuidBytes[i + 8] = (byte) (leastSigBits >> (8 * (7 - i)) & 0xFF);
        }

        // Encode the byte array into a Base64 string
        return Base64.getEncoder().encodeToString(uuidBytes);
    }

    public static void main(String[] args) {
        try {
            // Example Base64 string that represents a 16-byte binary data for UUID
            String base64Str = "4Vn+84d66X0VZpuL6r+5ct2w4GB1kBm8GuWGQE4ALb0/ykiVRFEZqQda9aWu+aF5";

            // Convert Base64 to UUID
            UUID uuid = base64ToUUID(base64Str);
            System.out.println("Decoded UUID: " + uuid);

            // Convert UUID back to Base64
            String encodedBase64 = uuidToBase64(uuid);
            System.out.println("Encoded Base64: " + encodedBase64);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
