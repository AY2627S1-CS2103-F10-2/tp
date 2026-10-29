package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises student name parsing, lookup, membership updates and JSON persistence together. */
public class StudentNameIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_allowedNameCharacters_preservesDisplayAndStoredNamesAcrossRestart() throws Exception {
        List<String> names = List.of("Anne-Marie Tan", "Sean O'Connor", "J. K. Tan", "John Tan, Jr.",
                "Tan Wei Ming (Alex)", "Ravi S\\O Kumar");
        for (int i = 0; i < names.size(); i++) {
            logic.execute("add sn/NAME203" + i + " n/" + names.get(i) + " e/student" + i + "@example.com");
        }

        var expectedNames = names.stream().map(name -> name.replace('\\', '/')).toList();
        assertEquals(expectedNames, model.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        ModelManager restarted = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(expectedNames, restarted.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals(model.getCommandHistory(), restarted.getCommandHistory());
    }

    @Test
    public void execute_editName_preservesMembershipAttendanceAndSearchAcrossRestart() throws Exception {
        logic.execute("add sn/A0800001X n/Ravi Kumar e/ravi@example.com");
        logic.execute("addclass m/CS2103 c/T01");
        logic.execute("assign 1 1");
        logic.execute("addattendance 1 1 d/2024-02-29");
        String updatedName = "Anne-Marie O'Connor, Jr. (Ravi S/O Kumar)";
        logic.execute("edit 1 n/Anne-Marie O'Connor, Jr. (Ravi S\\O Kumar)");

        for (String keyword : List.of("S\\O", "S/O", "Anne-Marie", "O'Connor,")) {
            logic.execute("find " + keyword);
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(updatedName, model.getFilteredPersonList().getFirst().getName().fullName);
        }

        ModelManager restarted = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        var student = restarted.getAddressBook().getPersonList().getFirst();
        assertEquals(updatedName, student.getName().fullName);
        assertSame(student, restarted.getAcademicClassList().getFirst().getStudents().iterator().next());
        assertSame(student, restarted.getAttendanceList().getFirst().getStudent());
        assertEquals(Set.of(LocalDate.of(2024, 2, 29)),
                restarted.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_backslashesResemblingPrefixes_doesNotSplitTheNameField() throws Exception {
        logic.execute("add sn/A0800001X n/Alex n\\Alias e\\Other sn\\Number tg\\Handle t\\Group "
                + "e/alex@example.com tg/alex_203 t/CS2103");
        var student = model.getAddressBook().getPersonList().getFirst();
        assertEquals("Alex n/Alias e/Other sn/Number tg/Handle t/Group", student.getName().fullName);
        assertEquals("alex@example.com", student.getEmail().value);
        assertEquals("alex_203", student.getTelegram().orElseThrow().value);
        assertEquals(1, student.getTags().size());

        logic.execute("edit 1 n/Blair n\\Other e\\Alias e/blair@example.com");
        student = addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("Blair n/Other e/Alias", student.getName().fullName);
        assertEquals("blair@example.com", student.getEmail().value);
        logic.execute("find n\\Other");
        assertEquals(List.of(student), model.getFilteredPersonList());
    }

    @Test
    public void execute_rejectedNameCharacters_preservesModelAndSavedData() throws Exception {
        logic.execute("add sn/A0800001X n/Existing Student e/existing@example.com");
        String savedContents = Files.readString(addressBookStorage.getAddressBookFilePath());
        var originalStudent = model.getAddressBook().getPersonList().getFirst();

        for (String name : List.of("Sean O\u2019Connor", "Jos\u00e9 Tan", "\u738b\u660e", "Alex\u00b7Tan",
                "---", "()", "Ravi S/O Kumar", "Alex@Tan")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_NAME_CONSTRAINTS, ()
                    -> logic.execute("add sn/A0800002X n/" + name + " e/rejected@example.com"));
            assertThrows(ParseException.class, ParserUtil.MESSAGE_NAME_CONSTRAINTS, ()
                    -> logic.execute("edit 1 n/" + name));
            assertEquals(List.of(originalStudent), model.getAddressBook().getPersonList());
            assertEquals(savedContents, Files.readString(addressBookStorage.getAddressBookFilePath()));
        }
    }
}
