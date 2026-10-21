package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;

import java.time.LocalDate;

import seedu.address.logic.commands.DeleteAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/** Parses the student, class and date for removing attendance. */
public class DeleteAttendanceCommandParser implements Parser<DeleteAttendanceCommand> {
    public static final String MESSAGE_UNKNOWN_PARAMETER = AttendanceParserUtil.MESSAGE_UNKNOWN_PARAMETER;

    @Override
    public DeleteAttendanceCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = AttendanceParserUtil.parseArguments(args, DeleteAttendanceCommand.MESSAGE_USAGE);
        StudentNumber studentNumber = ParserUtil.parseStudentNumber(
                arguments.getValue(PREFIX_STUDENT_NUMBER).orElseThrow());
        ModuleCode moduleCode = ParserUtil.parseModuleCode(arguments.getValue(PREFIX_MODULE_CODE).orElseThrow());
        ClassName className = ParserUtil.parseClassName(arguments.getValue(PREFIX_CLASS_NAME).orElseThrow());
        LocalDate date = ParserUtil.parseAttendanceDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        return new DeleteAttendanceCommand(studentNumber, moduleCode, className, date);
    }

}
