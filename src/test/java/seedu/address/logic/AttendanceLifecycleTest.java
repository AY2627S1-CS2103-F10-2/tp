package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.attendance.Attendance;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises attendance lifecycle operations through commands and real JSON storage. */
public class AttendanceLifecycleTest {
    private static final String TARGET = " sn/A0123456X m/CS2103 c/F10-2";
    private static final String ATTENDANCE_TARGET = " 1 1";
    private static final LocalDate DATE = LocalDate.of(2024, 2, 29);
    @TempDir
    public Path temporaryFolder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;
    private StorageManager storage;

    @BeforeEach
    public void setUp() throws Exception {
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        model = new ModelManager();
        logic = new LogicManager(model, storage);
        logic.execute("add sn/A0123456X n/Alice e/alice@example.com");
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("assign" + TARGET);
        logic.execute("addattendance" + ATTENDANCE_TARGET + " d/2024-02-29");
    }

    @Test
    public void execute_unassignDeleteReassign_keepsRemainingHistoryAcrossReload() throws Exception {
        logic.execute("addattendance" + ATTENDANCE_TARGET + " d/2024-02-28");
        logic.execute("unassign" + ATTENDANCE_TARGET);
        assertThrows(CommandException.class, () ->
                logic.execute("addattendance" + ATTENDANCE_TARGET + " d/2024-03-01"));
        logic.execute("deleteattendance" + ATTENDANCE_TARGET + " d/2024-02-29");
        reload();
        assertDates(Set.of(LocalDate.of(2024, 2, 28)));
        assertEquals(Set.of(), model.getAcademicClassList().getFirst().getStudents());
        logic.execute("assign" + TARGET);
        logic.execute("addattendance" + ATTENDANCE_TARGET + " d/2024-02-29");
        reload();
        assertDates(Set.of(LocalDate.of(2024, 2, 28), DATE));
    }

    @Test
    public void execute_editStudent_preservesHistoryUnderNewIdentity() throws Exception {
        logic.execute("edit 1 sn/A7654321Z n/Alice Updated e/updated@example.com");
        reload();
        var student = model.getAddressBook().getPersonList().getFirst();
        assertEquals("A7654321Z", student.getStudentNumber().toString());
        assertEquals("Alice Updated", student.getName().toString());
        assertSame(student, model.getAttendanceList().getFirst().getStudent());
        assertEquals(Set.of(student), model.getAcademicClassList().getFirst().getStudents());
        assertDates(Set.of(DATE));
        logic.execute("deleteattendance 1 1 d/2024-02-29");
        reload();
        assertDates(Set.of());
    }

    @Test
    public void execute_deleteStudent_removesOnlyTheirHistory() throws Exception {
        logic.execute("add sn/A9999999Z n/Bob e/bob@example.com");
        logic.execute("assign sn/A9999999Z m/CS2103 c/F10-2");
        logic.execute("addattendance 2 1 d/2024-02-29");
        logic.execute("delete 1");
        reload();
        assertDates(Set.of(DATE));
        assertEquals("A9999999Z", model.getAttendanceList().getFirst().getStudent().getStudentNumber().toString());
        assertEquals(1, model.getAcademicClassList().getFirst().getStudents().size());
    }

    @Test
    public void execute_deleteAndRecreateClass_doesNotRestoreOldHistory() throws Exception {
        logic.execute("addclass m/CS2101 c/F10-2");
        logic.execute("assign sn/A0123456X m/CS2101 c/F10-2");
        logic.execute("addattendance 1 2 d/2024-02-29");
        logic.execute("deleteclass m/CS2103 c/F10-2");
        reload();
        assertDates(Set.of(DATE));
        assertEquals("CS2101", model.getAttendanceList().getFirst().getAcademicClass().getModuleCode().toString());
        logic.execute("addclass m/CS2103 c/F10-2");
        logic.execute("assign" + TARGET);
        assertThrows(CommandException.class, () ->
                logic.execute("deleteattendance 1 2 d/2024-02-29"));
        logic.execute("addattendance 1 2 d/2024-02-29");
        reload();
        assertEquals(2, model.getAttendanceList().size());
    }

    @Test
    public void execute_clear_removesAttendanceAcrossReload() throws Exception {
        logic.execute("clear");
        reload();
        assertEquals(List.of(), model.getAttendanceList());
        assertEquals(List.of(), model.getAcademicClassList());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_loadedFutureHistory_canBeDeletedButNotAdded() throws Exception {
        AddressBook restored = new AddressBook(model.getAddressBook());
        restored.setAttendance(List.of(Attendance.fromRecordedDates(
                restored.getPersonList().getFirst(), restored.getAcademicClassList().getFirst(),
                Set.of(DATE, LocalDate.of(9999, 12, 31)))));
        addressBookStorage.saveAddressBook(restored);
        reload();
        logic.execute("deleteattendance" + ATTENDANCE_TARGET + " d/9999-12-31");
        reload();
        assertDates(Set.of(DATE));
        assertThrows(CommandException.class, () ->
                logic.execute("addattendance" + ATTENDANCE_TARGET + " d/9999-12-31"));
        reload();
        assertDates(Set.of(DATE));
    }

    @Test
    public void execute_yearBoundaries_rejectsZeroAndAcceptsYearOne() throws Exception {
        assertThrows(ParseException.class, () -> logic.execute("addattendance" + ATTENDANCE_TARGET + " d/0000-01-01"));
        assertThrows(ParseException.class, () ->
                logic.execute("deleteattendance" + ATTENDANCE_TARGET + " d/0000-01-01"));
        logic.execute("addattendance" + ATTENDANCE_TARGET + " d/0001-01-01");
        reload();
        assertDates(Set.of(LocalDate.of(1, 1, 1), DATE));
        logic.execute("deleteattendance" + ATTENDANCE_TARGET + " d/0001-01-01");
        reload();
        assertDates(Set.of(DATE));
    }

    private void reload() throws Exception {
        model = new ModelManager();
        model.setAddressBook(addressBookStorage.readAddressBook().orElseThrow());
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_renameClass_attendanceCommandsUseNewIdentityAcrossReload() throws Exception {
        logic.execute("listbyclass 1");
        logic.execute("editclass 1 m/CS2101 c/F11-2");
        assertEquals(1, model.getFilteredPersonList().size());
        assertDates(Set.of(DATE));
        assertSame(model.getAcademicClassList().getFirst(), model.getAttendanceList().getFirst().getAcademicClass());
        String renamedTarget = " 1 1";
        assertThrows(CommandException.class, () -> logic.execute("addattendance" + renamedTarget + " d/2024-02-29"));
        logic.execute("addattendance" + renamedTarget + " d/2024-03-01");
        logic.execute("deleteattendance" + renamedTarget + " d/2024-02-29");
        reload();
        assertDates(Set.of(LocalDate.of(2024, 3, 1)));
        assertEquals("CS2101-F11-2", model.getAttendanceList().getFirst().getAcademicClass().toString());
    }

    @Test
    public void execute_renameAfterUnassignment_canDeleteRetainedHistory() throws Exception {
        logic.execute("unassign" + ATTENDANCE_TARGET);
        logic.execute("editclass 1 c/F11-2");
        reload();
        assertDates(Set.of(DATE));
        assertEquals(Set.of(), model.getAcademicClassList().getFirst().getStudents());
        String renamedTarget = " 1 1";
        assertThrows(CommandException.class, () -> logic.execute("addattendance" + renamedTarget + " d/2024-03-01"));
        logic.execute("deleteattendance" + renamedTarget + " d/2024-02-29");
        reload();
        assertDates(Set.of());
        assertEquals(Set.of(), model.getAcademicClassList().getFirst().getStudents());
    }

    private void assertDates(Set<LocalDate> expected) {
        assertEquals(1, model.getAttendanceList().size());
        assertEquals(expected, model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(expected.size(), model.getAttendanceList().getFirst().getAttendanceCount());
    }
}
