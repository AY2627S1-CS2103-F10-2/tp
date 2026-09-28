package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private static final String REMARK = "Likes baseball";

    private final Model model = new ModelManager();

    @Test
    public void execute_throwsCommandExceptionWithArguments() {
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK));
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ARGUMENTS,
                INDEX_FIRST_PERSON.getOneBased(), REMARK);

        assertCommandFailure(remarkCommand, model, expectedMessage);
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
