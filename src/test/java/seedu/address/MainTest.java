package seedu.address;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.storage.ApplicationLock;

public class MainTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void launchWithLock_available_holdsLockDuringLaunchAndReleasesAfterwards() throws Exception {
        AtomicBoolean launched = new AtomicBoolean();
        Main.launchWithLock(temporaryFolder, () -> {
            launched.set(true);
            try {
                assertTrue(ApplicationLock.tryAcquire(temporaryFolder).isEmpty());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
        assertTrue(launched.get());
        try (ApplicationLock lock = ApplicationLock.tryAcquire(temporaryFolder).orElseThrow()) {
            assertTrue(Files.exists(temporaryFolder.resolve("tassist.lock")));
        }
    }

    @Test
    public void launchWithLock_alreadyOwned_printsErrorWithoutLaunching() throws Exception {
        AtomicBoolean launched = new AtomicBoolean();
        try (ApplicationLock lock = ApplicationLock.tryAcquire(temporaryFolder).orElseThrow()) {
            String error = captureError(() -> Main.launchWithLock(temporaryFolder, () -> launched.set(true)));
            assertTrue(error.contains("TAssist is already running"));
            assertFalse(launched.get());
            assertTrue(ApplicationLock.tryAcquire(temporaryFolder).isEmpty());
        }
    }

    @Test
    public void launchWithLock_invalidFolder_printsErrorWithoutLaunching() throws Exception {
        Path file = Files.createFile(temporaryFolder.resolve("file"));
        AtomicBoolean launched = new AtomicBoolean();
        String error = captureError(() -> Main.launchWithLock(file, () -> launched.set(true)));
        assertTrue(error.contains("Unable to access the TAssist application lock"));
        assertFalse(launched.get());
    }

    @Test
    public void launchWithLock_launchThrows_releasesLock() throws Exception {
        assertThrows(IllegalStateException.class, () -> Main.launchWithLock(temporaryFolder, () -> {
            throw new IllegalStateException("Launch failed");
        }));
        try (ApplicationLock lock = ApplicationLock.tryAcquire(temporaryFolder).orElseThrow()) {
            assertTrue(ApplicationLock.tryAcquire(temporaryFolder).isEmpty());
        }
    }

    private String captureError(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(output)) {
            System.setErr(stream);
            action.run();
        } finally {
            System.setErr(original);
        }
        return output.toString();
    }
}
