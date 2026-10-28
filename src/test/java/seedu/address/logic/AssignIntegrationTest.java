package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AssignIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_assignAndRestart_preservesMembershipAndStudentReferences() throws Exception {
        Path file = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ModelManager model = new ModelManager();
        LogicManager logic = createLogic(model, storage);
        logic.execute("add sn/A1111111H n/Alice Tan e/alice@example.com");
        logic.execute("addclass m/CS2103 c/F-10");
        logic.execute("find Alice");
        String expected = "Student (A1111111H) has been assigned to CS2103-F-10.";
        assertEquals(expected, logic.execute("assign  1  1 ").getFeedbackToUser());
        String duplicate = "Student (A1111111H) is already assigned to class CS2103-F-10.";
        assertThrows(CommandException.class, duplicate, ()
                -> logic.execute("assign 1 1"));
        assertEquals(1, model.getFilteredPersonList().size());

        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        AcademicClass restoredClass = restarted.getAcademicClassList().get(0);
        assertEquals(1, restoredClass.getStudents().size());
        assertSame(restarted.getAddressBook().getPersonList().get(0), restoredClass.getStudents().iterator().next());
        assertThrows(CommandException.class, duplicate, ()
                -> createLogic(restarted, storage).execute("assign 1 1"));
        assertEquals(1, restoredClass.getStudents().size());
    }

    @Test
    public void execute_rejectedAssignments_preserveMembershipAndSavedFile() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        ModelManager model = new ModelManager();
        AcademicClass academicClass = populateModel(model);
        LogicManager logic = createLogic(model, storage);
        logic.execute("assign 1 1");
        String saved = Files.readString(storage.getAddressBookFilePath());

        for (String input : List.of("assign 3 1", "assign 2 2", "assign 999 999")) {
            assertThrows(CommandException.class, () -> logic.execute(input));
            assertEquals(Set.of(ALICE), academicClass.getStudents());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        for (String input : List.of("assign", "assign 1", "assign 0 1", "assign 1 -1",
                "assign 1 1 extra", "assign sn/A0123456X m/CS2103T c/F10-2")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(Set.of(ALICE), academicClass.getStudents());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_saveFailures_reportErrorAndAllowRetry() throws Exception {
        for (IOException failure : List.of(new IOException("save failed"), new AccessDeniedException("denied"))) {
            ModelManager model = new ModelManager();
            populateModel(model);
            Path file = temporaryFolder.resolve("failure.json");
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(file) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw failure;
                }
            };
            LogicManager logic = createLogic(model, failingStorage);
            String message = String.format(failure instanceof AccessDeniedException
                    ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT,
                    failure.getMessage());
            String command = "assign 1 1";
            assertThrows(CommandException.class, message, () -> logic.execute(command));
            // LogicManager rolls back the in-memory change when saving fails.
            assertEquals(Set.of(), model.getAddressBook().getAcademicClassList().get(0).getStudents());
            JsonAddressBookStorage workingStorage = new JsonAddressBookStorage(file);
            createLogic(model, workingStorage).execute(command);
            assertEquals(Set.of(ALICE), model.getAddressBook().getAcademicClassList().get(0).getStudents());
            assertEquals(Set.of(ALICE), workingStorage.readAddressBook().orElseThrow()
                    .getAcademicClassList().get(0).getStudents());
        }
    }

    private LogicManager createLogic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private AcademicClass populateModel(ModelManager model) {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        model.addAcademicClass(academicClass);
        return academicClass;
    }
}
