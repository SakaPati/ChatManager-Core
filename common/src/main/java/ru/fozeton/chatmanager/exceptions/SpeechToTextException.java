package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when speech recognition fails (Vosk, audio line, model).
 */
@StandardException
public class SpeechToTextException extends ChatManagerException {
}
