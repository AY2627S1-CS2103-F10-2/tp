package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.ApplicationClassLoader;

public class LogsCenterTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void getLogger_invalidApplicationLocation_retainsConsoleLogging() throws Exception {
        Logger base = Logger.getLogger("ab3");
        Handler[] originalHandlers = base.getHandlers();
        Level originalLevel = base.getLevel();
        boolean originalUseParentHandlers = base.getUseParentHandlers();
        PrintStream originalError = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream error = new PrintStream(output)) {
            System.setErr(error);
            ClassLoader loader = new ApplicationClassLoader(ApplicationClassLoader.invalidLocation(),
                    "seedu.address.commons.core.LogsCenter", "seedu.address.commons.util.ApplicationPaths");
            Class<?> logsCenter = loader.loadClass("seedu.address.commons.core.LogsCenter");
            Logger logger = (Logger) logsCenter.getMethod("getLogger", String.class).invoke(null, "startup-test");
            assertEquals(1, base.getHandlers().length);
            assertTrue(base.getHandlers()[0] instanceof ConsoleHandler);
            logger.warning("Console remains usable");
            assertTrue(output.toString().contains("Unable to locate the application folder for logging"));
            assertTrue(output.toString().contains("Console remains usable"));
        } finally {
            System.setErr(originalError);
            for (Handler handler : base.getHandlers()) {
                base.removeHandler(handler);
                handler.close();
            }
            for (Handler handler : originalHandlers) {
                base.addHandler(handler);
            }
            base.setLevel(originalLevel);
            base.setUseParentHandlers(originalUseParentHandlers);
        }
    }

    @Test
    public void addFileHandler_specialCharacters_writesInLiteralFolder() throws Exception {
        Path folder = Files.createDirectories(temporaryFolder.resolve("app %t %h %g %u #1"));
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        LogsCenter.addFileHandler(logger, folder.resolve("addressbook.log"));
        try {
            assertEquals(1, logger.getHandlers().length);
            logger.info("Saved beside the JAR");
            logger.getHandlers()[0].flush();
            assertTrue(Files.readString(folder.resolve("addressbook.log.0")).contains("Saved beside the JAR"));
        } finally {
            for (Handler handler : logger.getHandlers()) {
                handler.close();
                logger.removeHandler(handler);
            }
        }
    }

    @Test
    public void addFileHandler_invalidFolder_keepsLoggerUsableWithoutFileHandler() throws Exception {
        Path file = Files.createFile(temporaryFolder.resolve("not-a-directory"));
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        LogsCenter.addFileHandler(logger, file.resolve("addressbook.log"));
        assertEquals(0, logger.getHandlers().length);
        logger.info("Still usable");
    }
}
