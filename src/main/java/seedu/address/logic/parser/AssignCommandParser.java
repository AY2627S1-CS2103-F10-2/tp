package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AssignCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student and class indices for an assign command.
 */
public class AssignCommandParser implements Parser<AssignCommand> {

    /**
     * Parses exactly two positive displayed indices and creates an assign command.
     *
     * @throws ParseException if the arguments do not conform to the expected format.
     */
    @Override
    public AssignCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args);
        Index[] indices = ParserUtil.parseIndices(arguments.getPreamble(), AssignCommand.MESSAGE_USAGE);

        return new AssignCommand(indices[0], indices[1]);
    }
}
