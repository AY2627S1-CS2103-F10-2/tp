package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListByStudentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a displayed student index for the {@code listbystudent} command. */
public class ListByStudentCommandParser implements Parser<ListByStudentCommand> {

    private static final Logger logger = LogsCenter.getLogger(ListByStudentCommandParser.class);

    @Override
    public ListByStudentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ListByStudentCommand(index);
        } catch (ParseException pe) {
            logger.fine("Rejected listbystudent arguments: expected one positive integer");
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ListByStudentCommand.MESSAGE_USAGE), pe);
        }
    }
}
