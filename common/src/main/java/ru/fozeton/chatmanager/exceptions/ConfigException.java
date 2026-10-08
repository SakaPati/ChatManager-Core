package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when reading, writing or deserializing configs and resources fails.
 */
@StandardException
public class ConfigException extends ChatManagerException {
}
