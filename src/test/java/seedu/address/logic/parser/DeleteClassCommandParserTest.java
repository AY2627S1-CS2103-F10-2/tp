package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DeleteClassCommandParser.MESSAGE_DUPLICATE_PARAMETER;
import static seedu.address.logic.parser.DeleteClassCommandParser.MESSAGE_UNKNOWN_PARAMETER;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteClassCommand;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class DeleteClassCommandParserTest {

    private final DeleteClassCommandParser parser = new DeleteClassCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        DeleteClassCommand expectedCommand = new DeleteClassCommand("CS2103", "F10-2");
        assertParseSuccess(parser, " m/CS2103 c/F10-2", expectedCommand);
        assertParseSuccess(parser, " c/f10-2 m/cs2103", expectedCommand);
        assertParseSuccess(parser, " m/   CS2103   c/   F10-2   ", expectedCommand);
    }

    @Test
    public void parse_missingParameter_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, " m/CS2103", expectedMessage);
        assertParseFailure(parser, " c/F10-2", expectedMessage);
    }

    @Test
    public void parse_emptyValue_failure() {
        assertParseFailure(parser, " m/ c/F10-2", ModuleCode.MESSAGE_EMPTY);
        assertParseFailure(parser, " c/F10-2 m/   ", ModuleCode.MESSAGE_EMPTY);
        assertParseFailure(parser, " m/CS2103 c/", ClassName.MESSAGE_EMPTY);
        assertParseFailure(parser, " c/   m/CS2103", ClassName.MESSAGE_EMPTY);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        assertParseFailure(parser, " m/CS2103 m/CS2101 c/F10-2",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_MODULE_CODE));
        assertParseFailure(parser, " m/CS2103 m/CS2103 c/F10-2",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_MODULE_CODE));
        assertParseFailure(parser, " m/CS2103 c/F10-2 c/F11-2",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_CLASS_NAME));
        assertParseFailure(parser, " m/CS2103 c/F10-2 c/F10-2",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_CLASS_NAME));
    }

    @Test
    public void parse_unknownParameter_failure() {
        String expectedMessage = String.format(MESSAGE_UNKNOWN_PARAMETER, "x/");
        assertParseFailure(parser, " x/unknown m/CS2103 c/F10-2", expectedMessage);
        assertParseFailure(parser, " m/CS2103 x/unknown c/F10-2", expectedMessage);
        assertParseFailure(parser, " m/CS2103 c/F10-2 x/unknown", expectedMessage);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " extra m/CS2103 c/F10-2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClassCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidCreationFormat_acceptsNonemptyLookupValues() {
        assertParseSuccess(parser, " m/2103 c/---", new DeleteClassCommand("2103", "---"));
        assertParseSuccess(parser, " m/CS 2103 c/T 24", new DeleteClassCommand("CS 2103", "T 24"));
        assertParseSuccess(parser, " m/CS-2103 c///", new DeleteClassCommand("CS-2103", "//"));
    }
}
