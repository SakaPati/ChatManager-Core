package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when loading, decoding or reading a GIF or {@code .mcanim} animation fails.
 */
@StandardException
public class GifException extends ChatManagerException {
}
