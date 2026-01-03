package at.technikum.timetracker.model;

import java.util.UUID;

public class AccountingTask extends Task {
    public AccountingTask(String name, String description) { super(name, description); }
    public AccountingTask(UUID id, String name, String description) { super(id, name, description); }

    @Override
    public String getType() { return "ACCOUNTING"; }
}
