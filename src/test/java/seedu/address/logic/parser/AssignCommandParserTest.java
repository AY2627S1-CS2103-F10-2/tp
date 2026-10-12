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

public class AssignCommandParserTest {
    private final AssignCommandParser parser = new AssignCommandParser();

    @Test
    public void parse_anyParameterOrderAndWhitespace_success() {
        AssignCommand expected = new AssignCommand("A1111111H", "CS2103", "F10");
        for (String args : List.of(" sn/A1111111H m/CS2103 c/F10", " sn/A1111111H c/F10 m/CS2103",
                " m/CS2103 sn/A1111111H c/F10", " m/CS2103 c/F10 sn/A1111111H",
                " c/F10 sn/A1111111H m/CS2103", " c/F10 m/CS2103 sn/A1111111H",
                " sn/   A1111111H   m/   CS2103   c/   F10   ")) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_invalidIdentifiers_acceptsSearchText() {
        assertParseSuccess(parser, " sn/random-numbers m/fake-module c/fake",
                new AssignCommand("random-numbers", "fake-module", "fake"));
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
        assertEquals(new AssignCommand("A1111111H", "CS2103", "F10"),
                new AddressBookParser().parseCommand("assign sn/A1111111H m/CS2103 c/F10"));
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("assign"));
    }
}
