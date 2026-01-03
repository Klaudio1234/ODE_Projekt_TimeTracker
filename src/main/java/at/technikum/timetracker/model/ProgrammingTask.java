package at.technikum.timetracker.model;

import java.util.UUID;

public class ProgrammingTask extends Task {

    public ProgrammingTask(String name, String description) { super(name, description); }

    public ProgrammingTask(UUID id, String name, String description) { super(id, name, description); }

    @Override
    public String getType() { return "PROGRAMMING"; }
}