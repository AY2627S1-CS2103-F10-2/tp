package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.AddClassCommandParser.MESSAGE_DUPLICATE_PARAMETER;
import static seedu.address.logic.parser.AddClassCommandParser.MESSAGE_MISSING_CLASS_NAME;
import static seedu.address.logic.parser.AddClassCommandParser.MESSAGE_MISSING_MODULE_CODE;
import static seedu.address.logic.parser.AddClassCommandParser.MESSAGE_UNKNOWN_PARAMETER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddClassCommand;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

public class AddClassCommandParserTest {
    private AddClassCommandParser parser = new AddClassCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        AcademicClass expectedAcademicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));

        // module code before class name
        assertParseSuccess(parser, " m/CS2103 c/F10-2", new AddClassCommand(expectedAcademicClass));

        // class name before module code
        assertParseSuccess(parser, " c/f10-2 m/cs2103", new AddClassCommand(expectedAcademicClass));

        // leading and trailing spaces accepted
        assertParseSuccess(parser, " m/   CS2103 c/   F10-2 ", new AddClassCommand(expectedAcademicClass));
    }

    @Test
    public void parse_repeatedValue_failure() {
        assertParseFailure(parser, " m/CS2103 m/CS2103T c/F10-2",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_MODULE_CODE));
        assertParseFailure(parser, " m/CS2103 c/F10-2 c/T24",
                String.format(MESSAGE_DUPLICATE_PARAMETER, PREFIX_CLASS_NAME));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        assertParseFailure(parser, " c/F10-2", MESSAGE_MISSING_MODULE_CODE);
        assertParseFailure(parser, " m/CS2103", MESSAGE_MISSING_CLASS_NAME);
    }

    @Test
    public void parse_invalidValue_failure() {
        // empty module code
        assertParseFailure(parser, " m/ c/F10-2", ModuleCode.MESSAGE_EMPTY);

        // invalid module codes
        assertParseFailure(parser, " m/2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS 2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS-2103 c/F10-2", ModuleCode.MESSAGE_CONSTRAINTS);

        // empty class name
        assertParseFailure(parser, " m/CS2103 c/", ClassName.MESSAGE_EMPTY);

        // invalid class names
        assertParseFailure(parser, " m/CS2103 c/T 24", ClassName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS2103 c///", ClassName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " m/CS2103 c/---", ClassName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownParameter_failure() {
        assertParseFailure(parser, " m/CS2103 x/unknown c/F10-2",
                String.format(MESSAGE_UNKNOWN_PARAMETER, "x/"));
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " extra m/CS2103 c/F10-2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddClassCommand.MESSAGE_USAGE));
    }
}
