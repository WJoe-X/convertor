package convertor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Generator {


    public static void main(String[] args) {
        // Example usage
        String saltFileName = "salt.txt";
        String originalFileName = "code.txt";
        try {
            String  salt = FileStringReader.readSmallFile(saltFileName);
            if (salt.isBlank()) {
                System.out.println("salt is empty");
                return;
            }
            List<String> originalList = FileStringReader.readAllLines(originalFileName);
            if (originalList.isEmpty()) {
                System.out.println("originalList is empty");
                return;
            }
            SecureStringConverter converter = new SecureStringConverter(salt);

            List<String> outPut = new ArrayList<>();
            for (String o : originalList) {
               String str = StringHandler.transformString(o);
                String uuid = converter.convert(str);
                outPut.add(uuid);
            }

            Files.write(Paths.get("output.txt"), outPut);
            List<String> originalInput = new ArrayList<>();
            for (String o : outPut) {
                String uuid = converter.revert(o);
                String str = StringHandler.reverse(uuid);

                originalInput.add(str);
                Files.write(Paths.get("originalInput.txt"), originalInput);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
