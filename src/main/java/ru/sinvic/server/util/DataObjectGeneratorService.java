package ru.sinvic.server.util;

import ru.sinvic.model.DataInnerObject;
import ru.sinvic.model.DataObject;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class DataObjectGeneratorService {
    private static final String INNER_OBJECT_STRING_FIELD = "Inner object";
    private static final int DATA_OBJECT_STRING_FIELD_LENGTH = 1000;
    private static final Random random = new Random();

    public static DataObject generateDataObject(int requestId) {
        UUID uuid = UUID.randomUUID();
        boolean randomBoolean = random.nextBoolean();
        String randomString = generateRandomStringFromBytesArray();
        DataInnerObject dataInnerObject = generateInnerObject(requestId);
        return new DataObject(requestId, uuid, randomBoolean, randomString, dataInnerObject);
    }

    private static DataInnerObject generateInnerObject(int requestId) {
        return new DataInnerObject(requestId, INNER_OBJECT_STRING_FIELD);
    }

    private static String generateRandomStringFromBytesArray() {
        byte[] arrayBytes = new byte[DATA_OBJECT_STRING_FIELD_LENGTH];

        for (int i = 0; i < DATA_OBJECT_STRING_FIELD_LENGTH; i++) {
            arrayBytes[i] = (byte) (32 + random.nextInt(95));
        }

        return new String(arrayBytes, StandardCharsets.UTF_8);
    }
}
