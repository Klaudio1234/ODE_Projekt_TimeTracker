package at.technikum.timetracker.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TimeManager {
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final List<TimeEntry> entries = new CopyOnWriteArrayList<>();

    public List<Task> getTasks() { return List.copyOf(tasks); }
    public List<TimeEntry> getEntries() { return List.copyOf(entries); }

    public Optional<Task> findTask(UUID id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public void addTask(Task t) {
        if (t == null) return;

        String newName = normalize(t.getName());
        String newType = normalize(t.getType());

        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Task name must not be empty");
        }
        if (newType.isEmpty()) {
            throw new IllegalArgumentException("Task type missing");
        }

        boolean duplicate = tasks.stream().anyMatch(existing ->
                normalize(existing.getType()).equals(newType) &&
                        normalize(existing.getName()).equals(newName)
        );

        if (duplicate) {
            throw new IllegalArgumentException("Task already exists: " + t.getName() + " (" + t.getType() + ")");
        }

        tasks.add(t);
    }

    public void updateTask(Task task, String newNameRaw, String newDescRaw) {
        if (task == null) return;

        String newName = normalize(newNameRaw);
        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Task name must not be empty");
        }

        String type = normalize(task.getType());

        boolean duplicate = tasks.stream().anyMatch(existing ->
                !existing.getId().equals(task.getId()) &&
                        normalize(existing.getType()).equals(type) &&
                        normalize(existing.getName()).equals(newName)
        );

        if (duplicate) {
            throw new IllegalArgumentException("Task already exists: " + newNameRaw + " (" + task.getType() + ")");
        }

        task.setName(newNameRaw == null ? "" : newNameRaw.trim());
        task.setDescription(newDescRaw == null ? "" : newDescRaw.trim());
    }

    public void deleteTask(Task task) {
        if (task == null) return;

        tasks.removeIf(t -> t.getId().equals(task.getId()));
        entries.removeIf(e -> e.getTaskId().equals(task.getId()));
    }


    public void addEntry(TimeEntry entry) {
        Objects.requireNonNull(entry);
        entries.add(entry);
    }

    public List<TimeEntry> getEntriesForTask(UUID taskId) {
        return entries.stream()
                .filter(e -> e.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(TimeEntry::getStart).reversed())
                .collect(Collectors.toList());
    }

    public long getTotalSecondsForTask(UUID taskId) {
        return getEntriesForTask(taskId).stream().mapToLong(TimeEntry::getDurationSeconds).sum();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
