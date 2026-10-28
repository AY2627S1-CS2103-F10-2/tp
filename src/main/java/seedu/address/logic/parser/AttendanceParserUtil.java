package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.util.List;

import seedu.address.logic.parser.exceptions.ParseException;

/** Shared argument structure validation for attendance commands. */
final class AttendanceParserUtil {
    static final String MESSAGE_UNKNOWN_PARAMETER = "Unknown parameter: %1$s. Supported parameters: d/.";
    private static final List<Prefix> REQUIRED_PREFIXES = List.of(PREFIX_DATE);

    private AttendanceParserUtil() {}

    /** Tokenizes arguments and validates the required prefixes. */
    static ArgumentMultimap parseArguments(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.rejectUnknownPrefixes(args, PREFIX_DATE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, REQUIRED_PREFIXES.toArray(Prefix[]::new));
        validateStructure(arguments);
        return arguments;
    }

    private static void validateStructure(ArgumentMultimap arguments) throws ParseException {
        for (Prefix prefix : REQUIRED_PREFIXES) {
            ParserUtil.requirePrefix(arguments, prefix, "YYYY-MM-DD");
        }
        arguments.verifyNoDuplicatePrefixesFor(REQUIRED_PREFIXES.toArray(Prefix[]::new));
    }

}
