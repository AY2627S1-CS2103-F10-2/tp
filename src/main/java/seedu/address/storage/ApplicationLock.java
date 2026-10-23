package seedu.address.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/** Holds an operating-system lock for an application's working folder. */
public final class ApplicationLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private ApplicationLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /** Returns empty if another instance already owns the folder; I/O failures are propagated. */
    public static Optional<ApplicationLock> tryAcquire(Path applicationFolder) throws IOException {
        Files.createDirectories(applicationFolder);
        FileChannel channel = FileChannel.open(applicationFolder.resolve("tassist.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return Optional.of(new ApplicationLock(channel, lock));
            }
        } catch (OverlappingFileLockException e) {
            // Another instance in this JVM already owns the lock.
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
        channel.close();
        return Optional.empty();
    }

    /** Releases the lock without deleting the file, so other processes keep using the same file. */
    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            channel.close();
        }
    }
}
