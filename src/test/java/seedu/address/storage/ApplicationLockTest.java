package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationLockTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void tryAcquire_sameFolder_rejectsSecondInstanceUntilReleased() throws Exception {
        Path dataFolder = temporaryFolder.resolve("data");
        try (ApplicationLock first = ApplicationLock.tryAcquire(dataFolder).orElseThrow()) {
            assertTrue(ApplicationLock.tryAcquire(dataFolder).isEmpty());
            assertTrue(ApplicationLock.tryAcquire(dataFolder.resolve(".")).isEmpty());
        }
        try (ApplicationLock reopened = ApplicationLock.tryAcquire(dataFolder).orElseThrow()) {
            assertTrue(Files.exists(dataFolder.resolve("tassist.lock")));
        }
    }

    @Test
    public void tryAcquire_differentFolders_allowsBothInstances() throws Exception {
        try (ApplicationLock first = ApplicationLock.tryAcquire(temporaryFolder.resolve("first")).orElseThrow();
                ApplicationLock second = ApplicationLock.tryAcquire(temporaryFolder.resolve("second")).orElseThrow()) {
            assertTrue(Files.exists(temporaryFolder.resolve("first/tassist.lock")));
            assertTrue(Files.exists(temporaryFolder.resolve("second/tassist.lock")));
        }
    }

    @Test
    public void tryAcquire_existingLockFileWithoutOwner_acquiresLock() throws Exception {
        Files.createFile(temporaryFolder.resolve("tassist.lock"));
        try (ApplicationLock lock = ApplicationLock.tryAcquire(temporaryFolder).orElseThrow()) {
            assertTrue(ApplicationLock.tryAcquire(temporaryFolder).isEmpty());
        }
    }
}
