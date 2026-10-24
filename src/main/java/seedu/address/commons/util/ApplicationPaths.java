package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the application folder shared by locking and saved files.
 */
public class ApplicationPaths {
    /**
     * Returns the JAR's folder for packaged runs, or the working directory for IDE/Gradle runs.
     *
     * @throws URISyntaxException if the application's code location cannot be converted to a URI.
     */
    public static Path getApplicationDirectory() throws URISyntaxException {
        Path codeLocation = Paths.get(ApplicationPaths.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        return getApplicationDirectory(codeLocation, Paths.get("."));
    }

    /** Returns an absolute application folder for the given code location and working directory. */
    static Path getApplicationDirectory(Path codeLocation, Path workingDirectory) {
        requireNonNull(codeLocation);
        requireNonNull(workingDirectory);
        Path directory = Files.isRegularFile(codeLocation)
                ? codeLocation.toAbsolutePath().getParent() : workingDirectory;
        return directory.toAbsolutePath().normalize();
    }
}
