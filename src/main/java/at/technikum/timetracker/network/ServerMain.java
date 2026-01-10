package at.technikum.timetracker.network;

import at.technikum.timetracker.exception.NetworkException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ServerMain {

    public static void main(String[] args) {
        Server server = new Server(5555, System.out::println);

        try {
            server.startAsync();
        } catch (NetworkException e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        System.out.println("Type messages and press ENTER to broadcast to all clients.");
        System.out.println("Type 'exit' to stop the server.");

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = console.readLine()) != null) {
                String msg = line.trim();
                if (msg.isEmpty()) continue;

                if (msg.equalsIgnoreCase("exit")) break;

                server.broadcast("SERVER_CMD|" + msg);
            }
        } catch (Exception e) {
            System.out.println("Console stopped: " + e.getMessage());
        } finally {
            server.close();
        }
    }
}
