package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when an external dependency cannot be downloaded or added to the classpath.
 */
@StandardException
public class DependencyLoadException extends ChatManagerException {
}
