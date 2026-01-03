package at.technikum.timetracker.model;

import java.util.Objects;
import java.util.UUID;

public abstract class Task {

    private final UUID id;
    private String name;
    private String description;

    protected Task(UUID id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        setName(name);
        setDescription(description);
    }

    protected Task(String name, String description) {
        this(UUID.randomUUID(), name, description);
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Task name is required");
        this.name = name.trim();
    }

    public String getDescription() { return description; }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public static Task fromStorage(UUID id, String type, String name, String desc) {
        if (type == null) throw new IllegalArgumentException("Task type missing");

        switch (type) {
            case "PROGRAMMING":
                return new ProgrammingTask(id, name, desc);
            case "DESIGN":
                return new DesignTask(id, name, desc);
            case "ACCOUNTING":
                return new AccountingTask(id, name, desc);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public abstract String getType();


    @Override
    public String toString() {
        return name + " (" + getType() + ")";
    }
}
