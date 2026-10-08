package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when a command is executed without the required permission or authorization.
 */
@StandardException
public class ExecutingUnauthorizedCommandException extends ChatManagerException {
}