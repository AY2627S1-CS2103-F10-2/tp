package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the student, class and date for removing attendance. */
public class DeleteAttendanceCommandParser implements Parser<DeleteAttendanceCommand> {
    public static final String MESSAGE_UNKNOWN_PARAMETER = AttendanceParserUtil.MESSAGE_UNKNOWN_PARAMETER;

    @Override
    public DeleteAttendanceCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = AttendanceParserUtil.parseArguments(args);
        Index[] indices = ParserUtil.parseIndices(arguments.getPreamble(), DeleteAttendanceCommand.MESSAGE_USAGE);
        LocalDate date = ParserUtil.parseAttendanceDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        return new DeleteAttendanceCommand(indices[0], indices[1], date);
    }

}
