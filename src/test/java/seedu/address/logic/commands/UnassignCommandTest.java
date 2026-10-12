package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;

/**
 * Tests unassign command execution and its interaction with the model.
 */
public class UnassignCommandTest {
    private static final ModuleCode MODULE_CODE = new ModuleCode("CS2103");
    private static final ClassName CLASS_NAME = new ClassName("F10");

    private final ModelManager model = new ModelManager();

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> commandFor(ALICE).execute(null));
    }

    @Test
    public void execute_assignedStudent_removesOnlyTargetMembership() throws Exception {
        AcademicClass targetClass = new AcademicClassWithStudents(MODULE_CODE, CLASS_NAME, ALICE, BENSON);
        AcademicClass otherClass = new AcademicClassWithStudents(MODULE_CODE, new ClassName("F11"), ALICE);
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addAcademicClass(targetClass);
        model.addAcademicClass(otherClass);

        CommandResult result = commandFor(ALICE).execute(model);

        assertEquals(new CommandResult(String.format(UnassignCommand.MESSAGE_SUCCESS,
                ALICE.getStudentNumber(), targetClass)), result);
        assertEquals(Set.of(BENSON), targetClass.getStudents());
        assertEquals(Set.of(ALICE), otherClass.getStudents());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(targetClass, otherClass), model.getAcademicClassList());
    }

    @Test
    public void execute_studentHiddenByFilter_preservesFilterAndDisplayedOrder() throws Exception {
        AcademicClass targetClass = new AcademicClassWithStudents(MODULE_CODE, CLASS_NAME, ALICE, BENSON);
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addAcademicClass(targetClass);
        model.updateFilteredPersonListAlphabetically(person -> person.equals(BENSON));

        commandFor(ALICE).execute(model);

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(Set.of(BENSON), targetClass.getStudents());
    }

    @Test
    public void execute_studentNotFound_throwsCommandExceptionWithoutChangingMembership() {
        AcademicClass targetClass = new AcademicClassWithStudents(MODULE_CODE, CLASS_NAME, BENSON);
        model.addPerson(BENSON);
        model.addAcademicClass(targetClass);

        assertThrows(CommandException.class, String.format(UnassignCommand.MESSAGE_STUDENT_NOT_FOUND,
                ALICE.getStudentNumber()), () -> commandFor(ALICE).execute(model));

        assertEquals(Set.of(BENSON), targetClass.getStudents());
        assertEquals(List.of(BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_classNotFound_throwsCommandException() {
        model.addPerson(ALICE);
        AcademicClass otherClass = new AcademicClassWithStudents(MODULE_CODE, new ClassName("F11"), ALICE);
        model.addAcademicClass(otherClass);

        assertThrows(CommandException.class, String.format(UnassignCommand.MESSAGE_CLASS_NOT_FOUND,
                MODULE_CODE, CLASS_NAME), () -> commandFor(ALICE).execute(model));

        assertEquals(Set.of(ALICE), otherClass.getStudents());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_studentNotAssigned_throwsCommandException() {
        AcademicClass targetClass = new AcademicClass(MODULE_CODE, CLASS_NAME);
        model.addPerson(ALICE);
        model.addAcademicClass(targetClass);

        assertThrows(CommandException.class, "Student is not assigned to this class.", () ->
                commandFor(ALICE).execute(model));

        assertTrue(targetClass.getStudents().isEmpty());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_unassignTwice_secondAttemptThrowsCommandException() throws Exception {
        AcademicClass targetClass = new AcademicClassWithStudents(MODULE_CODE, CLASS_NAME, ALICE);
        model.addPerson(ALICE);
        model.addAcademicClass(targetClass);
        UnassignCommand command = commandFor(ALICE);

        command.execute(model);
        assertThrows(CommandException.class, "Student is not assigned to this class.", () -> command.execute(model));

        assertFalse(targetClass.getStudents().contains(ALICE));
    }

    private UnassignCommand commandFor(Person person) {
        return new UnassignCommand(person.getStudentNumber(), MODULE_CODE, CLASS_NAME);
    }

    /**
     * Supplies pre-existing memberships until the production assignment API is available.
     * This fixture tests command/model coordination, not AcademicClass's removal implementation.
     */
    private static class AcademicClassWithStudents extends AcademicClass {
        private final Set<Person> members = new HashSet<>();

        AcademicClassWithStudents(ModuleCode moduleCode, ClassName className, Person... persons) {
            super(moduleCode, className);
            members.addAll(List.of(persons));
        }

        @Override
        public Set<Person> getStudents() {
            return Collections.unmodifiableSet(members);
        }

        @Override
        public boolean unassignStudent(Person person) {
            return members.remove(person);
        }
    }
}
