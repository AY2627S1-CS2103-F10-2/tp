package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_THRESHOLD;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_POSITIVE_INTEGER;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ListByClassCommand;

public class ListByClassCommandParserTest {

    private final ListByClassCommandParser parser = new ListByClassCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, " 1 ", new ListByClassCommand(Index.fromOneBased(1)));
    }

    @Test
    public void parse_validIndexAndThreshold_returnsCommand() {
        assertParseSuccess(parser, " 1 t/1",
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(1)));
        assertParseSuccess(parser, " 2   t/5  ",
                new ListByClassCommand(Index.fromOneBased(2), Optional.of(5)));
        assertParseSuccess(parser, " 1 t/2147483647",
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingOrEmptyParameters_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", message);
        assertParseFailure(parser, "   ", message);
        assertParseFailure(parser, " t/5", message);
        assertParseFailure(parser, " m/CS2103 c/F10-2",
                "Unknown parameter: m/. Supported parameters: t/.");
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " extra 1", message);
        assertParseFailure(parser, " 0", message);
        assertParseFailure(parser, " -1", message);
        assertParseFailure(parser, " abc", message);
        assertParseFailure(parser, " 1 2", message);
        assertParseFailure(parser, " 1 m/CS2103",
                "Unknown parameter: m/. Supported parameters: t/.");
        assertParseFailure(parser, " 0 t/5", message);
    }

    @Test
    public void parse_invalidThreshold_failure() {
        assertParseFailure(parser, " 1 t/", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/   ", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/0", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/-1", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/+1", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/abc", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/1.5", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/2147483648", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/5 extra", MESSAGE_INVALID_POSITIVE_INTEGER);
        assertParseFailure(parser, " 1 t/5 m/CS2103",
                "Unknown parameter: m/. Supported parameters: t/.");
    }

    @Test
    public void parse_duplicateThresholdPrefix_failure() {
        assertParseFailure(parser, " 1 t/3 t/5",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_THRESHOLD));
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

