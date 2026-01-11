package ru.sinvic.model;

import lombok.NonNull;

public record DataInnerObject(int requestId, @NonNull String text) {
}
