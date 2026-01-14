package at.technikum.timetracker.network;

import at.technikum.timetracker.exception.NetworkException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Client implements Closeable {

    private final ExecutorService sendPool = Executors.newSingleThreadExecutor();
    private final Consumer<String> onMessage;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private Thread listenerThread;

    public Client(Consumer<String> onMessage) {
        this.onMessage = onMessage == null ? (s -> {}) : onMessage;
    }

    public void connect(String host, int port) throws NetworkException {
        try {
            if (isConnected()) {
                onMessage.accept("Already connected to server.");
                return;
            }

            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            onMessage.accept("Connected to " + host + ":" + port);

            listenerThread = new Thread(this::listenLoop, "client-listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException ex) {
            throw new NetworkException("Connect failed: " + ex.getMessage(), ex);
        }
    }

    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                onMessage.accept(line);
            }
        } catch (IOException ex) {
            onMessage.accept("Connection closed: " + ex.getMessage());
        }
    }

    public void sendLineAsync(String line) {
        sendPool.submit(() -> {
            try {
                if (out == null) {
                    onMessage.accept("Send failed: Not connected (out is null)");
                    return;
                }
                out.write(line);
                out.newLine();
                out.flush();
                onMessage.accept("TX: " + line);
            } catch (IOException ex) {
                onMessage.accept("Send failed: " + ex.getMessage());
            }
        });
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        sendPool.shutdownNow();
    }
}
