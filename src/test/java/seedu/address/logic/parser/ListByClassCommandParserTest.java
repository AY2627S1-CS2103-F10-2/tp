package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListByClassCommand;

public class ListByClassCommandParserTest {

    private final ListByClassCommandParser parser = new ListByClassCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, " 1 ", new ListByClassCommand(Index.fromOneBased(1)));
    }

    @Test
    public void parse_missingOrEmptyParameters_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", message);
        assertParseFailure(parser, "   ", message);
        assertParseFailure(parser, " m/CS2103 c/F10-2", message);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " extra 1", message);
        assertParseFailure(parser, " 0", message);
        assertParseFailure(parser, " abc", message);
        assertParseFailure(parser, " 1 2", message);
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
