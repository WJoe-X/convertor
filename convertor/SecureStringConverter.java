package convertor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SecureStringConverter {
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int GCM_TAG_LENGTH = 128;

    // 存储密码，但不直接使用
    private final String password;

    public SecureStringConverter(String password) {
        this.password = password;
    }

    /**
     * 将字符串转换为加密后的格式
     */
    public String convert(String input) throws Exception {
        // 生成随机盐值和IV
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        // 从密码派生密钥
        SecretKey key = deriveKey(password, salt);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // 添加关联数据（salt）以增加安全性
        cipher.updateAAD(salt);

        // 加密数据
        byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        // 组合所有数据：salt + iv + 密文
        ByteBuffer buffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
        buffer.put(salt);
        buffer.put(iv);
        buffer.put(cipherText);

        // 转换为Base58编码（比Base64更适合URL，且不包含容易混淆的字符）
        return Base58.encode(buffer.array());
    }

    /**
     * 将加密的字符串转换回原始格式
     */
    public String revert(String encrypted) throws Exception {
        // 解码Base58
        byte[] allBytes = Base58.decode(encrypted);

        // 提取salt、iv和密文
        ByteBuffer buffer = ByteBuffer.wrap(allBytes);
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        byte[] cipherText = new byte[allBytes.length - SALT_LENGTH - IV_LENGTH];

        buffer.get(salt);
        buffer.get(iv);
        buffer.get(cipherText);

        // 从密码派生密钥
        SecretKey key = deriveKey(password, salt);

        // 初始化解密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // 添加关联数据（salt）
        cipher.updateAAD(salt);

        // 解密
        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用PBKDF2派生密钥
     */
    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATION_COUNT,
                KEY_LENGTH
        );
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 生成随机字节
     */
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * Base58编码实现（比Base64更适合URL，且避免混淆字符）
     */
    private static class Base58 {
        private static final char[] ALPHABET =
                "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
        private static final int[] INDEXES = new int[128];

        static {
            for (int i = 0; i < INDEXES.length; i++) {
                INDEXES[i] = -1;
            }
            for (int i = 0; i < ALPHABET.length; i++) {
                INDEXES[ALPHABET[i]] = i;
            }
        }

        public static String encode(byte[] input) {
            if (input.length == 0) {
                return "";
            }

            // 计算前导零
            int zeros = 0;
            while (zeros < input.length && input[zeros] == 0) {
                zeros++;
            }

            // 复制输入数组并转换为Base58
            byte[] temp = new byte[input.length * 2];
            int j = temp.length;

            int startAt = zeros;
            while (startAt < input.length) {
                byte mod = divmod58(input, startAt);
                if (input[startAt] == 0) {
                    startAt++;
                }
                temp[--j] = (byte) ALPHABET[mod];
            }

            while (j < temp.length && temp[j] == ALPHABET[0]) {
                ++j;
            }

            while (--zeros >= 0) {
                temp[--j] = (byte) ALPHABET[0];
            }

            byte[] output = new byte[temp.length - j];
            System.arraycopy(temp, j, output, 0, output.length);
            return new String(output);
        }

        public static byte[] decode(String input) {
            if (input.length() == 0) {
                return new byte[0];
            }

            byte[] input58 = new byte[input.length()];
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                int digit58 = -1;
                if (c >= 0 && c < 128) {
                    digit58 = INDEXES[c];
                }
                if (digit58 < 0) {
                    throw new RuntimeException("Invalid Base58 character: " + c);
                }
                input58[i] = (byte) digit58;
            }

            // 计算结果长度
            int zeros = 0;
            while (zeros < input58.length && input58[zeros] == 0) {
                zeros++;
            }

            byte[] temp = new byte[input.length()];
            int j = temp.length;

            int startAt = zeros;
            while (startAt < input58.length) {
                byte mod = divmod256(input58, startAt);
                if (input58[startAt] == 0) {
                    startAt++;
                }
                temp[--j] = mod;
            }

            while (j < temp.length && temp[j] == 0) {
                ++j;
            }

            byte[] output = new byte[temp.length - j + zeros];
            System.arraycopy(temp, j, output, zeros, temp.length - j);
            return output;
        }

        private static byte divmod58(byte[] number, int startAt) {
            int remainder = 0;
            for (int i = startAt; i < number.length; i++) {
                int digit256 = number[i] & 0xFF;
                int temp = remainder * 256 + digit256;
                number[i] = (byte) (temp / 58);
                remainder = temp % 58;
            }
            return (byte) remainder;
        }

        private static byte divmod256(byte[] number58, int startAt) {
            int remainder = 0;
            for (int i = startAt; i < number58.length; i++) {
                int digit58 = number58[i] & 0xFF;
                int temp = remainder * 58 + digit58;
                number58[i] = (byte) (temp / 256);
                remainder = temp % 256;
            }
            return (byte) remainder;
        }
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            SecureStringConverter converter = new SecureStringConverter("your_strong_password");

            // 测试用例
            String[] testStrings = {
                    "Hello, World!",
                    "这是中文测试",
                    "Special chars: !@#$%^&*()",
                    "12345678901234567890",
                    "Mixed content: ABC123测试"
            };

            for (String original : testStrings) {
                String converted = converter.convert(original);
                String reverted = converter.revert(converted);

                System.out.println("原文: " + original);
                System.out.println("加密: " + converted);
                System.out.println("解密: " + reverted);
                System.out.println("验证: " + original.equals(reverted));
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
