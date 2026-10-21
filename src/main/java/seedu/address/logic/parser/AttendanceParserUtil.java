package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.logic.parser.exceptions.ParseException;

/** Shared argument structure validation for attendance commands. */
final class AttendanceParserUtil {
    static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s";
    private static final List<Prefix> REQUIRED_PREFIXES = List.of(
            PREFIX_STUDENT_NUMBER, PREFIX_MODULE_CODE, PREFIX_CLASS_NAME, PREFIX_DATE);
    private static final Set<String> VALID_PREFIXES = REQUIRED_PREFIXES.stream()
            .map(Prefix::getPrefix).collect(Collectors.toUnmodifiableSet());
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<=\\s)([^\\s/]+/)");

    private AttendanceParserUtil() {}

    /** Tokenizes arguments and rejects unknown, missing, repeated prefixes and unexpected preamble. */
    static ArgumentMultimap parseArguments(String args, String commandUsage) throws ParseException {
        requireNonNull(args);
        requireNonNull(commandUsage);
        rejectUnknownPrefixes(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, REQUIRED_PREFIXES.toArray(Prefix[]::new));
        validateStructure(arguments, commandUsage);
        return arguments;
    }

    private static void validateStructure(ArgumentMultimap arguments, String commandUsage) throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage));
        }
        for (Prefix prefix : REQUIRED_PREFIXES) {
            if (arguments.getValue(prefix).isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage));
            }
        }
        arguments.verifyNoDuplicatePrefixesFor(REQUIRED_PREFIXES.toArray(Prefix[]::new));
    }

    private static void rejectUnknownPrefixes(String args) throws ParseException {
        Matcher matcher = PREFIX_PATTERN.matcher(" " + args);
        while (matcher.find()) {
            if (!VALID_PREFIXES.contains(matcher.group(1))) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PARAMETER, matcher.group(1)));
            }
        }
    }
}
