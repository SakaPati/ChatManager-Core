package ru.fozeton.chatmanager.utils.stt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VoiceIndicator {
    @Getter
    private static State state = State.IDLE;
    private static final AtomicBoolean running = new AtomicBoolean(false);
    @Getter
    private static float level = 0;

    public static boolean start() {
        if (!running.compareAndSet(false, true)) return false;
        state = State.LOADING;
        running.set(true);
        level = 0;
        return true;
    }

    public static void error() {
        state = State.ERROR;
    }

    public static void listening() {
        state = State.LISTENING;
    }

    public static void finished() {
        if (state != State.ERROR) {
            state = State.IDLE;
            running.set(false);
            level = 0;
        }
    }

    public static float audio(byte[] buffer, int len) {
        int byteLen = len / 2;
        if (byteLen == 0) return -1;
        long sum = 0;
        for (int i = 0; i + 1 < len; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
            sum += sample * sample;
        }
        float rms = (float) Math.sqrt(sum / (double) byteLen) / 32768f;
        float v = Math.min(1f, rms * 8f);
        level = Math.max(v, level * 0.8f);
        return level;
    }

    public static <G> void render(G graphics, BiConsumer<G, Frame> renderer) {
        if (state == State.ERROR) state = State.IDLE;
        if (state == State.IDLE) return;
        renderer.accept(graphics, new Frame(state, level));
    }

    public enum State {IDLE, LOADING, LISTENING, ERROR}

    public record Frame(State state, float level) {
    }
}
