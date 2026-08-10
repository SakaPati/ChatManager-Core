package ru.fozeton.chatmanager.utils.stt;

/**
 * Interface mapping to {@code org.vosk.Recognizer}.
 * Used to avoid strict compile-time dependency on the Vosk library.
 */
public interface VoskRecognizer extends AutoCloseable {

    /**
     * Feeds the next chunk of audio into the recognizer.
     *
     * @param data byte array containing audio PCM data
     * @param len  length of the valid audio data in the buffer
     * @return true if silence is occurred and you can retrieve a new utterance with result
     */
    boolean acceptWaveForm(byte[] data, int len);

    /**
     * Returns the final STT result as a JSON string.
     *
     * @return final recognition result in JSON format
     */
    String getResult();

    /**
     * Returns a partial, in-progress STT result as a JSON string.
     *
     * @return partial recognition result in JSON format
     */
    String getPartialResult();
    void close();
}