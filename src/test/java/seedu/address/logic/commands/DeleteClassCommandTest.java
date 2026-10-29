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

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
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
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteClassCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DeleteClassCommand command = new DeleteClassCommand(Index.fromOneBased(1));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_validIndex_deletesOnlySelectedClass() {
        model.addAcademicClass(CS2103_F11);
        model.addAcademicClass(CS2103_F10);
        model.addAcademicClass(CS2101_F10);
        model.addPerson(ALICE);

        Model expectedModel = new ModelManager();
        expectedModel.addAcademicClass(CS2103_F11);
        expectedModel.addAcademicClass(CS2101_F10);
        expectedModel.addPerson(ALICE);

        assertCommandSuccess(new DeleteClassCommand(Index.fromOneBased(2)), model,
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

        assertCommandSuccess(new DeleteClassCommand(Index.fromOneBased(1)), model,
                String.format(DeleteClassCommand.MESSAGE_DELETE_CLASS_SUCCESS, CS2103_F10), expectedModel);
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_emptyClassList_throwsCommandException() {
        assertCommandFailure(new DeleteClassCommand(Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndex_doesNotChangeModel() {
        model.addAcademicClass(CS2103_F10);
        model.addPerson(ALICE);

        assertCommandFailure(new DeleteClassCommand(Index.fromOneBased(2)), model,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
    }

    @Test
    public void execute_deleteTwice_secondCommandFails() throws Exception {
        model.addAcademicClass(CS2103_F10);
        DeleteClassCommand command = new DeleteClassCommand(Index.fromOneBased(1));
        command.execute(model);

        assertTrue(model.getAcademicClassList().isEmpty());
        assertCommandFailure(command, model,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredClasses_usesDisplayedIndex() throws Exception {
        model.addAcademicClass(CS2103_F10);
        model.addAcademicClass(CS2103_F11);
        model.addAcademicClass(CS2101_F10);
        model.updateFilteredAcademicClassList(academicClass -> !academicClass.equals(CS2103_F10));
        var displayed = model.getFilteredAcademicClassList();

        assertCommandFailure(new DeleteClassCommand(Index.fromOneBased(3)), model,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        new DeleteClassCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(CS2103_F10, CS2101_F10), model.getAcademicClassList());
        assertEquals(List.of(CS2101_F10), displayed);
        new DeleteClassCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(CS2103_F10), model.getAcademicClassList());
        assertTrue(displayed.isEmpty());
        assertCommandFailure(new DeleteClassCommand(Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
    }

    @Test
    public void execute_lastIndex_success() throws Exception {
        model.addAcademicClass(CS2103_F10);
        model.addAcademicClass(CS2103_F11);
        new DeleteClassCommand(Index.fromOneBased(2)).execute(model);
        assertEquals(List.of(CS2103_F10), model.getAcademicClassList());
    }

    @Test
    public void equals() {
        DeleteClassCommand command = new DeleteClassCommand(Index.fromOneBased(1));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeleteClassCommand(Index.fromOneBased(1))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertFalse(command.equals(new DeleteClassCommand(Index.fromOneBased(2))));
    }

    @Test
    public void toStringMethod() {
        DeleteClassCommand command = new DeleteClassCommand(Index.fromOneBased(1));
        String expected = DeleteClassCommand.class.getCanonicalName() + "{targetIndex=" + Index.fromOneBased(1) + "}";
        assertEquals(expected, command.toString());
    }
}
