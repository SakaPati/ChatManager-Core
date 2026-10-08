package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when a component or provider is used before it has been initialized (or is initialized twice).
 */
@StandardException
public class NotInitializedException extends ChatManagerException {
}
