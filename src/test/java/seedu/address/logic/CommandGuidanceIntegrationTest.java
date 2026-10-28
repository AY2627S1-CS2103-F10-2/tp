package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks actionable feedback across parsers and displayed views without mutating saved data on rejection. */
public class CommandGuidanceIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private Path savedFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        savedFile = temporaryFolder.resolve("addressbook.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(savedFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("add sn/A0123456X n/Alice Tan e/alice@example.com t/CS2103");
        logic.execute("add sn/A0234567Y n/Bob Lim e/bob@example.com");
        logic.execute("addclass m/CS2103T c/F01");
        logic.execute("addclass m/ST2334 c/T24");
    }

    @Test
    public void execute_unknownPrefixes_identifiesSupportedFields() throws Exception {
        String[][] cases = {
            {"add sn/A0345678Z n/Carol Tan e/carol@example.com x/extra", "sn/, n/, e/, tg/, t/"},
            {"edit 1 n/Alice Tan x/extra", "sn/, n/, e/, tg/, t/"},
            {"addclass m/CS2103 c/F02 x/extra", "m/, c/"},
            {"editclass 1 c/F01 x/extra", "m/, c/"},
            {"listbyclass 1 t/5 x/extra", "t/"},
            {"addattendance 1 1 d/2024-02-29 x/extra", "d/"},
            {"deleteattendance 1 1 d/2024-02-29 x/extra", "d/"}
        };
        for (String[] entry : cases) {
            assertRejected(entry[0], ParseException.class,
                    "Unknown parameter: x/. Supported parameters: " + entry[1] + ".");
        }
    }

    @Test
    public void execute_missingFields_identifiesRequiredParameter() throws Exception {
        String[][] cases = {
            {"add n/Carol Tan e/carol@example.com", "sn/STUDENT_NUMBER"},
            {"add sn/A0345678Z e/carol@example.com", "n/NAME"},
            {"add sn/A0345678Z n/Carol Tan", "e/EMAIL"},
            {"addattendance 1 1", "d/YYYY-MM-DD"},
            {"deleteattendance 1 1", "d/YYYY-MM-DD"}
        };
        for (String[] entry : cases) {
            assertRejected(entry[0], ParseException.class, "Missing required parameter: " + entry[1]);
        }
    }

    @Test
    public void execute_invalidStudentIndices_usesFilteredRangeAndEmptyViewGuidance() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice Tan"));
        List<String> commands = List.of("edit 9 n/Carol Tan", "delete 9", "listbystudent 9", "assign 9 1",
                "unassign 9 1", "addattendance 9 1 d/2024-02-29", "deleteattendance 9 1 d/2024-02-29");
        for (String command : commands) {
            assertRejected(command, CommandException.class,
                    "Student index 9 is invalid for the current Students list. "
                    + "Choose an index from 1 to 1, or use list to show all students.");
        }
        model.updateFilteredPersonList(person -> false);
        for (String command : commands) {
            assertRejected(command, CommandException.class,
                    "No students are displayed. Use list to show all students, or add a student.");
        }
    }

    @Test
    public void execute_invalidClassIndices_usesFilteredRangeAndEmptyViewGuidance() throws Exception {
        model.updateFilteredAcademicClassList(academicClass -> academicClass.getClassName().value.equals("F01"));
        List<String> commands = List.of("editclass 9 c/F02", "deleteclass 9", "listbyclass 9", "assign 1 9",
                "unassign 1 9", "addattendance 1 9 d/2024-02-29", "deleteattendance 1 9 d/2024-02-29");
        for (String command : commands) {
            assertRejected(command, CommandException.class,
                    "Class index 9 is invalid for the current Classes list. "
                    + "Choose an index from 1 to 1, or use listclass to show all classes.");
        }
        model.updateFilteredAcademicClassList(academicClass -> false);
        for (String command : commands) {
            assertRejected(command, CommandException.class,
                    "No classes are displayed. Use listclass to show all classes, or add a class with addclass.");
        }
    }

    @Test
    public void execute_unknownCommands_includesInputAndRecovery() throws Exception {
        for (String command : List.of("LIST", "unknownCommand")) {
            assertRejected(command, ParseException.class, "Unknown command '" + command
                    + "'. Command names are case-sensitive. Type help for usage instructions.");
        }
    }

    @Test
    public void execute_thresholdBoundaries_explainsFieldAndSupportedRange() throws Exception {
        for (String value : List.of("", "0", "-1", "1.5", "abc", "2147483648")) {
            assertRejected("listbyclass 1 t/" + value, ParseException.class,
                    "Attendance threshold t/ must be a whole number from 1 to 2147483647. Example: t/5.");
        }
    }

    @Test
    public void execute_editEmptyTag_stillClearsTags() throws Exception {
        logic.execute("edit 1 t/");
        assertEquals(0, model.getAddressBook().getPersonList().getFirst().getTags().size());
    }

    private void assertRejected(String command, Class<? extends Throwable> exceptionType, String message)
            throws Exception {
        String savedContents = Files.readString(savedFile);
        var savedData = new JsonAddressBookStorage(savedFile).readAddressBook().orElseThrow();
        var students = List.copyOf(model.getFilteredPersonList());
        var classes = List.copyOf(model.getFilteredAcademicClassList());
        assertThrows(exceptionType, message, () -> logic.execute(command));
        assertEquals(savedContents, Files.readString(savedFile));
        assertEquals(savedData, model.getAddressBook());
        assertEquals(savedData.getCommandHistory(), model.getCommandHistory());
        assertEquals(students, model.getFilteredPersonList());
        assertEquals(classes, model.getFilteredAcademicClassList());
    }
}
