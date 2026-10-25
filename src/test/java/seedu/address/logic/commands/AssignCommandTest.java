package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

public class AssignCommandTest {
    private final ModelManager model = new ModelManager();
    private AcademicClass target;
    private AcademicClass other;

    @BeforeEach
    public void setUp() {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        target = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10"));
        other = new AcademicClass(new ModuleCode("CS2100"), new ClassName("L01"));
        model.addAcademicClass(other);
        model.addAcademicClass(target);
    }

    @Test
    public void execute_validIndices_assignsOnlySelectedStudentAndClass() throws Exception {
        assertEquals(new CommandResult(String.format(AssignCommand.MESSAGE_SUCCESS,
                ALICE.getStudentNumber(), target)), command(2, 2).execute(model));
        assertEquals(Set.of(ALICE), target.getStudents());
        assertTrue(other.getStudents().isEmpty());
        assertEquals(List.of(BENSON, ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_filteredLists_usesBothDisplayedIndicesAndPreservesViews() throws Exception {
        model.assignStudentToClass(BENSON, target);
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        model.updateFilteredAcademicClassListByStudent(BENSON.getStudentNumber());
        command(1, 1).execute(model);
        assertEquals(Set.of(ALICE, BENSON), target.getStudents());
        assertTrue(other.getStudents().isEmpty());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(target), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_sortedClassView_usesDisplayedStudentOrder() throws Exception {
        model.assignStudentToClass(ALICE, other);
        model.assignStudentToClass(BENSON, other);
        model.filterPersonsByClass(other);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        command(1, 2).execute(model);
        assertEquals(Set.of(ALICE), target.getStudents());
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndices_preservesMemberships() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, ()
                -> command(3, 1).execute(model));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX, ()
                -> command(1, 3).execute(model));
        assertTrue(target.getStudents().isEmpty());
        assertTrue(other.getStudents().isEmpty());
    }

    @Test
    public void execute_hiddenRecords_cannotBeSelected() {
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, ()
                -> command(1, 1).execute(model));
        model.updateFilteredPersonList(person -> true);
        model.updateFilteredAcademicClassList(academicClass -> false);
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX, ()
                -> command(1, 1).execute(model));
        assertTrue(target.getStudents().isEmpty());
    }

    @Test
    public void execute_duplicateAssignment_rejectedWithoutChangingMembership() throws Exception {
        command(2, 2).execute(model);
        assertThrows(CommandException.class, String.format(AssignCommand.MESSAGE_ALREADY_ASSIGNED,
                ALICE.getStudentNumber(), target), () -> command(2, 2).execute(model));
        assertEquals(Set.of(ALICE), target.getStudents());
    }

    @Test
    public void execute_reassignment_preservesAttendance() throws Exception {
        model.assignStudentToClass(ALICE, target);
        LocalDate date = LocalDate.of(2024, 2, 29);
        model.addAttendance(ALICE, target, date);
        model.unassignStudentFromClass(ALICE, target);
        command(2, 2).execute(model);
        assertEquals(Set.of(date), model.getAttendanceList().getFirst().getAttendedDates());
    }

    @Test
    public void constructorOrExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new AssignCommand(null, Index.fromOneBased(1)));
        assertThrows(NullPointerException.class, () -> new AssignCommand(Index.fromOneBased(1), null));
        assertThrows(NullPointerException.class, () -> command(1, 1).execute(null));
    }

    @Test
    public void equalsAndToString_useBothIndices() {
        AssignCommand command = command(1, 2);
        assertEquals(command, command);
        assertEquals(command(1, 2), command);
        assertNotEquals(command(2, 2), command);
        assertNotEquals(command(1, 1), command);
        assertNotEquals(null, command);
        assertNotEquals("assign", command);
        assertTrue(command.toString().contains("studentIndex=" + Index.fromOneBased(1)));
        assertTrue(command.toString().contains("classIndex=" + Index.fromOneBased(2)));
    }

    private AssignCommand command(int studentIndex, int classIndex) {
        return new AssignCommand(Index.fromOneBased(studentIndex), Index.fromOneBased(classIndex));
    }
}
