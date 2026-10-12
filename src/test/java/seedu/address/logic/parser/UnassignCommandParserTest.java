package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UnassignCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;

/**
 * Tests unassign argument validation and the identifiers passed to the command.
 */
public class UnassignCommandParserTest {
    private static final String VALID_ARGUMENTS = " sn/" + ALICE.getStudentNumber() + " m/CS2103 c/F10";
    private final UnassignCommandParser parser = new UnassignCommandParser();

    @Test
    public void parse_validArguments_executesWithExpectedIdentities() throws Exception {
        assertParsedCommandTargetsExpectedRecords(parser.parse(VALID_ARGUMENTS));
    }

    @Test
    public void parse_reorderedLowercaseArguments_executesWithNormalizedIdentities() throws Exception {
        assertParsedCommandTargetsExpectedRecords(parser.parse(
                " c/f10 m/cs2103 sn/" + ALICE.getStudentNumber().value.toLowerCase(java.util.Locale.ROOT)));
    }

    @Test
    public void parseCommand_unassign_routesToUnassignParser() throws Exception {
        assertParsedCommandTargetsExpectedRecords(new AddressBookParser().parseCommand(
                UnassignCommand.COMMAND_WORD + VALID_ARGUMENTS));
    }

    @Test
    public void parse_missingPrefixes_failure() {
        assertParseFailure(parser, "", UnassignCommandParser.MESSAGE_MISSING_PARAMETERS);
        assertParseFailure(parser, " m/CS2103 c/F10", UnassignCommandParser.MESSAGE_MISSING_PARAMETERS);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " c/F10",
                UnassignCommandParser.MESSAGE_MISSING_PARAMETERS);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " m/CS2103",
                UnassignCommandParser.MESSAGE_MISSING_PARAMETERS);
    }

    @Test
    public void parse_emptyValues_failure() {
        assertParseFailure(parser, " sn/ m/CS2103 c/F10", StudentNumber.MESSAGE_EMPTY);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " m/ c/F10", ModuleCode.MESSAGE_EMPTY);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " m/CS2103 c/", ClassName.MESSAGE_EMPTY);
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " sn/A12 m/CS2103 c/F10", StudentNumber.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " m/2103 c/F10",
                ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " sn/" + ALICE.getStudentNumber() + " m/CS2103 c/---",
                ClassName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, VALID_ARGUMENTS + " sn/" + ALICE.getStudentNumber(),
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STUDENT_NUMBER));
        assertParseFailure(parser, VALID_ARGUMENTS + " m/CS2103",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MODULE_CODE));
        assertParseFailure(parser, VALID_ARGUMENTS + " c/F10",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLASS_NAME));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, VALID_ARGUMENTS + " x/unknown",
                String.format(UnassignCommandParser.MESSAGE_UNKNOWN_PARAMETER, "x/"));
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " extra" + VALID_ARGUMENTS,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, UnassignCommand.MESSAGE_USAGE));
    }

    private void assertParsedCommandTargetsExpectedRecords(Command command) throws Exception {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        ModelStubRecordingUnassignment model = new ModelStubRecordingUnassignment();
        model.addPerson(ALICE);
        model.addAcademicClass(academicClass);

        CommandResult result = command.execute(model);

        assertSame(ALICE, model.unassignedPerson);
        assertSame(academicClass, model.unassignedClass);
        assertEquals(new CommandResult(String.format(UnassignCommand.MESSAGE_SUCCESS,
                ALICE.getStudentNumber(), academicClass)), result);
    }

    /**
     * Records which stored objects the parsed command passes to the model.
     */
    private static class ModelStubRecordingUnassignment extends ModelManager {
        private Person unassignedPerson;
        private AcademicClass unassignedClass;

        @Override
        public boolean unassignStudentFromClass(Person person, AcademicClass academicClass) {
            unassignedPerson = person;
            unassignedClass = academicClass;
            return true;
        }
    }
}
