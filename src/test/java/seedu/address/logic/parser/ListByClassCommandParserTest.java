package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListByClassCommand;

public class ListByClassCommandParserTest {

    private final ListByClassCommandParser parser = new ListByClassCommandParser();

    @Test
    public void parse_validParameters_acceptsEitherOrderAndTrimsValues() {
        ListByClassCommand expected = new ListByClassCommand("CS2103", "F10-2");
        assertParseSuccess(parser, " m/CS2103 c/F10-2", expected);
        assertParseSuccess(parser, " c/F10-2 m/CS2103", expected);
        assertParseSuccess(parser, " m/  cs2103   c/  f10-2  ", expected);
    }

    @Test
    public void parse_missingOrEmptyParameters_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", message);
        assertParseFailure(parser, "   ", message);
        assertParseFailure(parser, " m/CS2103", message);
        assertParseFailure(parser, " c/F10-2", message);
        assertParseFailure(parser, " m/ c/F10-2", message);
        assertParseFailure(parser, " m/CS2103 c/   ", message);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " extra m/CS2103 c/F10-2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_repeatedParameters_failure() {
        assertParseFailure(parser, " m/CS2103 m/CS2103 c/F10-2",
                String.format(ListByClassCommandParser.MESSAGE_DUPLICATE_PARAMETER, "m/"));
        assertParseFailure(parser, " m/CS2103 c/F10-2 c/F11-2",
                String.format(ListByClassCommandParser.MESSAGE_DUPLICATE_PARAMETER, "c/"));
    }

    @Test
    public void parse_unknownParameters_failure() {
        String message = String.format(ListByClassCommandParser.MESSAGE_UNKNOWN_PARAMETER, "x/");
        assertParseFailure(parser, " x/value m/CS2103 c/F10-2", message);
        assertParseFailure(parser, " m/CS2103 x/value c/F10-2", message);
        assertParseFailure(parser, " m/CS2103 c/F10-2 x/value", message);
    }

    @Test
    public void parse_lookupValues_doesNotApplyCreationValidation() {
        assertParseSuccess(parser, " m/2103 c/---", new ListByClassCommand("2103", "---"));
        assertParseSuccess(parser, " m/CS 2103 c/T 24", new ListByClassCommand("CS 2103", "T 24"));
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
