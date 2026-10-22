package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListByStudentCommand;

/** Verifies that only a single positive displayed index is accepted. */
public class ListByStudentCommandParserTest {

    private final ListByStudentCommandParser parser = new ListByStudentCommandParser();

    @Test
    public void parse_nullArguments_rejectedAsProgrammingError() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, "1", new ListByStudentCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ListByStudentCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingInvalidOrExtraArguments_rejected() {
        for (String args : new String[] { "", " ", "0", "-1", "1.5", "abc", "2147483648", "1 2", "1 sn/A12345" }) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByStudentCommand.MESSAGE_USAGE));
        }
    }
}
