package dev.jsinco.brewery.garden.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.garden.Garden;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String readInternalResource(String path) {
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement readJsonFromFile(String path) {
        return readJsonFromFile(new File(Garden.getInstance().getDataFolder(), path));
    }

    public static JsonElement readJsonFromFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return JsonParser.parseReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
