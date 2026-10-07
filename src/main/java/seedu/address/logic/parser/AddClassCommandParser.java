package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Parses input arguments and creates a new AddClassCommand object.
 */
public class AddClassCommandParser implements Parser<AddClassCommand> {

    public static final String MESSAGE_MISSING_MODULE_CODE = "Missing required parameter: m/MODULE_CODE";
    public static final String MESSAGE_MISSING_CLASS_NAME = "Missing required parameter: c/CLASS_NAME";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    public static final String MESSAGE_DUPLICATE_PARAMETER = "Parameter %1$s was specified more than once.";

    private static final Set<String> VALID_PREFIXES = Set.of(PREFIX_MODULE_CODE.toString(),
            PREFIX_CLASS_NAME.toString());
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    /**
     * Parses the given {@code String} of arguments in the context of the AddClassCommand
     * and returns an AddClassCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddClassCommand parse(String args) throws ParseException {
        rejectUnknownPrefixes(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    AddClassCommand.MESSAGE_USAGE));
        }
        if (argMultimap.getValue(PREFIX_MODULE_CODE).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_MODULE_CODE);
        }
        if (argMultimap.getValue(PREFIX_CLASS_NAME).isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_CLASS_NAME);
        }

        rejectDuplicatePrefix(argMultimap, PREFIX_MODULE_CODE);
        rejectDuplicatePrefix(argMultimap, PREFIX_CLASS_NAME);
        ModuleCode moduleCode = ParserUtil.parseModuleCode(argMultimap.getValue(PREFIX_MODULE_CODE).get());
        ClassName className = ParserUtil.parseClassName(argMultimap.getValue(PREFIX_CLASS_NAME).get());
        AcademicClass academicClass = new AcademicClass(moduleCode, className);

        return new AddClassCommand(academicClass);
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

    private void rejectDuplicatePrefix(ArgumentMultimap argMultimap, Prefix prefix) throws ParseException {
        if (argMultimap.getAllValues(prefix).size() > 1) {
            throw new ParseException(String.format(MESSAGE_DUPLICATE_PARAMETER, prefix));
        }
    }
}
