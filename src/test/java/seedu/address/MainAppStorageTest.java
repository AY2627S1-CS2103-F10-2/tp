package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;

public class MainAppStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void init_commandAndShutdown_persistThroughConfiguredStorage() throws Exception {
        MainApp app = new MainApp() {
            @Override
            void initModelAndStorage(Path applicationFolder) {
                assertEquals(Path.of("").toAbsolutePath(), applicationFolder);
                super.initModelAndStorage(temporaryFolder);
            }
        };
        app.init();
        app.logic.execute("add sn/A0999602X n/Storage Probe e/storage@example.com");
        app.stop();

        MainApp reopened = new MainApp();
        reopened.initModelAndStorage(temporaryFolder);
        assertTrue(reopened.model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getStudentNumber().value.equals("A0999602X")));
        assertTrue(Files.exists(temporaryFolder.resolve("preferences.json")));
    }

    @Test
    public void initModelAndStorage_applicationFolder_savesAndReloadsInThatFolder() throws Exception {
        Path applicationFolder = Files.createDirectories(temporaryFolder.resolve("app with spaces #1"));
        MainApp app = new MainApp();
        app.initModelAndStorage(applicationFolder);
        assertEquals(applicationFolder.resolve("preferences.json"), app.storage.getUserPrefsFilePath());
        assertEquals(applicationFolder.resolve("data/addressbook.json"), app.storage.getAddressBookFilePath());

        AddressBook addressBook = getTypicalAddressBook();
        app.storage.saveAddressBook(addressBook);
        UserPrefs preferences = new UserPrefs();
        preferences.setGuiSettings(new GuiSettings(900, 700, 50, 60));
        app.storage.saveUserPrefs(preferences);

        MainApp reopened = new MainApp();
        reopened.initModelAndStorage(applicationFolder);
        assertEquals(addressBook, reopened.model.getAddressBook());
        assertEquals(preferences, reopened.storage.readUserPrefs().orElseThrow());
        assertTrue(Files.exists(applicationFolder.resolve("data/addressbook.json")));
    }

    @Test
    public void initModelAndStorage_separateFolders_keepsDataSeparate() throws Exception {
        Path firstFolder = Files.createDirectories(temporaryFolder.resolve("first"));
        Path secondFolder = Files.createDirectories(temporaryFolder.resolve("second"));
        MainApp first = new MainApp();
        first.initModelAndStorage(firstFolder);
        first.storage.saveAddressBook(getTypicalAddressBook());

        MainApp second = new MainApp();
        second.initModelAndStorage(secondFolder);
        assertTrue(second.storage.readAddressBook().isEmpty());
        second.storage.saveAddressBook(new AddressBook());
        assertEquals(getTypicalAddressBook(), first.storage.readAddressBook().orElseThrow());
        assertEquals(new AddressBook(), second.storage.readAddressBook().orElseThrow());
    }

    @Test
    public void initModelAndStorage_blockedDataDirectory_saveFailsWithoutFallback() throws Exception {
        Path applicationFolder = Files.createDirectories(temporaryFolder.resolve("blocked"));
        Files.createFile(applicationFolder.resolve("data"));
        MainApp app = new MainApp();
        app.initModelAndStorage(applicationFolder);
        assertThrows(IOException.class, () -> app.storage.saveAddressBook(getTypicalAddressBook()));
        assertEquals(applicationFolder.resolve("data/addressbook.json"), app.storage.getAddressBookFilePath());
        assertFalse(Files.exists(applicationFolder.resolve("addressbook.json")));
    }
}
