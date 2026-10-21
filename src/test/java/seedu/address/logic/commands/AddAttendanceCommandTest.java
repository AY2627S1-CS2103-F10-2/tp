package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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

/**
 * Tests attendance recording against the real model without changing memberships or views.
 */
public class AddAttendanceCommandTest {
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
    }

    @Test
    public void execute_assignedStudent_recordsDateWithCanonicalReferences() throws Exception {
        CommandResult result = command(DATE).execute(model);

        assertEquals("Recorded attendance for student (" + ALICE.getStudentNumber()
                + ") in CS2103-F10-2 on 2024-02-29.", result.getFeedbackToUser());
        Attendance record = model.getAttendanceList().getFirst();
        assertEquals(Set.of(DATE), record.getAttendedDates());
        assertEquals(1, record.getAttendanceCount());
        assertSame(ALICE, record.getStudent());
        assertSame(academicClass, record.getAcademicClass());
        assertUnchangedStudentsAndMembership();
    }

    @Test
    public void execute_today_recordsDate() throws Exception {
        LocalDate today = LocalDate.now();
        command(today).execute(model);
        assertEquals(Set.of(today), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_secondDate_preservesExistingDate() throws Exception {
        command(DATE).execute(model);
        command(DATE.plusDays(1)).execute(model);
        assertEquals(1, model.getAttendanceList().size());
        assertEquals(Set.of(DATE, DATE.plusDays(1)), model.getAttendanceList().getFirst().getAttendedDates());
        assertEquals(2, model.getAttendanceList().getFirst().getAttendanceCount());
    }

    @Test
    public void execute_duplicateDate_rejectsWithoutMutation() throws Exception {
        command(DATE).execute(model);
        assertFailureWithoutMutation(command(DATE), String.format(AddAttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE,
                ALICE.getStudentNumber(), academicClass, DATE));
    }

    @Test
    public void execute_futureDate_rejectsWithoutCreatingRecord() {
        assertFailureWithoutMutation(command(LocalDate.now().plusDays(1)),
                "Attendance date must not be in the future.");
    }

    @Test
    public void execute_futureDateWithExistingHistory_preservesHistory() throws Exception {
        command(DATE).execute(model);
        assertFailureWithoutMutation(command(LocalDate.MAX), "Attendance date must not be in the future.");
    }

    @Test
    public void execute_missingStudent_rejectsWithoutMutation() {
        StudentNumber missing = new StudentNumber("A9999999Z");
        assertFailureWithoutMutation(new AddAttendanceCommand(missing, MODULE, CLASS_NAME, DATE),
                String.format(AddAttendanceCommand.MESSAGE_STUDENT_NOT_FOUND, missing));
    }

    @Test
    public void execute_missingClass_rejectsWithoutMutation() {
        ModuleCode missing = new ModuleCode("CS9999");
        assertFailureWithoutMutation(new AddAttendanceCommand(ALICE.getStudentNumber(), missing, CLASS_NAME, DATE),
                String.format(AddAttendanceCommand.MESSAGE_CLASS_NOT_FOUND, missing, CLASS_NAME));
    }

    @Test
    public void execute_unassignedStudent_rejectsWithoutAssigning() {
        assertFailureWithoutMutation(new AddAttendanceCommand(BENSON.getStudentNumber(), MODULE, CLASS_NAME, DATE),
                String.format(AddAttendanceCommand.MESSAGE_NOT_ASSIGNED, BENSON.getStudentNumber(), academicClass));
    }

    @Test
    public void execute_unassignedWithHistory_rejectsNewDateUntilReassigned() throws Exception {
        command(DATE).execute(model);
        model.unassignStudentFromClass(ALICE, academicClass);
        assertFailureWithoutMutation(command(DATE.plusDays(1)),
                String.format(AddAttendanceCommand.MESSAGE_NOT_ASSIGNED, ALICE.getStudentNumber(), academicClass));
        model.assignStudentToClass(ALICE, academicClass);
        command(DATE.plusDays(1)).execute(model);
        assertEquals(1, model.getAttendanceList().size());
        assertEquals(Set.of(DATE, DATE.plusDays(1)), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void execute_sameDateForDifferentStudentOrModule_keepsSeparateHistories() throws Exception {
        AcademicClass otherModule = new AcademicClass(new ModuleCode("CS2101"), CLASS_NAME);
        model.addAcademicClass(otherModule);
        model.assignStudentToClass(ALICE, otherModule);
        model.assignStudentToClass(BENSON, academicClass);
        command(DATE).execute(model);
        new AddAttendanceCommand(BENSON.getStudentNumber(), MODULE, CLASS_NAME, DATE).execute(model);
        new AddAttendanceCommand(ALICE.getStudentNumber(), otherModule.getModuleCode(), CLASS_NAME, DATE)
                .execute(model);

        List<Attendance> records = model.getAttendanceList();
        assertEquals(3, records.size());
        assertEquals(List.of(ALICE, BENSON, ALICE), records.stream().map(Attendance::getStudent).toList());
        assertEquals(List.of(academicClass, academicClass, otherModule),
                records.stream().map(Attendance::getAcademicClass).toList());
        records.forEach(record -> assertEquals(Set.of(DATE), record.getAttendedDates()));
    }

    @Test
    public void execute_lowercaseIdentifiers_resolvesStoredEndpoints() throws Exception {
        new AddAttendanceCommand(new StudentNumber(ALICE.getStudentNumber().toString().toLowerCase()),
                new ModuleCode("cs2103"), new ClassName("f10-2"), DATE).execute(model);
        assertSame(ALICE, model.getAttendanceList().getFirst().getStudent());
        assertSame(academicClass, model.getAttendanceList().getFirst().getAcademicClass());
    }

    @Test
    public void execute_findView_preservesHiddenTargetAndClassFilter() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        model.updateFilteredAcademicClassList(unused -> false);
        var displayed = model.getFilteredPersonList();
        command(DATE).execute(model);
        assertSame(displayed, model.getFilteredPersonList());
        assertEquals(List.of(BENSON), displayed);
        assertEquals(List.of(), model.getFilteredAcademicClassList());
        assertUnchangedStudentsAndMembership();
    }

    @Test
    public void execute_listView_preservesInsertionOrder() throws Exception {
        command(DATE).execute(model);
        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_classView_preservesSortingAndMembershipRefresh() throws Exception {
        model.assignStudentToClass(BENSON, academicClass);
        model.filterPersonsByClass(academicClass);
        command(DATE).execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        model.unassignStudentFromClass(ALICE, academicClass);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_otherClassView_doesNotSwitchToAttendanceClass() throws Exception {
        AcademicClass otherClass = new AcademicClass(MODULE, new ClassName("F11-2"));
        model.addAcademicClass(otherClass);
        model.assignStudentToClass(BENSON, otherClass);
        model.filterPersonsByClass(otherClass);
        command(DATE).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(Set.of(ALICE), academicClass.getStudents());
        assertEquals(Set.of(BENSON), otherClass.getStudents());
    }

    @Test
    public void execute_failedAddition_preservesActiveClassFilter() throws Exception {
        model.filterPersonsByClass(academicClass);
        command(DATE).execute(model);
        assertFailureWithoutMutation(command(DATE), String.format(AddAttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE,
                ALICE.getStudentNumber(), academicClass, DATE));
        model.assignStudentToClass(BENSON, academicClass);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddAttendanceCommand(null, MODULE, CLASS_NAME, DATE));
        assertThrows(NullPointerException.class, () -> new AddAttendanceCommand(ALICE.getStudentNumber(),
                null, CLASS_NAME, DATE));
        assertThrows(NullPointerException.class, () -> new AddAttendanceCommand(ALICE.getStudentNumber(),
                MODULE, null, DATE));
        assertThrows(NullPointerException.class, () -> command(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(DATE).execute(null));
    }

    @Test
    public void equals_comparesAllCommandArguments() {
        AddAttendanceCommand original = command(DATE);
        assertEquals(original, original);
        assertEquals(original, command(DATE));
        assertFalse(original.equals(null));
        assertFalse(original.equals("addattendance"));
        assertFalse(original.equals(command(DATE.plusDays(1))));
        assertFalse(original.equals(new AddAttendanceCommand(BENSON.getStudentNumber(), MODULE, CLASS_NAME, DATE)));
        assertFalse(original.equals(new AddAttendanceCommand(ALICE.getStudentNumber(),
                new ModuleCode("CS2101"), CLASS_NAME, DATE)));
        assertFalse(original.equals(new AddAttendanceCommand(ALICE.getStudentNumber(),
                MODULE, new ClassName("F11-2"), DATE)));
    }

    @Test
    public void toString_containsTargetAndDate() {
        String expected = AddAttendanceCommand.class.getCanonicalName() + "{studentNumber="
                + ALICE.getStudentNumber() + ", moduleCode=CS2103, className=F10-2, date=2024-02-29}";
        assertEquals(expected, command(DATE).toString());
    }

    private AddAttendanceCommand command(LocalDate date) {
        return new AddAttendanceCommand(ALICE.getStudentNumber(), MODULE, CLASS_NAME, date);
    }

    private void assertFailureWithoutMutation(AddAttendanceCommand command, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<?> displayed = List.copyOf(model.getFilteredPersonList());
        Set<?> members = Set.copyOf(academicClass.getStudents());
        assertThrows(CommandException.class, message, () -> command.execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(members, academicClass.getStudents());
    }

    private void assertUnchangedStudentsAndMembership() {
        assertEquals(List.of(BENSON, ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(academicClass), model.getAcademicClassList());
        assertEquals(Set.of(ALICE), academicClass.getStudents());
    }
}
