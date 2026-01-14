package at.technikum.timetracker.network;

import at.technikum.timetracker.exception.NetworkException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Server implements Closeable {
    private final int port;
    private final Consumer<String> onMessage;

    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running;

    private final CopyOnWriteArrayList<BufferedWriter> clientWriters = new CopyOnWriteArrayList<>();

    private final Path serverEntriesFile = Path.of("network-data").resolve("server-entries.txt");
    private final Object fileLock = new Object();

    public Server(int port, Consumer<String> onMessage) {
        this.port = port;
        this.onMessage = onMessage == null ? (s -> {}) : onMessage;
    }

    public void startAsync() throws NetworkException {
        try {
            serverSocket = new ServerSocket(port);
            running = true;

            Files.createDirectories(serverEntriesFile.getParent());

            onMessage.accept("Server listening on " + port);
            onMessage.accept("Server entries file: " + serverEntriesFile.toAbsolutePath());

            pool.submit(this::acceptLoop);
        } catch (IOException ex) {
            throw new NetworkException("Server start failed: " + ex.getMessage(), ex);
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket s = serverSocket.accept();
                pool.submit(() -> handleClient(s));
            } catch (IOException ex) {
                if (running) onMessage.accept("Server error: " + ex.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        String remote = socket.getRemoteSocketAddress().toString();
        onMessage.accept("Client connected: " + remote);

        BufferedWriter out = null;

        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            out = writer;

            clientWriters.add(out);
            sendToWriter(out, "WELCOME|Connected to server");

            String line;
            while ((line = in.readLine()) != null) {
                onMessage.accept("RX " + remote + ": " + line);

                if (isStateEvent(line)) {
                    if (line.startsWith("ENTRY|")) {
                        appendServerEntry(remote, line);
                    }
                    broadcast(line);
                } else {
                    sendToWriter(out, "OK|" + line);
                }
            }

        } catch (IOException ex) {
            onMessage.accept("Client disconnected (" + remote + "): " + ex.getMessage());
        } finally {
            if (out != null) {
                clientWriters.remove(out);
            }
        }
    }

    private boolean isStateEvent(String line) {
        if (line == null) return false;
        return line.startsWith("ENTRY|")
                || line.startsWith("TASK|")
                || line.startsWith("UPDATE_TASK|")
                || line.startsWith("DELETE_TASK|");
    }

    private void appendServerEntry(String remote, String payload) {
        String line = java.time.Instant.now().toString() + "|" + remote + "|" + payload;

        synchronized (fileLock) {
            try (BufferedWriter w = Files.newBufferedWriter(
                    serverEntriesFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            )) {
                w.write(line);
                w.newLine();
            } catch (IOException ex) {
                onMessage.accept("Server file write failed: " + ex.getMessage());
            }
        }
    }

    public void broadcast(String message) {
        if (message == null) return;

        int ok = 0;
        var toRemove = new java.util.ArrayList<BufferedWriter>();

        for (BufferedWriter w : clientWriters) {
            if (sendToWriter(w, message)) ok++;
            else toRemove.add(w);
        }

        clientWriters.removeAll(toRemove);
        onMessage.accept("TX broadcast: " + message + " (to " + ok + " clients)");
    }

    private boolean sendToWriter(BufferedWriter w, String message) {
        try {
            w.write(message);
            w.newLine();
            w.flush();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public void close() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        pool.shutdownNow();
        clientWriters.clear();
        onMessage.accept("Server stopped");
    }
}
