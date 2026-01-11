package ru.sinvic.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static Properties readProperties(String fileName) throws IOException {
        Properties props = new Properties();
        try (InputStream is = Config.class.getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Ресурс не найден: " + fileName);
            }
            props.load(is);
        }
        return props;
    }
}
