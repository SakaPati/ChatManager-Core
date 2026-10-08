package ru.fozeton.chatmanager.exceptions;

import lombok.experimental.StandardException;

/**
 * Thrown when a received socket packet has an unknown type or cannot be decoded.
 */
@StandardException
public class UnknownPacketException extends ChatManagerException {
}