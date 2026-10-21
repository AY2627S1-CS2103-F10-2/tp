package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/** Tests attendance syntax through the production command router. */
public class AddAttendanceCommandParserTest {
    private static final String IDENTIFIERS = " sn/A0123456X m/CS2103 c/F10-2";
    private static final String VALID_COMMAND = "addattendance" + IDENTIFIERS + " d/2024-02-29";
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddAttendanceCommandParser().parse(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAttendanceDate(null));
    }

    @Test
    public void parseCommand_validDate_routesToAttendanceCommand() throws Exception {
        assertEquals(expected(LocalDate.of(2024, 2, 29)), parser.parseCommand(VALID_COMMAND));
    }

    @Test
    public void parseCommand_reorderedPrefixesAndWhitespace_normalizesIdentifiers() throws Exception {
        assertEquals(expected(LocalDate.of(2024, 2, 29)), parser.parseCommand(
                "  addattendance d/ 2024-02-29  c/ f10-2  sn/ a0123456x  m/ cs2103  "));
    }

    @Test
    public void parseCommand_futureDate_defersEligibilityToExecution() throws Exception {
        assertEquals(expected(LocalDate.of(9999, 12, 31)),
                parser.parseCommand("addattendance" + IDENTIFIERS + " d/9999-12-31"));
    }

    @Test
    public void parseCommand_missingPrefix_rejectsInput() {
        for (String parameter : List.of("sn/A0123456X", "m/CS2103", "c/F10-2", "d/2024-02-29")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND.replace(parameter, "")));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("addattendance"));
    }

    @Test
    public void parseCommand_emptyValue_rejectsInput() {
        for (String value : List.of("A0123456X", "CS2103", "F10-2", "2024-02-29")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND.replace(value, "")));
        }
    }

    @Test
    public void parseCommand_repeatedPrefix_rejectsEvenIdenticalOrEmptyValues() {
        for (String parameter : List.of("sn/A0123456X", "m/CS2103", "c/F10-2", "d/2024-02-29",
                "sn/", "m/", "c/", "d/")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND + " " + parameter));
        }
    }

    @Test
    public void parseCommand_unknownPrefix_rejectsBeforeOrAfterValidArguments() {
        for (String parameter : List.of("x/value", "t/friend", "D/2024-02-29")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND + " " + parameter));
            assertThrows(ParseException.class, () -> parser.parseCommand("addattendance " + parameter
                    + IDENTIFIERS + " d/2024-02-29"));
        }
    }

    @Test
    public void parseCommand_extraText_rejectsInput() {
        assertThrows(ParseException.class, () -> parser.parseCommand("addattendance 1"
                + IDENTIFIERS + " d/2024-02-29"));
        assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND + " extra"));
    }

    @Test
    public void parseCommand_invalidIdentifiers_reusesSharedValidation() {
        assertThrows(ParseException.class, StudentNumber.MESSAGE_CONSTRAINTS, () ->
                parser.parseCommand(VALID_COMMAND.replace("A0123456X", "bad!")));
        assertThrows(ParseException.class, ModuleCode.MESSAGE_CONSTRAINTS, () ->
                parser.parseCommand(VALID_COMMAND.replace("CS2103", "2103")));
        assertThrows(ParseException.class, ClassName.MESSAGE_CONSTRAINTS, () ->
                parser.parseCommand(VALID_COMMAND.replace("F10-2", "---")));
    }

    @Test
    public void parseCommand_invalidDate_rejectsWithoutNormalizing() {
        for (String date : List.of("2023-02-29", "1900-02-29", "2024-02-30", "2024-04-31", "2024-13-01",
                "2024-00-01", "2024-01-00", "2024-2-29", "24-02-29", "20240229", "29-02-2024",
                "+2024-02-29", "10000-01-01", "2024-02-29T12:00:00", "today", "999999999999999999")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_ATTENDANCE_DATE, () ->
                    parser.parseCommand("addattendance" + IDENTIFIERS + " d/" + date));
        }
    }

    @Test
    public void parseCommand_centuryLeapDay_acceptsValidDate() throws Exception {
        assertEquals(expected(LocalDate.of(2000, 2, 29)),
                parser.parseCommand("addattendance" + IDENTIFIERS + " d/2000-02-29"));
    }

    private AddAttendanceCommand expected(LocalDate date) {
        return new AddAttendanceCommand(new StudentNumber("A0123456X"), new ModuleCode("CS2103"),
                new ClassName("F10-2"), date);
    }
}
