package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Tests unassignment through displayed indices, filters, and retained attendance history. */
public class UnassignCommandTest {
    private final ModelManager model = new ModelManager();
    private AcademicClass target;
    private AcademicClass other;

    @BeforeEach
    public void setUp() {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        target = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        other = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F11"));
        model.addAcademicClass(target);
        model.addAcademicClass(other);
        model.assignStudentToClass(ALICE, target);
        model.assignStudentToClass(BENSON, target);
        model.assignStudentToClass(ALICE, other);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(1, 1).execute(null));
    }

    @Test
    public void execute_assignedStudent_removesOnlyTargetMembership() throws Exception {
        CommandResult result = command(2, 1).execute(model);
        assertEquals(new CommandResult(String.format(UnassignCommand.MESSAGE_SUCCESS,
                ALICE.getStudentNumber(), target)), result);
        assertEquals(Set.of(BENSON), target.getStudents());
        assertEquals(Set.of(ALICE), other.getStudents());
        assertEquals(List.of(BENSON, ALICE), model.getAddressBook().getPersonList());
        assertEquals(List.of(target, other), model.getAcademicClassList());
    }

    @Test
    public void execute_filteredStudent_usesDisplayedIndexAndPreservesFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        command(1, 1).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(Set.of(BENSON), target.getStudents());
    }

    @Test
    public void execute_sortedClassView_usesDisplayedOrderAndRefreshesMemberships() throws Exception {
        model.filterPersonsByClass(target);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        command(1, 1).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(Set.of(BENSON), target.getStudents());
        command(1, 1).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(target.getStudents().isEmpty());
    }

    @Test
    public void execute_filteredClass_usesDisplayedIndexAndRefreshesClassList() throws Exception {
        model.updateFilteredAcademicClassListByStudent(ALICE.getStudentNumber());
        command(2, 1).execute(model);
        assertEquals(List.of(other), model.getFilteredAcademicClassList());
        command(2, 1).execute(model);
        assertTrue(model.getFilteredAcademicClassList().isEmpty());
        assertFalse(other.getStudents().contains(ALICE));
    }

    @Test
    public void execute_invalidStudentIndex_preservesMemberships() {
        assertThrows(CommandException.class,
                Messages.getInvalidStudentIndexMessage(
                        Index.fromOneBased(3), model.getFilteredPersonList().size()), ()
                -> command(3, 1).execute(model));
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
    }

    @Test
    public void execute_invalidClassIndex_preservesMemberships() {
        assertThrows(CommandException.class,
                Messages.getInvalidClassIndexMessage(
                        Index.fromOneBased(3), model.getFilteredAcademicClassList().size()), ()
                -> command(1, 3).execute(model));
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
    }

    @Test
    public void execute_emptyStudentView_rejectsHiddenStudent() {
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class,
                Messages.getInvalidStudentIndexMessage(
                        Index.fromOneBased(1), model.getFilteredPersonList().size()), ()
                -> command(1, 1).execute(model));
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
    }

    @Test
    public void execute_emptyClassView_rejectsHiddenClass() {
        model.updateFilteredAcademicClassList(academicClass -> false);
        assertThrows(CommandException.class,
                Messages.getInvalidClassIndexMessage(
                        Index.fromOneBased(1), model.getFilteredAcademicClassList().size()), ()
                -> command(1, 1).execute(model));
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
    }

    @Test
    public void execute_studentNotAssigned_preservesMemberships() {
        assertThrows(CommandException.class, String.format(Messages.MESSAGE_STUDENT_NOT_IN_CLASS,
                BENSON.getStudentNumber(), other), () -> command(1, 2).execute(model));
        assertEquals(Set.of(ALICE), other.getStudents());
    }

    @Test
    public void execute_unassignTwiceWithUnchangedViews_secondAttemptFails() throws Exception {
        command(2, 1).execute(model);
        assertThrows(CommandException.class, String.format(Messages.MESSAGE_STUDENT_NOT_IN_CLASS,
                ALICE.getStudentNumber(), target), () -> command(2, 1).execute(model));
        assertEquals(Set.of(BENSON), target.getStudents());
    }

    @Test
    public void execute_withAttendance_retainsHistoryAndAllowsReassignment() throws Exception {
        LocalDate date = LocalDate.of(2024, 2, 29);
        model.addAttendance(ALICE, target, date);
        command(2, 1).execute(model);
        assertEquals(Set.of(date), model.getAttendanceList().getFirst().getAttendedDates());
        assertTrue(model.assignStudentToClass(ALICE, target));
        assertEquals(Set.of(date), model.getAttendanceList().getFirst().getAttendedDates());
    }

    private UnassignCommand command(int studentIndex, int classIndex) {
        return new UnassignCommand(Index.fromOneBased(studentIndex), Index.fromOneBased(classIndex));
    }
}
