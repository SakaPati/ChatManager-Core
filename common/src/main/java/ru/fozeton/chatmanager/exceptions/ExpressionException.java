package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when a {@code MathEngine} expression has a syntax or evaluation error.
 */
@StandardException
public class ExpressionException extends ChatManagerException {
}
