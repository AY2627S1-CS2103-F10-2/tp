package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {
    @TempDir
    public Path temporaryFolder;

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
