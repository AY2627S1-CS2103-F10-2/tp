package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Contains integration tests (interaction with the Model) and unit tests for DeleteClassCommand.
 */
public class DeleteClassCommandTest {

    private static final AcademicClass CS2103_F10 =
            new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
    private static final AcademicClass CS2103_F11 =
            new AcademicClass(new ModuleCode("CS2103"), new ClassName("F11-2"));
    private static final AcademicClass CS2101_F10 =
            new AcademicClass(new ModuleCode("CS2101"), new ClassName("F10-2"));

    private final Model model = new ModelManager();

    @Test
    public void constructor_nullLookupValue_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteClassCommand(null, "F10-2"));
        assertThrows(NullPointerException.class, () -> new DeleteClassCommand("CS2103", null));
    }

    @Test
    public void constructor_emptyLookupValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteClassCommand("  ", "F10-2"));
        assertThrows(IllegalArgumentException.class, () -> new DeleteClassCommand("CS2103", "  "));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteClassCommand("CS2103", "F10-2").execute(null));
    }

    @Test
    public void execute_existingClass_deletesOnlyMatchingPair() {
        model.addAcademicClass(CS2103_F11);
        model.addAcademicClass(CS2103_F10);
        model.addAcademicClass(CS2101_F10);
        model.addPerson(ALICE);

        Model expectedModel = new ModelManager();
        expectedModel.addAcademicClass(CS2103_F11);
        expectedModel.addAcademicClass(CS2101_F10);
        expectedModel.addPerson(ALICE);

        assertCommandSuccess(new DeleteClassCommand("cs2103", "f10-2"), model,
                String.format(DeleteClassCommand.MESSAGE_DELETE_CLASS_SUCCESS, CS2103_F10), expectedModel);
    }

    @Test
    public void execute_filteredPersons_deletesClassWithoutChangingFilter() {
        model.addAcademicClass(CS2103_F10);
        model.addPerson(ALICE);
        model.updateFilteredPersonList(person -> false);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteAcademicClass(CS2103_F10);
        expectedModel.updateFilteredPersonList(person -> false);

        assertCommandSuccess(new DeleteClassCommand(" CS2103 ", " F10-2 "), model,
                String.format(DeleteClassCommand.MESSAGE_DELETE_CLASS_SUCCESS, CS2103_F10), expectedModel);
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_emptyClassList_throwsCommandException() {
        assertCommandFailure(new DeleteClassCommand("CS2103", "F10-2"), model,
                String.format(DeleteClassCommand.MESSAGE_CLASS_NOT_FOUND, "CS2103", "F10-2"));
    }

    @Test
    public void execute_missingClass_doesNotChangeModel() {
        model.addAcademicClass(CS2103_F10);
        model.addPerson(ALICE);

        assertCommandFailure(new DeleteClassCommand("CS2103", "F11-2"), model,
                String.format(DeleteClassCommand.MESSAGE_CLASS_NOT_FOUND, "CS2103", "F11-2"));
        assertCommandFailure(new DeleteClassCommand("CS2101", "F10-2"), model,
                String.format(DeleteClassCommand.MESSAGE_CLASS_NOT_FOUND, "CS2101", "F10-2"));
    }

    @Test
    public void execute_invalidCreationFormat_reportsClassNotFound() {
        model.addAcademicClass(CS2103_F10);

        assertCommandFailure(new DeleteClassCommand("2103", "---"), model,
                String.format(DeleteClassCommand.MESSAGE_CLASS_NOT_FOUND, "2103", "---"));
    }

    @Test
    public void execute_deleteTwice_secondCommandFails() throws Exception {
        model.addAcademicClass(CS2103_F10);
        DeleteClassCommand command = new DeleteClassCommand("CS2103", "F10-2");
        command.execute(model);

        assertTrue(model.getAcademicClassList().isEmpty());
        assertCommandFailure(command, model,
                String.format(DeleteClassCommand.MESSAGE_CLASS_NOT_FOUND, "CS2103", "F10-2"));
    }

    @Test
    public void equals() {
        DeleteClassCommand command = new DeleteClassCommand("CS2103", "F10-2");

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeleteClassCommand("CS2103", "F10-2")));
        assertTrue(command.equals(new DeleteClassCommand(" cs2103 ", " f10-2 ")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new DeleteClassCommand("CS2101", "F10-2")));
        assertFalse(command.equals(new DeleteClassCommand("CS2103", "F11-2")));
    }

    @Test
    public void toStringMethod() {
        DeleteClassCommand command = new DeleteClassCommand("CS2103", "F10-2");
        String expected = DeleteClassCommand.class.getCanonicalName() + "{moduleCode=CS2103, className=F10-2}";
        assertEquals(expected, command.toString());
    }
}
