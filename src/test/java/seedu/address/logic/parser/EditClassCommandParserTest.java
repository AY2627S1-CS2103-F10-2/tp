package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditClassCommand;
import seedu.address.logic.commands.EditClassCommand.EditClassDescriptor;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Tests index parsing, optional class edits, validation and command routing.
 */
public class EditClassCommandParserTest {
    private final EditClassCommandParser parser = new EditClassCommandParser();
    private final String invalidFormat = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
            EditClassCommand.MESSAGE_USAGE);

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_bothFieldsPresent_success() {
        assertParseSuccess(parser, "1 m/CS2103 c/OB5", expected("CS2103", "OB5"));
    }

    @Test
    public void parse_reorderedLowercaseFieldsWithWhitespace_success() {
        assertParseSuccess(parser, " 1 c/ ob5  m/ cs2103  ", expected("CS2103", "OB5"));
    }

    @Test
    public void parse_moduleOnly_success() {
        assertParseSuccess(parser, "1 m/CS2103", expected("CS2103", null));
    }

    @Test
    public void parse_classNameOnly_success() {
        assertParseSuccess(parser, "1 c/OB5", expected(null, "OB5"));
    }

    @Test
    public void parse_missingOrInvalidIndex_failureWithClassUsage() {
        for (String input : new String[] {"", "c/OB5", "0 c/OB5", "-1 c/OB5", "one c/OB5",
            "1.5 c/OB5", "2147483648 c/OB5", "1 extra c/OB5"}) {
            assertParseFailure(parser, input, invalidFormat);
        }
    }

    @Test
    public void parse_missingFields_failure() {
        assertParseFailure(parser, "1", EditClassCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "1   ", EditClassCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_emptyFields_failure() {
        assertParseFailure(parser, "1 m/", ModuleCode.MESSAGE_EMPTY);
        assertParseFailure(parser, "1 c/", ClassName.MESSAGE_EMPTY);
    }

    @Test
    public void parse_invalidFields_failure() {
        assertParseFailure(parser, "1 m/2103", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 m/CS-2103", ModuleCode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 c/---", ClassName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 c/OB 5", ClassName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateFields_failureEvenWithIdenticalValues() {
        assertParseFailure(parser, "1 m/CS2103 m/CS2103",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MODULE_CODE));
        assertParseFailure(parser, "1 c/OB5 c/OB6",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLASS_NAME));
        assertParseFailure(parser, "1 c/ c/OB5",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLASS_NAME));
    }

    @Test
    public void parse_unknownPrefixAfterValue_failure() {
        assertParseFailure(parser, "1 c/OB5 x/extra", ClassName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseCommand_editClass_routesToCorrectParser() throws Exception {
        assertEquals(expected("CS2103", "OB5"),
                new AddressBookParser().parseCommand("editclass 1 m/CS2103 c/OB5"));
    }

    private EditClassCommand expected(String moduleCode, String className) {
        EditClassDescriptor descriptor = new EditClassDescriptor();
        if (moduleCode != null) {
            descriptor.setModuleCode(new ModuleCode(moduleCode));
        }
        if (className != null) {
            descriptor.setClassName(new ClassName(className));
        }
        return new EditClassCommand(Index.fromOneBased(1), descriptor);
    }
}
