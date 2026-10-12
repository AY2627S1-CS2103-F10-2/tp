package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AssignCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/** Parses assignment arguments and validates their identifiers. */
public class AssignCommandParser implements Parser<AssignCommand> {
    public static final String MESSAGE_MISSING_PARAMETERS = "You're missing some parameters. The correct format is "
            + "assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    private static final Set<String> VALID_PREFIXES = Set.of("sn/", "m/", "c/");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    @Override
    public AssignCommand parse(String args) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(" " + args);
        while (matcher.find()) {
            if (!VALID_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, matcher.group(1)));
            }
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    AssignCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);
        for (Prefix prefix : new Prefix[] { PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME }) {
            if (arguments.getValue(prefix).isEmpty()) {
                throw new ParseException(MESSAGE_MISSING_PARAMETERS);
            }
        }
        StudentNumber studentNumber = ParserUtil.parseStudentNumber(
                arguments.getValue(PREFIX_STUDENT_NUMBER).orElseThrow());
        ModuleCode moduleCode = ParserUtil.parseModuleCode(arguments.getValue(PREFIX_MODULE_CODE).orElseThrow());
        ClassName className = ParserUtil.parseClassName(arguments.getValue(PREFIX_CLASS_NAME).orElseThrow());
        return new AssignCommand(studentNumber, moduleCode, className);
    }
}
