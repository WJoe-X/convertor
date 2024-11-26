package convertor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileStringReader {

    /**
     * Read file using Files.readString (Java 11+)
     */
    public static String readUsingFiles(String filePath) throws IOException {
        return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Read file using BufferedReader
     */
    public static String readUsingBufferedReader(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Read file using Files.lines() stream (Java 8+)
     */
    public static String readUsingStream(String filePath) throws IOException {
        try (var lines = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            return lines.collect(Collectors.joining("\n"));
        }
    }

    /**
     * Read file as single string with try-with-resources
     */
    public static String readUsingInputStream(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath);
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Read small file in one go
     */
    public static String readSmallFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read file into List of lines using Files.readAllLines
     */
    public static List<String> readAllLines(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        // Example file path - adjust according to your system
        String filePath = "salt.txt";

        try {
            // First, let's create a test file
            String testContent = "Hello, World!\nThis is a test file.\nMultiple lines of content.";
            Files.write(Paths.get(filePath), testContent.getBytes(StandardCharsets.UTF_8));

            // Now read it using different methods
            System.out.println("Using Files.readString (Java 11+):");
            System.out.println(readUsingFiles(filePath));
            System.out.println("\nUsing BufferedReader:");
            System.out.println(readUsingBufferedReader(filePath));
            System.out.println("\nUsing Stream:");
            System.out.println(readUsingStream(filePath));
            System.out.println("\nUsing InputStream:");
            System.out.println(readUsingInputStream(filePath));
            System.out.println("\nReading small file:");
            System.out.println(readSmallFile(filePath));

            // Performance test with a larger file
            System.out.println("\nPerformance test:");
            String largeContent = "Hello, World!".repeat(100000);
            String largePath = "code.txt";
            Files.write(Paths.get(largePath), largeContent.getBytes(StandardCharsets.UTF_8));

            // Test each method
            long startTime;

            startTime = System.nanoTime();
            readUsingFiles(largePath);
            System.out.printf("Files.readString: %.2f ms%n",
                    (System.nanoTime() - startTime) / 1_000_000.0);

            startTime = System.nanoTime();
            readUsingBufferedReader(largePath);
            System.out.printf("BufferedReader: %.2f ms%n",
                    (System.nanoTime() - startTime) / 1_000_000.0);

            startTime = System.nanoTime();
            readUsingStream(largePath);
            System.out.printf("Stream: %.2f ms%n",
                    (System.nanoTime() - startTime) / 1_000_000.0);

            startTime = System.nanoTime();
            readUsingInputStream(largePath);
            System.out.printf("InputStream: %.2f ms%n",
                    (System.nanoTime() - startTime) / 1_000_000.0);

            // Clean up test files
            //Files.delete(Paths.get(filePath));
            //Files.delete(Paths.get(largePath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}