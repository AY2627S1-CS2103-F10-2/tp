package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.StudentNumber;

/** Tests attendance removal syntax through the command router. */
public class DeleteAttendanceCommandParserTest {
    private static final String IDENTIFIERS = " sn/A0123456X m/CS2103 c/F10-2";
    private static final String VALID_COMMAND = "deleteattendance" + IDENTIFIERS + " d/2024-02-29";
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteAttendanceCommandParser().parse(null));
    }

    @Test
    public void parseCommand_validInput_routesToDeletion() throws Exception {
        assertEquals(expected(LocalDate.of(2024, 2, 29)), parser.parseCommand(VALID_COMMAND));
    }

    @Test
    public void parseCommand_reorderedWhitespaceAndCase_normalizesValues() throws Exception {
        assertEquals(expected(LocalDate.of(2024, 2, 29)), parser.parseCommand(
                "  deleteattendance d/ 2024-02-29  c/ f10-2  sn/ a0123456x  m/ cs2103  "));
    }

    @Test
    public void parseCommand_futureDate_defersEligibilityToModel() throws Exception {
        assertEquals(expected(LocalDate.of(9999, 12, 31)),
                parser.parseCommand(VALID_COMMAND.replace("2024-02-29", "9999-12-31")));
    }

    @Test
    public void parseCommand_missingPrefix_rejectsInput() {
        for (String parameter : List.of("sn/A0123456X", "m/CS2103", "c/F10-2", "d/2024-02-29")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND.replace(parameter, "")));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("deleteattendance"));
    }

    @Test
    public void parseCommand_emptyValues_rejectsInput() {
        for (String value : List.of("A0123456X", "CS2103", "F10-2", "2024-02-29")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND.replace(value, "")));
        }
    }

    @Test
    public void parseCommand_duplicatePrefixes_rejectsInput() {
        for (String parameter : List.of("sn/A0123456X", "m/CS2103", "c/F10-2", "d/2024-02-29",
                "sn/", "m/", "c/", "d/")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(VALID_COMMAND + " " + parameter));
        }
    }

    @Test
    public void parseCommand_unknownPrefixes_rejectsInput() {
        for (String parameter : List.of("x/value", "t/friend", "D/2024-02-29")) {
            assertThrows(ParseException.class, "Unknown parameter: " + parameter.split("/")[0] + "/", () ->
                    parser.parseCommand(VALID_COMMAND + " " + parameter));
        }
    }

    @Test
    public void parseCommand_extraText_rejectsInput() {
        assertThrows(ParseException.class, () -> parser.parseCommand("deleteattendance 1"
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
    public void parseCommand_invalidDates_rejectsWithoutNormalization() {
        for (String date : List.of("0000-01-01", "2023-02-29", "1900-02-29", "2024-02-30", "2024-13-01",
                "2024-04-31", "2024-00-01", "2024-01-00", "2024-2-29", "24-02-29", "20240229", "29-02-2024",
                "+2024-02-29", "10000-01-01", "2024-02-29T12:00:00", "today")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_ATTENDANCE_DATE, () ->
                    parser.parseCommand(VALID_COMMAND.replace("2024-02-29", date)));
        }
    }

    private DeleteAttendanceCommand expected(LocalDate date) {
        return new DeleteAttendanceCommand(new StudentNumber("A0123456X"), new ModuleCode("CS2103"),
                new ClassName("F10-2"), date);
    }
}
