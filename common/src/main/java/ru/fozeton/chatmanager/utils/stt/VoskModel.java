package ru.fozeton.chatmanager.utils.stt;

/**
 * Interface mapping to {@code org.vosk.Model}.
 * Used to avoid strict compile-time dependency on the Vosk library.
 */
public interface VoskModel extends AutoCloseable {
    void close();
}