package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments and creates a new {@link ListClassCommand}. */
public class ListClassCommandParser implements Parser<ListClassCommand> {

    /**
     * Parses the arguments in the context of the {@code ListClassCommand}.
     *
     * @throws ParseException if arguments are provided.
     */
    @Override
    public ListClassCommand parse(String args) throws ParseException {
        if (!args.trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ListClassCommand.MESSAGE_USAGE));
        }
        return new ListClassCommand();
    }
}
