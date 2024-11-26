package convertor;

import java.util.UUID;


public class StringHandler {


    public static void main(String[] args) {
        String input = "xxx   asd   test  example";
        String transformedString = transformString(input);
        System.out.println("Transformed String: " + transformedString);
    }

    // 转换字符串
    public static String transformString(String input) {
        String sanitizedInput = input.trim().replaceAll("\\s+", "_");

        String[] segments = sanitizedInput.split("_");
        StringBuilder result = new StringBuilder();

        for (String segment : segments) {
            result.append(new StringBuilder(segment).reverse()).append("_");
        }

        if (!result.isEmpty() && result.charAt(result.length() - 1) == '_') {
            result.setLength(result.length() - 1);
        }

        String prefix = generateRandomString();
        String suffix = generateRandomString();

        // 拼接前缀、内容、后缀
        return prefix + "_" + result.toString() + "_" + suffix;
    }

    private static String generateRandomString() {
        return UUID.randomUUID().toString().substring(0, 4);
    }

    public static String reverse(String input) {
        String[] segments = input.split("_");

        if (segments.length < 3) {
            throw new IllegalArgumentException("Input string must contain at least a prefix, content, and suffix.");
        }
        String[] contentSegments = new String[segments.length - 2];
        System.arraycopy(segments, 1, contentSegments, 0, segments.length - 2);

        StringBuilder result = new StringBuilder();
        for (String segment : contentSegments) {
            result.append(new StringBuilder(segment).reverse()).append(" ");
        }

        return result.toString().trim();
    }
}

