package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    private static final String REMARK = "Likes baseball";

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_updatesRemark() {
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK));
        Person editedPerson = new PersonBuilder(ALICE).withRemark(REMARK).build();
        String expectedMessage = String.format("Added remark to Person: %1$s", Messages.format(editedPerson));
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setPerson(ALICE, editedPerson);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyRemark_removesRemark() {
        Person personWithRemark = new PersonBuilder(ALICE).withRemark(REMARK).build();
        model.setPerson(ALICE, personWithRemark);
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        String expectedMessage = String.format("Removed remark from Person: %1$s", Messages.format(ALICE));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithRemark, ALICE);

        assertCommandSuccess(remarkCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_SECOND_PERSON, new Remark(REMARK));
        Model emptyModel = new ModelManager();

        assertCommandFailure(remarkCommand, emptyModel, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand firstRemarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK));
        RemarkCommand secondRemarkCommand = new RemarkCommand(INDEX_SECOND_PERSON, new Remark(REMARK));

        assertTrue(firstRemarkCommand.equals(firstRemarkCommand));
        assertTrue(firstRemarkCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK))));
        assertFalse(firstRemarkCommand.equals(
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming"))));
        assertFalse(firstRemarkCommand.equals(secondRemarkCommand));
        assertFalse(firstRemarkCommand.equals(1));
        assertFalse(firstRemarkCommand.equals(null));
    }
}
