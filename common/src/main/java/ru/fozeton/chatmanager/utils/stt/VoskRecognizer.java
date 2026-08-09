package ru.fozeton.chatmanager.utils.stt;

public interface VoskRecognizer extends AutoCloseable {
    boolean acceptWaveForm(byte[] data, int len);
    String getResult();
    String getPartialResult();
    void close();
}
