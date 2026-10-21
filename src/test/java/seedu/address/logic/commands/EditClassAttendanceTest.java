package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditClassCommand.EditClassDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.AcademicClassNotFoundException;
import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests attendance preservation across class editing using the real model and storage.
 */
public class EditClassAttendanceTest {
    private static final ModuleCode MODULE = new ModuleCode("CS2103");
    private static final ClassName NAME = new ClassName("F01");
    private static final ModuleCode NEW_MODULE = new ModuleCode("CS2101");
    private static final ClassName NEW_NAME = new ClassName("F03");
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);

    private ModelManager model;
    private AcademicClass original;
    private AcademicClass other;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        original = new AcademicClass(MODULE, NAME);
        other = new AcademicClass(MODULE, new ClassName("F02"));
        model.addAcademicClass(original);
        model.addAcademicClass(other);
        model.assignStudentToClass(ALICE, original);
        model.assignStudentToClass(BENSON, original);
        model.addAttendance(ALICE, original, DATE);
        model.addAttendance(ALICE, original, DATE.plusDays(1));
        model.addAttendance(BENSON, original, DATE.plusDays(2));
        model.addAttendance(ALICE, other, DATE.plusDays(3));
    }

    @Test
    public void execute_bothFieldsEdited_rebindsAllStudentsWithoutChangingOtherHistory() throws Exception {
        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);

        assertEquals(3, model.getAttendanceList().size());
        assertHistory(model, ALICE, edited, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, edited, Set.of(DATE.plusDays(2)));
        assertHistory(model, ALICE, other, Set.of(DATE.plusDays(3)));
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
        assertTrue(model.findAcademicClass(MODULE, NAME).isEmpty());
    }

    @Test
    public void execute_classNameOnly_preservesAllHistory() throws Exception {
        AcademicClass edited = edit(null, NEW_NAME);
        assertEquals(MODULE, edited.getModuleCode());
        assertHistory(model, ALICE, edited, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, edited, Set.of(DATE.plusDays(2)));
    }

    @Test
    public void execute_moduleOnly_preservesAllHistory() throws Exception {
        AcademicClass edited = edit(NEW_MODULE, null);
        assertEquals(NAME, edited.getClassName());
        assertHistory(model, ALICE, edited, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, edited, Set.of(DATE.plusDays(2)));
    }

    @Test
    public void execute_sameNormalizedIdentity_rebindsToReplacementInstance() throws Exception {
        AcademicClass edited = edit(new ModuleCode("cs2103"), new ClassName("f01"));
        assertHistory(model, ALICE, edited, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, edited, Set.of(DATE.plusDays(2)));
    }

    @Test
    public void execute_unassignedStudent_preservesHistoricalAttendanceWithoutReassigning() throws Exception {
        model.unassignStudentFromClass(BENSON, original);

        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);

        assertEquals(Set.of(ALICE), edited.getStudents());
        assertHistory(model, BENSON, edited, Set.of(DATE.plusDays(2)));
        assertTrue(model.removeAttendance(BENSON, edited, DATE.plusDays(2)));
        assertHistory(model, BENSON, edited, Set.of());
    }

    @Test
    public void execute_emptyHistory_retainsZeroCountAssociation() throws Exception {
        model.removeAttendance(BENSON, original, DATE.plusDays(2));
        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);
        assertEquals(3, model.getAttendanceList().size());
        assertHistory(model, BENSON, edited, Set.of());
    }

    @Test
    public void execute_afterRename_attendanceOperationsUseNewIdentityWithoutDuplicateAssociations() throws Exception {
        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);
        assertFalse(model.addAttendance(ALICE, edited, DATE));
        assertTrue(model.addAttendance(ALICE, edited, DATE.plusDays(4)));
        assertTrue(model.removeAttendance(ALICE, edited, DATE.plusDays(1)));
        assertEquals(3, model.getAttendanceList().size());
        assertHistory(model, ALICE, edited, Set.of(DATE, DATE.plusDays(4)));
    }

    @Test
    public void execute_duplicateIdentity_rejectsWithoutChangingHistory() {
        List<Attendance> before = model.getAttendanceList();
        assertThrows(CommandException.class, EditClassCommand.MESSAGE_DUPLICATE_CLASS, () ->
                edit(other.getModuleCode(), other.getClassName()));
        assertEquals(before, model.getAttendanceList());
        assertHistory(model, ALICE, original, Set.of(DATE, DATE.plusDays(1)));
    }

    @Test
    public void setAcademicClass_rejectedReplacements_preserveAttendanceAndClassReferences() {
        List<Attendance> before = model.getAttendanceList();
        AcademicClass missing = new AcademicClass(NEW_MODULE, NEW_NAME);
        assertThrows(DuplicateAcademicClassException.class, () -> model.setAcademicClass(original, other));
        assertThrows(AcademicClassNotFoundException.class, () -> model.setAcademicClass(missing, original));
        assertThrows(NullPointerException.class, () -> model.setAcademicClass(original, null));
        assertThrows(NullPointerException.class, () -> model.setAcademicClass(null, missing));
        assertEquals(before, model.getAttendanceList());
        assertHistory(model, ALICE, original, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, original, Set.of(DATE.plusDays(2)));
    }

    @Test
    public void execute_renameTwice_rebindsAllHistoryToLatestClass() throws Exception {
        edit(NEW_MODULE, NEW_NAME);
        AcademicClass latest = edit(new ModuleCode("CS2100"), new ClassName("F04"));
        assertHistory(model, ALICE, latest, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, BENSON, latest, Set.of(DATE.plusDays(2)));
        assertTrue(model.findAcademicClass(NEW_MODULE, NEW_NAME).isEmpty());
    }

    @Test
    public void execute_thenEditStudent_keepsBothAttendanceEndpointsCanonical() throws Exception {
        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);
        Person updated = new PersonBuilder(ALICE).withStudentNumber("A8888883K").build();
        model.setPerson(ALICE, updated);
        assertHistory(model, updated, edited, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(model, updated, other, Set.of(DATE.plusDays(3)));
        assertEquals(Set.of(updated, BENSON), edited.getStudents());
    }

    @Test
    public void execute_thenDeleteRenamedClass_removesOnlyItsHistory() throws Exception {
        AcademicClass edited = edit(NEW_MODULE, NEW_NAME);
        model.deleteAcademicClass(edited);
        assertEquals(1, model.getAttendanceList().size());
        assertHistory(model, ALICE, other, Set.of(DATE.plusDays(3)));
    }

    @Test
    public void execute_saveAndReload_preservesDatesAndCanonicalEndpoints(@TempDir Path directory) throws Exception {
        model.unassignStudentFromClass(BENSON, original);
        edit(NEW_MODULE, NEW_NAME);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());

        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        AcademicClass restoredClass = restored.findAcademicClass(NEW_MODULE, NEW_NAME).orElseThrow();
        AcademicClass restoredOther = restored.findAcademicClass(other.getModuleCode(), other.getClassName())
                .orElseThrow();
        Person storedAlice = restored.findPersonByStudentNumber(ALICE.getStudentNumber()).orElseThrow();
        Person storedBenson = restored.findPersonByStudentNumber(BENSON.getStudentNumber()).orElseThrow();
        assertEquals(3, restored.getAttendanceList().size());
        assertHistory(restored, storedAlice, restoredClass, Set.of(DATE, DATE.plusDays(1)));
        assertHistory(restored, storedBenson, restoredClass, Set.of(DATE.plusDays(2)));
        assertHistory(restored, storedAlice, restoredOther, Set.of(DATE.plusDays(3)));
        assertEquals(Set.of(storedAlice), restoredClass.getStudents());
        assertTrue(restored.findAcademicClass(MODULE, NAME).isEmpty());
    }

    private AcademicClass edit(ModuleCode moduleCode, ClassName className) throws CommandException {
        EditClassDescriptor descriptor = new EditClassDescriptor();
        descriptor.setModuleCode(moduleCode);
        descriptor.setClassName(className);
        new EditClassCommand(Index.fromOneBased(1), descriptor).execute(model);
        return model.getAcademicClassList().getFirst();
    }

    private void assertHistory(ModelManager source, Person student, AcademicClass academicClass,
            Set<LocalDate> dates) {
        List<Attendance> matching = source.getAttendanceList().stream()
                .filter(record -> record.getStudent().isSamePerson(student)
                        && record.getAcademicClass().equals(academicClass))
                .toList();
        assertEquals(1, matching.size());
        Attendance attendance = matching.getFirst();
        assertSame(student, attendance.getStudent());
        assertSame(academicClass, attendance.getAcademicClass());
        assertEquals(dates, attendance.getAttendedDates());
        assertEquals(dates.size(), attendance.getAttendanceCount());
    }
}
