package ru.sinvic.server.grpc;

import lombok.Data;

@Data
public class DataObject {
    private final int number;
    private final long longNumber;
    private final boolean isRandomNumber;
    private final String text;
}
