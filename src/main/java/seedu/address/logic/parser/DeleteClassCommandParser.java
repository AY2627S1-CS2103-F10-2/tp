package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.DeleteClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Parses input arguments and creates a new DeleteClassCommand object.
 */
public class DeleteClassCommandParser implements Parser<DeleteClassCommand> {

    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    public static final String MESSAGE_DUPLICATE_PARAMETER = "Parameter %1$s was specified more than once.";

    private static final Set<String> VALID_PREFIXES = Set.of(PREFIX_MODULE_CODE.toString(),
            PREFIX_CLASS_NAME.toString());
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    /**
     * Parses the given arguments into a command that looks up a class by module code and class name.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public DeleteClassCommand parse(String args) throws ParseException {
        rejectUnknownPrefixes(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);

        if (!argMultimap.getPreamble().isEmpty()
                || argMultimap.getValue(PREFIX_MODULE_CODE).isEmpty()
                || argMultimap.getValue(PREFIX_CLASS_NAME).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClassCommand.MESSAGE_USAGE));
        }

        rejectDuplicatePrefix(argMultimap, PREFIX_MODULE_CODE);
        rejectDuplicatePrefix(argMultimap, PREFIX_CLASS_NAME);

        String moduleCode = argMultimap.getValue(PREFIX_MODULE_CODE).get().trim();
        String className = argMultimap.getValue(PREFIX_CLASS_NAME).get().trim();
        if (moduleCode.isEmpty()) {
            throw new ParseException(ModuleCode.MESSAGE_EMPTY);
        }
        if (className.isEmpty()) {
            throw new ParseException(ClassName.MESSAGE_EMPTY);
        }

        return new DeleteClassCommand(moduleCode, className);
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
