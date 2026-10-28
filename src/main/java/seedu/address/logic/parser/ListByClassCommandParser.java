package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_THRESHOLD;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListByClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments for the listbyclass command.
 */
public class ListByClassCommandParser implements Parser<ListByClassCommand> {
    /**
     * Parses the displayed class index and optional attendance threshold.
     *
     * @throws ParseException if required values are missing or parameters are invalid.
     */
    @Override
    public ListByClassCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.rejectUnknownPrefixes(args, PREFIX_THRESHOLD);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_THRESHOLD);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_THRESHOLD);

        if (argMultimap.getValue(PREFIX_THRESHOLD).isEmpty()) {
            return new ListByClassCommand(index);
        }

        int threshold = ParserUtil.parsePositiveInteger(argMultimap.getValue(PREFIX_THRESHOLD).get());
        return new ListByClassCommand(index, Optional.of(threshold));
    }
}
