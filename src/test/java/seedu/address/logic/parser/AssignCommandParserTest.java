package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AssignCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

public class AssignCommandParserTest {
    private final AssignCommandParser parser = new AssignCommandParser();

    @Test
    public void parse_repeatedIdenticalOrEmptyPrefixes_failure() {
        for (String duplicate : List.of("sn/A1111111H", "sn/", "m/CS2103", "m/", "c/F10", "c/")) {
            Prefix prefix = new Prefix(duplicate.substring(0, duplicate.indexOf('/') + 1));
            assertParseFailure(parser, " sn/A1111111H m/CS2103 c/F10 " + duplicate,
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
    }

    @Test
    public void parse_extraKnownButUnsupportedPrefixes_failure() {
        for (String prefix : List.of("n/", "e/", "tg/", "t/")) {
            assertParseFailure(parser, " sn/A1111111H m/CS2103 c/F10 " + prefix + "value",
                    String.format(AssignCommandParser.MESSAGE_UNKNOWN_PARAMETER, prefix));
        }
    }

    @Test
    public void parse_anyParameterOrderAndWhitespace_success() {
        AssignCommand expected = new AssignCommand(new StudentNumber("A1111111H"),
                new ModuleCode("CS2103"), new ClassName("F10"));
        for (String args : List.of(" sn/A1111111H m/CS2103 c/F10", " sn/A1111111H c/F10 m/CS2103",
                " m/CS2103 sn/A1111111H c/F10", " m/CS2103 c/F10 sn/A1111111H",
                " c/F10 sn/A1111111H m/CS2103", " c/F10 m/CS2103 sn/A1111111H",
                " sn/   A1111111H   m/   CS2103   c/   F10   ")) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_invalidIdentifiers_reportsValidationErrors() {
        for (String number : List.of("random-numbers", "1234", "A012 3456X")) {
            assertParseFailure(parser, " sn/" + number + " m/CS2103 c/F10", StudentNumber.MESSAGE_CONSTRAINTS);
        }
        for (String module : List.of("fake-module", "1234", "CS 2103")) {
            assertParseFailure(parser, " sn/A1111111H m/" + module + " c/F10", ModuleCode.MESSAGE_CONSTRAINTS);
        }
        for (String name : List.of("---", "F/10", "F 10")) {
            assertParseFailure(parser, " sn/A1111111H m/CS2103 c/" + name, ClassName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingOrEmptyParameter_failure() {
        for (String args : List.of("", " m/CS2103 c/F10", " sn/A1111111H c/F10",
                " sn/A1111111H m/CS2103", " sn/ m/CS2103 c/F10", " sn/A1111111H m/ c/F10",
                " sn/A1111111H m/CS2103 c/   ")) {
            assertParseFailure(parser, args, AssignCommandParser.MESSAGE_MISSING_PARAMETERS);
        }
    }

    @Test
    public void parse_unknownPrefixOrPreamble_failure() {
        assertParseFailure(parser, " sn/A1111111H m/CS2103 c/F10 x/value",
                String.format(AssignCommandParser.MESSAGE_UNKNOWN_PARAMETER, "x/"));
        assertParseFailure(parser, "unexpected sn/A1111111H m/CS2103 c/F10",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AssignCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        for (Prefix prefix : new Prefix[] {CliSyntax.PREFIX_STUDENT_NUMBER, CliSyntax.PREFIX_MODULE_CODE,
            CliSyntax.PREFIX_CLASS_NAME}) {
            assertParseFailure(parser, " sn/A1111111H m/CS2103 c/F10 " + prefix + "again",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
    }

    @Test
    public void parseCommand_assign_routesToParser() throws Exception {
        assertEquals(new AssignCommand(new StudentNumber("A1111111H"), new ModuleCode("CS2103"), new ClassName("F10")),
                new AddressBookParser().parseCommand("assign sn/A1111111H m/CS2103 c/F10"));
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("assign"));
    }
}
