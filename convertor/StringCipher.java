package convertor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class StringCipher {
    private final int[] shuffleMap;

    public StringCipher(String password) {
        this.shuffleMap = generateShuffleMap(password);
    }

    private int[] generateShuffleMap(String password) {
        // generate a seed
        long seed = 0;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xff);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }

        // 创建一个固定的映射表
        Random random = new Random(seed);
        int[] map = new int[256];
        for (int i = 0; i < 256; i++) {
            map[i] = i;
        }

        // Fisher-Yates shuffle
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // swap
            int temp = map[i];
            map[i] = map[j];
            map[j] = temp;
        }

        return map;
    }

    public String encrypt(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        byte[] bytes = text.getBytes();
        byte[] result = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            result[i] = (byte) shuffleMap[value];
        }

        // 转换为十六进制字符串
        StringBuilder hex = new StringBuilder();
        for (byte b : result) {
            hex.append(String.format("%02X", b & 0xFF));
        }

        return hex.toString();
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }

        // 创建解密映射表
        int[] reverseMap = new int[256];
        for (int i = 0; i < 256; i++) {
            reverseMap[shuffleMap[i]] = i;
        }

        // 将十六进制字符串转回字节数组
        int len = encrypted.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) Integer.parseInt(encrypted.substring(i, i + 2), 16);
        }

        // 解密
        byte[] decrypted = new byte[result.length];
        for (int i = 0; i < result.length; i++) {
            int value = result[i] & 0xFF;
            decrypted[i] = (byte) reverseMap[value];
        }

        return new String(decrypted);
    }

    // 测试方法
    public static void main(String[] args) {
        StringCipher cipher = new StringCipher("my_secret_password");

        // 测试用例
        String[] testStrings = {
                "Hello, World!",
                "这是中文测试",
                "123456789",
                "Special chars: !@#$%^&*()",
                "Mixed content: ABC123测试"
        };

        String decryptedTest = cipher.decrypt("FD0CA006C0585F3E5DBB065DBB8558A37054DBAE93F750BD31D81A");
        System.out.println("Verified: " + "Mixed content: ABC123测试".equals(decryptedTest));

        for (String original : testStrings) {
            String encrypted = cipher.encrypt(original);
            String decrypted = cipher.decrypt(encrypted);

            System.out.println("Original: " + original);
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Decrypted: " + decrypted);
            System.out.println("Verified: " + original.equals(decrypted));
            System.out.println();
        }
    }
}