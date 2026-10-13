package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListByClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Parses input arguments for the listbyclass command.
 */
public class ListByClassCommandParser implements Parser<ListByClassCommand> {
    public static final String MESSAGE_DUPLICATE_PARAMETER = "Parameter %1$s was specified more than once.";
    public static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";

    private static final Set<String> VALID_PREFIXES = Set.of(PREFIX_MODULE_CODE.toString(),
            PREFIX_CLASS_NAME.toString());
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    /**
     * Parses class lookup arguments using the shared module code and class name constraints.
     *
     * @throws ParseException if required values are missing or parameters are invalid.
     */
    @Override
    public ListByClassCommand parse(String args) throws ParseException {
        requireNonNull(args);
        rejectUnknownPrefixes(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME);
        if (!arguments.getPreamble().isEmpty()
                || arguments.getValue(PREFIX_MODULE_CODE).isEmpty()
                || arguments.getValue(PREFIX_CLASS_NAME).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListByClassCommand.MESSAGE_USAGE));
        }
        rejectDuplicatePrefix(arguments, PREFIX_MODULE_CODE);
        rejectDuplicatePrefix(arguments, PREFIX_CLASS_NAME);
        ModuleCode moduleCode = ParserUtil.parseModuleCode(arguments.getValue(PREFIX_MODULE_CODE).get());
        ClassName className = ParserUtil.parseClassName(arguments.getValue(PREFIX_CLASS_NAME).get());
        return new ListByClassCommand(moduleCode, className);
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

    private void rejectDuplicatePrefix(ArgumentMultimap arguments, Prefix prefix) throws ParseException {
        if (arguments.getAllValues(prefix).size() > 1) {
            throw new ParseException(String.format(MESSAGE_DUPLICATE_PARAMETER, prefix));
        }
    }
}
