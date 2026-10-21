package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddAttendanceCommand;
import seedu.address.logic.commands.DeleteAttendanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Protects command-specific feedback when attendance argument validation is shared. */
public class AttendanceParserValidationTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_missingOrUnexpectedArguments_reportsCorrectCommandUsage() {
        for (String args : List.of("", " sn/A0123456X m/CS2103 c/F10-2",
                " 1 sn/A0123456X m/CS2103 c/F10-2 d/2024-02-29")) {
            assertThrows(ParseException.class,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAttendanceCommand.MESSAGE_USAGE), () ->
                    parser.parseCommand("addattendance" + args));
            assertThrows(ParseException.class,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAttendanceCommand.MESSAGE_USAGE), () ->
                    parser.parseCommand("deleteattendance" + args));
        }
    }

    @Test
    public void parse_unknownPrefix_reportsSameFeedbackForBothCommands() {
        for (String command : List.of("addattendance", "deleteattendance")) {
            assertThrows(ParseException.class, "Unknown parameter: x/", () ->
                    parser.parseCommand(command + " x/value sn/A0123456X m/CS2103 c/F10-2 d/2024-02-29"));
        }
    }
}
