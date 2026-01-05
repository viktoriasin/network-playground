package ru.sinvic.server.http.perf;

import java.util.Arrays;

public class LeakedObject {
    public LeakedObject() {
        byte[] obj = new byte[1_048_576];
        Arrays.fill(obj, (byte) 0xFF);
    }
}
