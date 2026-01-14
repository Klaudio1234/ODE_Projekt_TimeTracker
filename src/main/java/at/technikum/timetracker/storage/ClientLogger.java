package at.technikum.timetracker.storage;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientLogger implements Closeable {

    private final Path logFile;
    private final ExecutorService writer = Executors.newSingleThreadExecutor();

    public ClientLogger(Path logFile) {
        this.logFile = logFile;
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException ignored) {}
    }

    public static ClientLogger defaultLogger() {
        Path dir = Path.of("client-data");
        return new ClientLogger(dir.resolve("client.log"));
    }

    public Path getLogFile() {
        return logFile;
    }

    public void logAsync(String message) {
        if (message == null) return;

        if (writer == null || writer.isShutdown() || writer.isTerminated()) return;

        String line = Instant.now().toString() + " | " + message;

        try {
            writer.submit(() -> {
                try (BufferedWriter w = Files.newBufferedWriter(
                        logFile,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND
                )) {
                    w.write(line);
                    w.newLine();
                } catch (IOException ignored) {
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {

        }
    }

    @Override
    public void close() {
        if (writer != null) writer.shutdownNow();
    }
}
