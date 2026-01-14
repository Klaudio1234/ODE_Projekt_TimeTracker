package at.technikum.timetracker.model;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class TimeEntry {

    private final UUID id;
    private final UUID taskId;
    private final Instant start;
    private final Instant end;


    private final String userName;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public TimeEntry(UUID id, UUID taskId, Instant start, Instant end, String userName) {
        this.id = Objects.requireNonNull(id);
        this.taskId = Objects.requireNonNull(taskId);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must be after start");
        }

        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name must not be empty");
        }

        this.userName = userName.trim();
    }


    public TimeEntry(UUID taskId, Instant start, Instant end, String userName) {
        this(UUID.randomUUID(), taskId, start, end, userName);
    }

    // GETTER
    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public String getUserName() {
        return userName;
    }

    // LOGIK
    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public long getDurationSeconds() {
        return getDuration().getSeconds();
    }

    // DISPLAY
    @Override
    public String toString() {
        long total = getDurationSeconds();
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;

        String who = userName.isBlank() ? "" : (userName + " | ");

        return String.format(
                "%s%s - %s | %02d:%02d:%02d",
                who,
                TIME_FMT.format(start),
                TIME_FMT.format(end),
                h, m, s
        );
    }
}
