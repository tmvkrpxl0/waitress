package uk.gemwire.waitress.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TempTOMLParser {
    public static HashMap<String, String> parse(String text){
        HashMap<String, String> map = new HashMap<>();
        text.lines().forEach(line -> {
            boolean doSomething = true;
            if (line.startsWith("#")) doSomething = false;
            if (line.startsWith("[[")) doSomething = false;
            if (line.isEmpty())doSomething = false;
            if (doSomething) {
                String[] split = line.split("=");
                String name = split[0].trim();
                String value;
                if (split[1].startsWith(" "))value = split[1].replaceFirst(" ", "");
                else value = split[1];
                map.put(name, value.replace("\"", ""));
            }
        });
        return map;
    }

    public static HashMap<String, String> parse(FileReader reader) throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        reader.close();
        return parse(buffer.toString());
    }
}
