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
import seedu.address.testutil.ApplicationClassLoader;

public class MainTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void main_packagedJarAlreadyLocked_reportsDuplicateWithoutLaunching() throws Exception {
        Path jar = Files.createFile(temporaryFolder.resolve("TAssist.jar"));
        ClassLoader loader = new ApplicationClassLoader(jar.toUri().toURL(),
                "seedu.address.Main", "seedu.address.commons.util.ApplicationPaths");
        Class<?> main = loader.loadClass("seedu.address.Main");
        try (ApplicationLock lock = ApplicationLock.tryAcquire(temporaryFolder).orElseThrow()) {
            String error = captureError(() -> invokeMain(main));
            assertTrue(error.contains("TAssist is already running"));
            assertTrue(ApplicationLock.tryAcquire(temporaryFolder).isEmpty());
        }
    }

    @Test
    public void main_invalidApplicationLocation_reportsErrorWithoutLaunching() throws Exception {
        ClassLoader loader = new ApplicationClassLoader(ApplicationClassLoader.invalidLocation(),
                "seedu.address.Main", "seedu.address.commons.util.ApplicationPaths");
        Class<?> main = loader.loadClass("seedu.address.Main");
        String error = captureError(() -> invokeMain(main));
        assertTrue(error.contains("Unable to locate the TAssist application folder"));
        assertFalse(error.contains("Unable to access the TAssist application lock"));
    }

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

    private void invokeMain(Class<?> main) {
        try {
            main.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Application entry point failed", e);
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
