package ru.fozeton.chatmanager.module.gif;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.exceptions.GifException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class McAnimCodec {
    public static Animation read(Path path) throws Exception {
        byte[] fileBytes = Files.readAllBytes(path);
        ByteBuffer buffer = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN);

        byte[] magic = new byte[6];
        buffer.get(magic);
        if (!new String(magic).equals("MCANIM")) throw new GifException("The file is not valid .mcanim!");

        int major = buffer.get() & 0xFF;
        int minor = buffer.get() & 0xFF;
        int patch = buffer.get() & 0xFF;
        int dataOffset = buffer.getInt();
        int frameCount = buffer.getInt();
        int width = buffer.getInt();
        int height = buffer.getInt();

        buffer.position(dataOffset);

        List<Frame> frames = new ArrayList<>(frameCount);
        int uncompressedFrameSize = width * height * 4;

        for (int i = 0; i < frameCount; i++) {
            int timestamp = buffer.getInt();
            int compressedLength = buffer.getInt();

            byte[] compressedData = new byte[compressedLength];
            buffer.get(compressedData);

            byte[] rgbaData = decompressZlib(compressedData, uncompressedFrameSize);
            frames.add(new Frame(timestamp, rgbaData));
        }

        return new Animation(major, minor, patch, width, height, frames);
    }

    private static byte[] decompressZlib(byte[] compressedData, int expectedSize) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        byte[] result = new byte[expectedSize];
        inflater.inflate(result);
        inflater.end();

        return result;
    }

    public record Frame(int timestamp, byte[] rgbaData) {
    }

    @Getter
    @RequiredArgsConstructor
    public static class Animation {
        @Getter(AccessLevel.NONE)
        private final int major;

        @Getter(AccessLevel.NONE)
        private final int minor;

        @Getter(AccessLevel.NONE)
        private final int patch;

        private final int width;
        private final int height;

        private final List<Frame> frames;

        public String getVersion() {
            return String.format("%s.%s.%s", major, minor, patch);
        }
    }
}