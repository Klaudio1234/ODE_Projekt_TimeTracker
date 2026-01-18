package at.technikum.timetracker.storage;

import at.technikum.timetracker.exception.StorageException;
import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeEntry;
import at.technikum.timetracker.model.TimeManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class FileStorage {

    private final Path dataFile;

    public FileStorage(Path dataFile) {
        this.dataFile = dataFile;
    }

    public static FileStorage defaultStorage() {
        Path dir = Path.of("client-data");
        return new FileStorage(dir.resolve("data.txt"));
    }

    public Path getDataFile() {
        return dataFile;
    }

    public void save(TimeManager manager) throws StorageException {
        try {
            Files.createDirectories(dataFile.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {

                for (Task t : manager.getTasks()) {
                    writer.write("TASK|" + t.getId()
                            + "|" + safe(t.getType())
                            + "|" + safe(t.getName())
                            + "|" + safe(t.getDescription()));
                    writer.newLine();
                }

                for (TimeEntry e : manager.getEntries()) {
                    Task t = findTask(manager, e.getTaskId());

                    String type = (t != null) ? t.getType() : "";
                    String name = (t != null) ? t.getName() : "";

                    writer.write("ENTRY|" + e.getId()
                            + "|" + e.getTaskId()
                            + "|" + e.getStart().toString()
                            + "|" + e.getEnd().toString()
                            + "|" + e.getDurationSeconds()
                            + "|" + safe(e.getUserName())
                            + "|" + safe(type)
                            + "|" + safe(name));
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            throw new StorageException("Save failed: " + ex.getMessage(), ex);
        }
    }


    public TimeManager load() throws StorageException {
        TimeManager manager = new TimeManager();

        if (!Files.exists(dataFile)) {
            return manager;
        }

        try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("TASK|")) {

                    String[] p = line.split("\\|", -1);

                    UUID id = UUID.fromString(p[1]);
                    String type = p.length >= 3 ? p[2] : "";
                    String name = p.length >= 4 ? p[3] : "";
                    String desc = p.length >= 5 ? p[4] : "";

                    manager.addTask(Task.fromStorage(id, type, name, desc));
                }

                else if (line.startsWith("ENTRY|")) {
                    String[] p = line.split("\\|", -1);

                    UUID entryId = UUID.fromString(p[1]);
                    UUID taskId = UUID.fromString(p[2]);

                    Instant start;
                    Instant end;
                    String user;

                    if (p.length >= 6 && p[3].contains("T") && p[4].contains("T")) {
                        start = Instant.parse(p[3]);
                        end = Instant.parse(p[4]);
                        user = p.length >= 7 ? p[6] : "";
                    } else {

                        long seconds = Long.parseLong(p[3]);
                        user = p.length >= 5 ? p[4] : "";
                        end = Instant.now();
                        start = end.minusSeconds(seconds);
                    }

                    manager.addEntry(new TimeEntry(entryId, taskId, start, end, user));
                }
            }
        } catch (Exception ex) {
            throw new StorageException("Load failed: " + ex.getMessage(), ex);
        }

        return manager;
    }

    private static Task findTask(TimeManager manager, UUID taskId) {
        for (Task t : manager.getTasks()) {
            if (t.getId().equals(taskId)) return t;
        }
        return null;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").trim();
    }
}
