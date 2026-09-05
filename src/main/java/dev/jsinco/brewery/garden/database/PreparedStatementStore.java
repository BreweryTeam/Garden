package dev.jsinco.brewery.garden.database;

import dev.jsinco.brewery.garden.utility.FileUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreparedStatementStore {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final String resourceFolder;

    public PreparedStatementStore(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    public String get(String fileName) {
        String cachedValue = cache.get(fileName);
        if (cachedValue == null) {
            cachedValue = FileUtil.readInternalResource("%s/%s".formatted(resourceFolder, fileName));
        }
        return cachedValue;
    }
}
