package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationPathsTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void getApplicationDirectory_packagedJar_usesJarFolder() throws Exception {
        Path jarFolder = Files.createDirectories(temporaryFolder.resolve("app with spaces #1"));
        Path jar = Files.createFile(jarFolder.resolve("TAssist.jar"));
        Path workingDirectory = Files.createDirectories(temporaryFolder.resolve("terminal"));
        assertEquals(jarFolder, ApplicationPaths.getApplicationDirectory(jar, workingDirectory));
    }

    @Test
    public void getApplicationDirectory_classesDirectory_usesWorkingDirectory() throws Exception {
        Path classes = Files.createDirectories(temporaryFolder.resolve("classes"));
        Path workingDirectory = Files.createDirectories(temporaryFolder.resolve("terminal"));
        assertEquals(workingDirectory, ApplicationPaths.getApplicationDirectory(classes, workingDirectory));
    }

    @Test
    public void getApplicationDirectory_relativeWorkingDirectory_returnsAbsolutePath() throws Exception {
        Path classes = Files.createDirectories(temporaryFolder.resolve("classes"));
        assertEquals(Path.of("").toAbsolutePath(),
                ApplicationPaths.getApplicationDirectory(classes, Path.of(".")));
    }

    @Test
    public void getApplicationDirectory_runtimeClasses_usesWorkingDirectory() throws Exception {
        assertEquals(Path.of("").toAbsolutePath(), ApplicationPaths.getApplicationDirectory());
    }

    @Test
    public void getApplicationDirectory_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                ApplicationPaths.getApplicationDirectory(null, temporaryFolder));
        assertThrows(NullPointerException.class, () ->
                ApplicationPaths.getApplicationDirectory(temporaryFolder, null));
    }
}
