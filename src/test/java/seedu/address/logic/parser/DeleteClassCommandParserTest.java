package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteClassCommand;

public class DeleteClassCommandParserTest {

    private final DeleteClassCommandParser parser = new DeleteClassCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, "1", new DeleteClassCommand(Index.fromOneBased(1)));
        assertParseSuccess(parser, "  2  ", new DeleteClassCommand(Index.fromOneBased(2)));
    }

    @Test
    public void parse_invalidInput_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClassCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "   ", "0", "-1", "1.5", "abc", "2147483648", "1 2", "1 x/extra"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
