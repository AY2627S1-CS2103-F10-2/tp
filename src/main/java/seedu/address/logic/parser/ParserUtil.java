package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.Telegram;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_POSITIVE_INTEGER = "Value must be a positive integer.";
    public static final String MESSAGE_NAME_CONSTRAINTS =
            "Names must contain at least one letter (A-Z, a-z) or digit (0-9). Spaces are allowed.\n"
            + "Allowed punctuation: hyphen (-), straight apostrophe ('), period (.), comma (,), "
            + "parentheses ( ), and backslash (\\).\n"
            + "Enter \\ instead of /; it is stored and displayed as /. "
            + "Non-ASCII characters and other punctuation are not allowed.";
    public static final String MESSAGE_INVALID_ATTENDANCE_DATE =
            "Attendance date must be a valid date in YYYY-MM-DD format with a year from 0001 to 9999.";

    /**
     * Parses an attendance date, trimming outer whitespace without normalizing invalid dates.
     * Future-date eligibility is checked by the model at execution time.
     *
     * @throws ParseException if the date is not a valid calendar date in YYYY-MM-DD format, years 0001 to 9999.
     */
    public static LocalDate parseAttendanceDate(String date) throws ParseException {
        requireNonNull(date);
        String trimmedDate = date.trim();
        if (!trimmedDate.matches("(?!0000)[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(MESSAGE_INVALID_ATTENDANCE_DATE);
        }
        try {
            return LocalDate.parse(trimmedDate);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_ATTENDANCE_DATE, e);
        }
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses exactly two displayed indices in input order.
     *
     * @throws ParseException if the input does not contain exactly two positive integer indices.
     */
    public static Index[] parseIndices(String preamble, String commandUsage) throws ParseException {
        requireNonNull(preamble);
        requireNonNull(commandUsage);
        String[] indices = preamble.trim().split("\\s+");
        if (indices.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage));
        }
        try {
            return new Index[] {parseIndex(indices[0]), parseIndex(indices[1])};
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage), e);
        }
    }

    /** Parses a positive integer. */
    public static int parsePositiveInteger(String value) throws ParseException {
        String trimmed = value.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmed)) {
            throw new ParseException(MESSAGE_INVALID_POSITIVE_INTEGER);
        }
        return Integer.parseInt(trimmed);
    }


    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     * Backslashes in the extracted field represent forward slashes in the stored name.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        if (name.contains("/")) {
            throw new ParseException(MESSAGE_NAME_CONSTRAINTS);
        }
        String trimmedName = name.trim().replace('\\', '/');
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(MESSAGE_NAME_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String studentNumber} into a {@code StudentNumber}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code studentNumber} is empty or invalid.
     */
    public static StudentNumber parseStudentNumber(String studentNumber) throws ParseException {
        requireNonNull(studentNumber);
        String trimmedStudentNumber = studentNumber.trim();
        if (trimmedStudentNumber.isEmpty()) {
            throw new ParseException(StudentNumber.MESSAGE_EMPTY);
        }
        if (!StudentNumber.isValidStudentNumber(trimmedStudentNumber)) {
            throw new ParseException(StudentNumber.MESSAGE_CONSTRAINTS);
        }
        return new StudentNumber(trimmedStudentNumber);
    }

    /**
     * Parses a {@code String telegram} into a {@code Telegram}.
     * Leading and trailing whitespaces will be trimmed. A leading '@' is accepted and removed.
     *
     * @throws ParseException if the given {@code telegram} is invalid.
     */
    public static Telegram parseTelegram(String telegram) throws ParseException {
        requireNonNull(telegram);
        String trimmedTelegram = telegram.trim();
        if (!Telegram.isValidTelegram(trimmedTelegram)) {
            throw new ParseException(Telegram.MESSAGE_CONSTRAINTS);
        }
        return new Telegram(trimmedTelegram);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a {@code String moduleCode} into a {@code ModuleCode}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code moduleCode} is invalid.
     */
    public static ModuleCode parseModuleCode(String moduleCode) throws ParseException {
        requireNonNull(moduleCode);
        String trimmedModuleCode = moduleCode.trim();
        if (trimmedModuleCode.isEmpty()) {
            throw new ParseException(ModuleCode.MESSAGE_EMPTY);
        }
        if (!ModuleCode.isValidModuleCode(trimmedModuleCode)) {
            throw new ParseException(ModuleCode.MESSAGE_CONSTRAINTS);
        }
        return new ModuleCode(trimmedModuleCode);
    }

    /**
     * Parses a {@code String className} into a {@code ClassName}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code className} is invalid.
     */
    public static ClassName parseClassName(String className) throws ParseException {
        requireNonNull(className);
        String trimmedClassName = className.trim();
        if (trimmedClassName.isEmpty()) {
            throw new ParseException(ClassName.MESSAGE_EMPTY);
        }
        if (!ClassName.isValidClassName(trimmedClassName)) {
            throw new ParseException(ClassName.MESSAGE_CONSTRAINTS);
        }
        return new ClassName(trimmedClassName);
    }
}
