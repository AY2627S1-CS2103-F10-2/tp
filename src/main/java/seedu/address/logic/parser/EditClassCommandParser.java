package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditClassCommand;
import seedu.address.logic.commands.EditClassCommand.EditClassDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments and creates an edit-class command.
 */
public class EditClassCommandParser implements Parser<EditClassCommand> {

    /**
     * Parses the given arguments and returns an edit-class command for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public EditClassCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_CLASS_NAME, PREFIX_MODULE_CODE);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditClassCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_NAME, PREFIX_MODULE_CODE);

        EditClassDescriptor editClassDescriptor = new EditClassDescriptor();

        if (argMultimap.getValue(PREFIX_CLASS_NAME).isPresent()) {
            editClassDescriptor.setClassName(
                    ParserUtil.parseClassName(argMultimap.getValue(PREFIX_CLASS_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_MODULE_CODE).isPresent()) {
            editClassDescriptor.setModuleCode(
                    ParserUtil.parseModuleCode(argMultimap.getValue(PREFIX_MODULE_CODE).get()));
        }

        if (!editClassDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditClassCommand.MESSAGE_NOT_EDITED);
        }

        return new EditClassCommand(index, editClassDescriptor);
    }

}
