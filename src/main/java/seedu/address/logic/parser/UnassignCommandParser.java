package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UnassignCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student and class indices for an unassign command.
 */
public class UnassignCommandParser implements Parser<UnassignCommand> {

    /**
     * Parses exactly two positive displayed indices and creates an unassign command.
     *
     * @throws ParseException if the arguments do not conform to the expected format
     */
    @Override
    public UnassignCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args);
        Index[] indices = ParserUtil.parseIndices(arguments.getPreamble(), UnassignCommand.MESSAGE_USAGE);

        return new UnassignCommand(indices[0], indices[1]);
    }
}
