package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListByClassCommand;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class ListByClassCommandParserTest {

    private final ListByClassCommandParser parser = new ListByClassCommandParser();

    @Test
    public void parse_validParameters_acceptsEitherOrderAndTrimsValues() {
        ListByClassCommand expected = new ListByClassCommand(new ModuleCode("CS2103"), new ClassName("F10-2"));
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
        assertParseFailure(parser, " m/ c/F10-2", ModuleCode.MESSAGE_EMPTY);
        assertParseFailure(parser, " m/CS2103 c/   ", ClassName.MESSAGE_EMPTY);
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
    public void parse_invalidModuleCode_failure() {
        assertParseFailure(parser, " m/2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS 2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/\u00df2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidClassName_failure() {
        assertParseFailure(parser, " m/CS2103 c/---", ClassName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS2103 c/T 24", ClassName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS2103 c/\u00df1", ClassName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
