package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListByClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments for the listbyclass command.
 */
public class ListByClassCommandParser implements Parser<ListByClassCommand> {
    /**
     * Parses the displayed class index.
     *
     * @throws ParseException if required values are missing or parameters are invalid.
     */
    @Override
    public ListByClassCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ListByClassCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE), pe);
        }
    }
}
