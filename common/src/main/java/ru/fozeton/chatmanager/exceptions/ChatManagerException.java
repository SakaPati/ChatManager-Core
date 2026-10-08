package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Base unchecked exception for all ChatManager errors.
 * Every other exception in this package extends it, so
 * {@code catch (ChatManagerException e)} catches any error thrown by the mod.
 */
@StandardException
public class ChatManagerException extends RuntimeException {
}
