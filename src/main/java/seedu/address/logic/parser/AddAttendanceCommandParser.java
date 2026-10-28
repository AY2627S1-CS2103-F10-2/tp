package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the student, class and date for recording attendance. */
public class AddAttendanceCommandParser implements Parser<AddAttendanceCommand> {
    public static final String MESSAGE_UNKNOWN_PARAMETER = AttendanceParserUtil.MESSAGE_UNKNOWN_PARAMETER;

    @Override
    public AddAttendanceCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = AttendanceParserUtil.parseArguments(args);
        Index[] indices = ParserUtil.parseIndices(arguments.getPreamble(), AddAttendanceCommand.MESSAGE_USAGE);
        LocalDate date = ParserUtil.parseAttendanceDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        return new AddAttendanceCommand(indices[0], indices[1], date);
    }

}
