package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.StudentNumber;

/** Tests attendance deletion without changing students, memberships or active views. */
public class DeleteAttendanceCommandTest {
    private static final LocalDate DATE = LocalDate.of(2024, 2, 29);
    private static final ModuleCode MODULE = new ModuleCode("CS2103");
    private static final ClassName CLASS_NAME = new ClassName("F10-2");
    private final ModelManager model = new ModelManager();
    private final AcademicClass academicClass = new AcademicClass(MODULE, CLASS_NAME);

    @BeforeEach
    public void setUp() {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(ALICE, academicClass);
        model.addAttendance(ALICE, academicClass, DATE);
    }

    @Test
    public void execute_recordedDate_removesOnlyRequestedDate() throws Exception {
        model.addAttendance(ALICE, academicClass, DATE.plusDays(1));
        CommandResult result = command(DATE).execute(model);
        assertEquals("Removed attendance for student (" + ALICE.getStudentNumber()
                + ") in CS2103-F10-2 on 2024-02-29.", result.getFeedbackToUser());
        assertEquals(Set.of(DATE.plusDays(1)), model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(1, model.getAttendanceList().getFirst().getAttendanceCount());
        assertEquals(List.of(BENSON, ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(academicClass), model.getAcademicClassList());
        assertEquals(Set.of(ALICE), academicClass.getStudents());
    }

    @Test
    public void execute_lastDate_canBeRecordedAgain() throws Exception {
        command(DATE).execute(model);
        assertEquals(0, model.getAttendanceList().getFirst().getAttendanceCount());
        new AddAttendanceCommand(ALICE.getStudentNumber(), MODULE, CLASS_NAME, DATE).execute(model);
        assertEquals(Set.of(DATE), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_unassignedStudent_removesHistoryWithoutReassigning() throws Exception {
        model.unassignStudentFromClass(ALICE, academicClass);
        command(DATE).execute(model);
        assertEquals(Set.of(), model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(Set.of(), academicClass.getStudents());
    }

    @Test
    public void execute_missingDate_rejectsWithoutMutation() {
        assertFailure(command(DATE.plusDays(1)), String.format(DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND,
                ALICE.getStudentNumber(), academicClass, DATE.plusDays(1)));
    }

    @Test
    public void execute_noHistory_rejectsWithoutCreatingRecord() {
        assertFailure(new DeleteAttendanceCommand(BENSON.getStudentNumber(), MODULE, CLASS_NAME, DATE),
                String.format(DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND,
                        BENSON.getStudentNumber(), academicClass, DATE));
    }

    @Test
    public void execute_repeatedDeletion_rejectsWithoutMutation() throws Exception {
        command(DATE).execute(model);
        assertFailure(command(DATE), String.format(DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND,
                ALICE.getStudentNumber(), academicClass, DATE));
    }

    @Test
    public void execute_missingStudent_rejectsWithoutMutation() {
        StudentNumber missing = new StudentNumber("A9999999Z");
        assertFailure(new DeleteAttendanceCommand(missing, MODULE, CLASS_NAME, DATE),
                String.format(DeleteAttendanceCommand.MESSAGE_STUDENT_NOT_FOUND, missing));
    }

    @Test
    public void execute_missingClass_rejectsWithoutMutation() {
        ModuleCode missing = new ModuleCode("CS9999");
        assertFailure(new DeleteAttendanceCommand(ALICE.getStudentNumber(), missing, CLASS_NAME, DATE),
                String.format(DeleteAttendanceCommand.MESSAGE_CLASS_NOT_FOUND, missing, CLASS_NAME));
    }

    @Test
    public void execute_futureDate_rejectsWithoutMutation() {
        assertFailure(command(LocalDate.MAX), "Attendance date must not be in the future.");
    }

    @Test
    public void execute_savedFutureDate_preservesHistoryUnderExistingDatePolicy() {
        AddressBook restored = new AddressBook(model.getAddressBook());
        restored.setAttendance(List.of(Attendance.fromRecordedDates(ALICE, academicClass,
                Set.of(DATE, LocalDate.MAX))));
        model.setAddressBook(restored);
        assertFailure(command(LocalDate.MAX), "Attendance date must not be in the future.");
        assertEquals(Set.of(DATE, LocalDate.MAX), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_otherHistories_preservesTheirDates() throws Exception {
        AcademicClass otherClass = new AcademicClass(MODULE, new ClassName("F11-2"));
        AcademicClass otherModule = new AcademicClass(new ModuleCode("CS2101"), CLASS_NAME);
        model.addAcademicClass(otherClass);
        model.addAcademicClass(otherModule);
        model.addAttendance(BENSON, academicClass, DATE);
        model.addAttendance(ALICE, otherClass, DATE);
        model.addAttendance(ALICE, otherModule, DATE);
        command(DATE).execute(model);
        assertEquals(4, model.getAttendanceList().size());
        assertEquals(Set.of(), model.getAttendanceList().getFirst().getAttendedDates());
        model.getAttendanceList().subList(1, 4).forEach(record ->
                assertEquals(Set.of(DATE), record.getAttendedDates()));
    }

    @Test
    public void execute_findView_preservesFiltersAndHiddenTarget() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        model.updateFilteredAcademicClassList(unused -> false);
        command(DATE).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(List.of(), model.getFilteredAcademicClassList());
        assertEquals(Set.of(), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_classView_preservesMembershipRefresh() throws Exception {
        model.filterPersonsByClass(academicClass);
        command(DATE).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        model.assignStudentToClass(BENSON, academicClass);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteAttendanceCommand(null, MODULE, CLASS_NAME, DATE));
        assertThrows(NullPointerException.class, () -> new DeleteAttendanceCommand(ALICE.getStudentNumber(),
                null, CLASS_NAME, DATE));
        assertThrows(NullPointerException.class, () -> new DeleteAttendanceCommand(ALICE.getStudentNumber(),
                MODULE, null, DATE));
        assertThrows(NullPointerException.class, () -> command(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(DATE).execute(null));
    }

    @Test
    public void equals_comparesAllArguments() {
        DeleteAttendanceCommand original = command(DATE);
        assertEquals(original, original);
        assertEquals(original, command(DATE));
        assertFalse(original.equals(null));
        assertFalse(original.equals("deleteattendance"));
        assertFalse(original.equals(command(DATE.plusDays(1))));
        assertFalse(original.equals(new DeleteAttendanceCommand(BENSON.getStudentNumber(), MODULE, CLASS_NAME, DATE)));
        assertFalse(original.equals(new DeleteAttendanceCommand(ALICE.getStudentNumber(),
                new ModuleCode("CS2101"), CLASS_NAME, DATE)));
        assertFalse(original.equals(new DeleteAttendanceCommand(ALICE.getStudentNumber(),
                MODULE, new ClassName("F11-2"), DATE)));
    }

    @Test
    public void toString_containsTargetAndDate() {
        assertEquals(DeleteAttendanceCommand.class.getCanonicalName() + "{studentNumber="
                + ALICE.getStudentNumber() + ", moduleCode=CS2103, className=F10-2, date=2024-02-29}",
                command(DATE).toString());
    }

    private DeleteAttendanceCommand command(LocalDate date) {
        return new DeleteAttendanceCommand(ALICE.getStudentNumber(), MODULE, CLASS_NAME, date);
    }

    private void assertFailure(DeleteAttendanceCommand command, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<?> displayed = List.copyOf(model.getFilteredPersonList());
        Set<?> members = Set.copyOf(academicClass.getStudents());
        assertThrows(CommandException.class, message, () -> command.execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(members, academicClass.getStudents());
    }
}
