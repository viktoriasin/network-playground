package ru.sinvic.server.http.perf;

import java.util.Arrays;

public class LeakedObject {

    private static final int OBJECT_SIZE_BYTES = 1024 * 1024;

    public LeakedObject() {
        byte[] obj = new byte[OBJECT_SIZE_BYTES];
        Arrays.fill(obj, (byte) 0xFF);
    }
}
