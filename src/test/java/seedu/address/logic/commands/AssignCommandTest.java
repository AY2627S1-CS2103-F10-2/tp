package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class AssignCommandTest {
    private final ModelManager model = new ModelManager();
    private final AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));

    @Test
    public void execute_sameClassNameAcrossModules_assignsOnlyTargetClass() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AcademicClass otherModule = new AcademicClass(new ModuleCode("CS2101"), new ClassName("F10-2"));
        model.addAcademicClass(otherModule);
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(BENSON, academicClass);

        new AssignCommand(ALICE.getStudentNumber().value, "CS2103T", "F10-2").execute(model);
        assertEquals(Set.of(ALICE, BENSON), academicClass.getStudents());
        assertTrue(otherModule.getStudents().isEmpty());
        new AssignCommand(ALICE.getStudentNumber().value, "CS2101", "F10-2").execute(model);
        assertEquals(Set.of(ALICE), otherModule.getStudents());
        assertEquals(Set.of(ALICE, BENSON), academicClass.getStudents());
    }

    @Test
    public void execute_existingStudentAndClass_assignsIdempotentlyWithoutChangingFilter() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addAcademicClass(academicClass);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        AssignCommand command = new AssignCommand("a0123456x", "cs2103t", "f10-2");
        String expected = String.format(AssignCommand.MESSAGE_SUCCESS, ALICE.getStudentNumber(), academicClass);

        assertEquals(expected, command.execute(model).getFeedbackToUser());
        assertEquals(expected, command.execute(model).getFeedbackToUser());
        assertEquals(Set.of(ALICE), academicClass.getStudents());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_missingOrInvalidStudent_reportsStudentFirstWithoutAssignment() {
        model.addAcademicClass(academicClass);
        for (String number : List.of("A9999999Z", "random-numbers")) {
            assertThrows(CommandException.class, String.format(AssignCommand.MESSAGE_STUDENT_NOT_FOUND, number), ()
                    -> new AssignCommand(number, "fake-module", "fake").execute(model));
        }
        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void execute_missingOrInvalidClass_doesNotAssign() {
        model.addPerson(ALICE);
        model.addAcademicClass(academicClass);
        for (String module : List.of("CS9999", "fake-module")) {
            assertThrows(CommandException.class,
                    String.format(AssignCommand.MESSAGE_CLASS_NOT_FOUND, module, "F10-2"), ()
                    -> new AssignCommand(ALICE.getStudentNumber().value, module, "F10-2").execute(model));
        }
        for (String name : List.of("W10", "---")) {
            assertThrows(CommandException.class,
                    String.format(AssignCommand.MESSAGE_CLASS_NOT_FOUND, "CS2103T", name), ()
                    -> new AssignCommand(ALICE.getStudentNumber().value, "CS2103T", name).execute(model));
        }
        assertTrue(academicClass.getStudents().isEmpty());
    }

    @Test
    public void nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AssignCommand(null, "CS2103T", "F10-2"));
        assertThrows(NullPointerException.class, () -> new AssignCommand("A0123456X", null, "F10-2"));
        assertThrows(NullPointerException.class, () -> new AssignCommand("A0123456X", "CS2103T", null));
        assertThrows(NullPointerException.class, ()
                -> new AssignCommand("A0123456X", "CS2103T", "F10-2").execute(null));
    }

    @Test
    public void equalsAndToString() {
        AssignCommand command = new AssignCommand("A0123456X", "CS2103T", "F10-2");
        assertTrue(command.equals(command));
        assertEquals(command, new AssignCommand("A0123456X", "CS2103T", "F10-2"));
        assertFalse(command.equals(null));
        assertFalse(command.equals("assign"));
        assertFalse(command.equals(new AssignCommand("A9999999Z", "CS2103T", "F10-2")));
        assertFalse(command.equals(new AssignCommand("A0123456X", "CS2101", "F10-2")));
        assertFalse(command.equals(new AssignCommand("A0123456X", "CS2103T", "W10")));
        assertEquals(AssignCommand.class.getCanonicalName()
                + "{studentNumber=A0123456X, moduleCode=CS2103T, className=F10-2}", command.toString());
    }
}
