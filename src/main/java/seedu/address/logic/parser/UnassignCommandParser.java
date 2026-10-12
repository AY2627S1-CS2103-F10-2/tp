package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnassignCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/**
 * Parses input arguments and creates a new unassign command.
 */
public class UnassignCommandParser implements Parser<UnassignCommand> {

    public static final String MESSAGE_MISSING_PARAMETERS = "You're missing some parameters. The correct format is "
            + "unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    private static final Set<String> VALID_PREFIXES = Set.of("sn/", "m/", "c/");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    /**
     * Parses the given arguments and returns an unassign command for execution.
     *
     * @throws ParseException if the arguments do not conform to the expected format
     */
    @Override
    public UnassignCommand parse(String args) throws ParseException {
        rejectUnknownPrefixes(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT_NUMBER,
                PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    UnassignCommand.MESSAGE_USAGE));
        }
        if (argMultimap.getValue(PREFIX_MODULE_CODE).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_PARAMETERS);
        }
        if (argMultimap.getValue(PREFIX_CLASS_NAME).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_PARAMETERS);
        }
        if (argMultimap.getValue(PREFIX_STUDENT_NUMBER).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_PARAMETERS);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_NUMBER,
                PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);
        StudentNumber studentNumber = ParserUtil.parseStudentNumber(argMultimap.getValue(PREFIX_STUDENT_NUMBER).get());
        ModuleCode moduleCode = ParserUtil.parseModuleCode(argMultimap.getValue(PREFIX_MODULE_CODE).get());
        ClassName className = ParserUtil.parseClassName(argMultimap.getValue(PREFIX_CLASS_NAME).get());

        return new UnassignCommand(studentNumber, moduleCode, className);
    }

    private void rejectUnknownPrefixes(String args) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(" " + args);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            if (!VALID_PREFIXES.contains(prefix)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, prefix));
            }
        }
    }

}
