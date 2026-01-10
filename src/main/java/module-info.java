module at.technikum.timetracker {
    requires javafx.controls;
    requires java.logging;

    exports at.technikum.timetracker.ui;
    exports at.technikum.timetracker.model;
    exports at.technikum.timetracker.network;
    exports at.technikum.timetracker.storage;
    exports at.technikum.timetracker.exception;
}
