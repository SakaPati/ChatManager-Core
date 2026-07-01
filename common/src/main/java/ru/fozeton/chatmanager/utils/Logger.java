package ru.fozeton.chatmanager.utils;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Logger {
    private static final List<LogEntry> buffer = Collections.synchronizedList(new ArrayList<>());
    private final String prefix;
    private final Path logsDir = Platform.getGameFolder().resolve("logs");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Logger(Class<?> clazz) {
        this.prefix = clazz.getSimpleName();
    }

    public void info(String msg) {
        addLog("INFO", msg);
    }

    public void debug(String msg) {
        addLog("DEBUG", msg);
    }

    public void warn(String msg) {
        addLog("WARN", msg);
    }

    public void error(String msg) {
        addLog("ERROR", msg);
    }

    private void addLog(String level, String msg) {
        buffer.add(new LogEntry(Instant.now().toEpochMilli(), prefix, level, msg));

        Path logFile = logsDir.resolve("chatManager.log");
        try {
            Files.createDirectories(logsDir);
            String timestamp = dateFormat.format(new Date());
            String threadName = Thread.currentThread().getName();
            String logMessage = String.format("[%s] [%s] [%s/%s]: %s", timestamp, prefix, threadName, level, msg);
            Files.writeString(logFile, logMessage + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record LogEntry(long timestamp, String prefix, String level, String msg) {
    }
}
