package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteClassCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests class deletion through parsing, command execution, model updates, and JSON persistence.
 */
public class DeleteClassIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final ModelManager model = new ModelManager();
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
    }

    @Test
    public void execute_deleteClass_savesRemainingClassesAndPersons() throws Exception {
        model.addPerson(AMY);
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("addclass m/CS2103 c/F11-2");
        var view = logic.getAcademicClassList();

        CommandResult result = logic.execute("deleteclass 1");

        AcademicClass remaining = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F11-2"));
        assertEquals("Deleted class: CS2103-F10-2", result.getFeedbackToUser());
        assertEquals(List.of(remaining), view);
        ReadOnlyAddressBook savedAddressBook = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), savedAddressBook);
        assertEquals(List.of(AMY), savedAddressBook.getPersonList());

        logic.execute("deleteclass 1");
        assertEquals(List.of(), storage.readAddressBook().orElseThrow().getAcademicClassList());
    }

    @Test
    public void execute_deleteClassInvalidInput_preservesModelAndSavedData() throws Exception {
        logic.execute("addclass m/CS2103 c/F10-2");

        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DeleteClassCommand.MESSAGE_USAGE);
        assertCommandFailurePreservesData("deleteclass 1 x/extra", ParseException.class, usage);
        assertCommandFailurePreservesData("deleteclass 0", ParseException.class, usage);
        assertCommandFailurePreservesData("deleteclass 1 2", ParseException.class, usage);
        assertCommandFailurePreservesData("deleteclass 2", CommandException.class,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
    }

    @Test
    public void execute_deleteFilteredClass_savesDisplayedSelection() throws Exception {
        model.addPerson(AMY);
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("addclass m/CS2103 c/F11-2");
        logic.execute("assign sn/" + AMY.getStudentNumber() + " m/CS2103 c/F11-2");
        logic.execute("listbystudent 1");
        var displayed = logic.getFilteredAcademicClassList();
        AcademicClass hidden = model.getAcademicClassList().getFirst();
        assertEquals(1, displayed.size());

        assertCommandFailurePreservesData("deleteclass 2", CommandException.class,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        logic.execute("deleteclass 1");

        assertEquals(List.of(), displayed);
        assertEquals(List.of(hidden), model.getAcademicClassList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
    }

    private void assertCommandFailurePreservesData(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) throws DataLoadingException {
        ReadOnlyAddressBook savedAddressBook = storage.readAddressBook().orElseThrow();
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(savedAddressBook, model.getAddressBook());
        assertEquals(savedAddressBook, storage.readAddressBook().orElseThrow());
    }
}
