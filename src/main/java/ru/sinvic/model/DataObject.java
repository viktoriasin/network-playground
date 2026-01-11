package ru.sinvic.model;

import lombok.NonNull;

import java.util.UUID;

public record DataObject(int requestId, @NonNull UUID uuid, boolean randomBoolean, @NonNull String text, @NonNull DataInnerObject dataInnerObject) {
}

