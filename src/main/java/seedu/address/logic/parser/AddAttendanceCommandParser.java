package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/** Parses the student, class and date for recording attendance. */
public class AddAttendanceCommandParser implements Parser<AddAttendanceCommand> {
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    private static final Set<String> VALID_PREFIXES = Set.of("sn/", "m/", "c/", "d/");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    @Override
    public AddAttendanceCommand parse(String args) throws ParseException {
        requireNonNull(args);
        rejectUnknownPrefixes(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME, PREFIX_DATE);
        validateStructure(arguments);
        StudentNumber studentNumber = ParserUtil.parseStudentNumber(
                arguments.getValue(PREFIX_STUDENT_NUMBER).orElseThrow());
        ModuleCode moduleCode = ParserUtil.parseModuleCode(arguments.getValue(PREFIX_MODULE_CODE).orElseThrow());
        ClassName className = ParserUtil.parseClassName(arguments.getValue(PREFIX_CLASS_NAME).orElseThrow());
        LocalDate date = ParserUtil.parseAttendanceDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        return new AddAttendanceCommand(studentNumber, moduleCode, className, date);
    }

    private void validateStructure(ArgumentMultimap arguments) throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAttendanceCommand.MESSAGE_USAGE));
        }
        Prefix[] requiredPrefixes = { PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME, PREFIX_DATE };
        for (Prefix prefix : requiredPrefixes) {
            if (arguments.getValue(prefix).isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        AddAttendanceCommand.MESSAGE_USAGE));
            }
        }
        arguments.verifyNoDuplicatePrefixesFor(
                PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME, PREFIX_DATE);
    }

    private void rejectUnknownPrefixes(String args) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(" " + args);
        while (matcher.find()) {
            if (!VALID_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, matcher.group(1)));
            }
        }
    }
}
