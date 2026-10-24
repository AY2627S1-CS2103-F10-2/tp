package seedu.address;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.application.Application;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ApplicationPaths;
import seedu.address.storage.ApplicationLock;

/**
 * The main entry point to the application.
 *
 * This is a workaround for the following error when MainApp is made the
 * entry point of the application:
 *
 *     Error: JavaFX runtime components are missing, and are required to run this application
 *
 * The reason is that MainApp extends Application. In that case, the
 * LauncherHelper will check for the javafx.graphics module to be present
 * as a named module. We don't use JavaFX via the module system so it can't
 * find the javafx.graphics module, and so the launch is aborted.
 *
 * By having a separate main class (Main) that doesn't extend Application
 * to be the entry point of the application, we avoid this issue.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Path lockDirectory = ApplicationPaths.getApplicationDirectory();
            launchWithLock(lockDirectory, () -> Application.launch(MainApp.class, args));
        } catch (URISyntaxException e) {
            System.err.println("Unable to locate the TAssist application folder: " + e.getMessage());
        }
    }

    /** Runs the application only while it owns the application folder's lock. */
    static void launchWithLock(Path applicationFolder, Runnable launchApplication) {
        try (ApplicationLock lock = ApplicationLock.tryAcquire(applicationFolder).orElse(null)) {
            if (lock == null) {
                System.err.println("Unable to open TAssist: TAssist is already running from this application folder. "
                        + "Close the other instance first.");
                return;
            }
            Logger logger = LogsCenter.getLogger(Main.class);
            logger.warning("The warnings about a 'restricted method in java.lang.System' "
                + "and 'enabling native access' appearing below (if any) can be ignored.");
            launchApplication.run();
        } catch (IOException e) {
            System.err.println("Unable to access the TAssist application lock: " + e.getMessage());
        }
    }
}
