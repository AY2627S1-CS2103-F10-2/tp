package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AssignCommand;
import seedu.address.logic.commands.Command;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Tests index validation and the displayed records targeted by the parsed command. */
public class AssignCommandParserTest {
    private final AssignCommandParser parser = new AssignCommandParser();

    @Test
    public void parse_validIndices_targetsDisplayedStudentAndClass() throws Exception {
        assertParsedCommandTargetsExpectedRecords(parser.parse(" 2 1"));
    }

    @Test
    public void parse_extraWhitespace_targetsDisplayedStudentAndClass() throws Exception {
        assertParsedCommandTargetsExpectedRecords(parser.parse("  2\t 1  "));
    }

    @Test
    public void parseCommand_assign_routesToAssignParser() throws Exception {
        assertParsedCommandTargetsExpectedRecords(new AddressBookParser().parseCommand("assign 2 1"));
    }

    @Test
    public void parse_missingOrExtraIndices_failure() {
        for (String input : new String[] {"", " ", "1", "1 2 3", "extra 1 2"}) {
            assertInvalidArguments(input);
        }
    }

    @Test
    public void parse_invalidIndices_failure() {
        for (String input : new String[] {"0 1", "1 0", "-1 1", "1 -1", "+1 1", "1 +1",
            "1.5 1", "1 1.5", "abc 1", "1 abc", "2147483648 1", "1 2147483648"}) {
            assertInvalidArguments(input);
        }
    }

    @Test
    public void parse_prefixedArguments_failure() {
        for (String input : new String[] {"sn/A1111111H m/CS2103 c/F10", "1 1 d/2024-02-29", "1 1 x/value"}) {
            assertInvalidArguments(input);
        }
    }

    private void assertInvalidArguments(String input) {
        assertParseFailure(parser, input,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AssignCommand.MESSAGE_USAGE));
    }

    private void assertParsedCommandTargetsExpectedRecords(Command command) throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        AcademicClass target = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        AcademicClass other = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F11"));
        model.addAcademicClass(target);
        model.addAcademicClass(other);
        model.assignStudentToClass(BENSON, target);
        model.assignStudentToClass(ALICE, other);

        assertEquals(String.format(AssignCommand.MESSAGE_SUCCESS, ALICE.getStudentNumber(), target),
                command.execute(model).getFeedbackToUser());
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
        assertEquals(Set.of(ALICE), other.getStudents());
    }
}
