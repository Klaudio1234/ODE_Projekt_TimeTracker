package at.technikum.timetracker.model;

import java.util.UUID;

public class DesignTask extends Task {
    public DesignTask(String name, String description) { super(name, description); }
    public DesignTask(UUID id, String name, String description) { super(id, name, description); }

    @Override
    public String getType() { return "DESIGN"; }
}
