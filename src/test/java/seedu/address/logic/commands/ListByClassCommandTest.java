package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ListByClassCommandTest {

    private final AddressBook addressBook = new AddressBookBuilder()
            .withPerson(BENSON).withPerson(ALICE).withPerson(DANIEL).build();
    private final ModelManager model = new ModelManager(addressBook, new UserPrefs());

    @Test
    public void execute_matchingClass_displaysOnlyMembersAlphabetically() throws CommandException {
        AcademicClass academicClass = new AcademicClassWithMembers("CS2103", "F10-2", Set.of(BENSON, ALICE));
        model.addAcademicClass(academicClass);
        model.addAcademicClass(new AcademicClassWithMembers("CS2100", "F10-2", Set.of(DANIEL)));
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F11-2", Set.of(DANIEL)));

        CommandResult result = new ListByClassCommand("CS2103", "F10-2").execute(model);

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals("Listed 2 students taking CS2103 (F10-2).", result.getFeedbackToUser());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
        assertEquals(Set.of(ALICE, BENSON), academicClass.getStudents());
        assertEquals(3, model.getAcademicClassList().size());
    }

    @Test
    public void execute_previousFilter_doesNotRestrictClassMembers() throws CommandException {
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON)));
        model.updateFilteredPersonList(person -> person.isSamePerson(DANIEL));

        new ListByClassCommand("CS2103", "F10-2").execute(model);

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_caseAndWhitespaceVariations_matchesExactPair() throws CommandException {
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F10-2", Set.of(ALICE)));

        new ListByClassCommand(" cs2103 ", " f10-2 ").execute(model);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_existingEmptyClass_displaysEmptyList() throws CommandException {
        model.addAcademicClass(new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2")));

        CommandResult result = new ListByClassCommand("CS2103", "F10-2").execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals("Listed 0 students taking CS2103 (F10-2).", result.getFeedbackToUser());
    }

    @Test
    public void execute_missingClass_preservesPreviousView() {
        model.addAcademicClass(new AcademicClassWithMembers("CS2100", "F10-2", Set.of(ALICE)));
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(BENSON));

        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("CS2103", "F10-2").execute(model));

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sameModuleDifferentClass_throwsCommandException() {
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F11-2", Set.of(ALICE)));

        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("CS2103", "F10-2").execute(model));

        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_unicodeClassName_doesNotMatchAsciiClass() {
        model.addAcademicClass(new AcademicClass(new ModuleCode("CS2103"), new ClassName("SS1")));
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(BENSON));

        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("CS2103", "\u00df1").execute(model));

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_unicodeModuleCode_doesNotMatchAsciiModule() {
        model.addAcademicClass(new AcademicClass(new ModuleCode("SS2103"), new ClassName("F10-2")));

        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("\u00df2103", "F10-2").execute(model));

        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidCreationFormat_reportsClassNotFound() {
        model.addAcademicClass(new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2")));

        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("2103", "F10-2").execute(model));
        assertThrows(CommandException.class, ListByClassCommand.MESSAGE_CLASS_NOT_FOUND, () ->
                new ListByClassCommand("CS2103", "---").execute(model));

        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_updatedStudentDetails_matchesByStudentNumber() throws CommandException {
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F10-2", Set.of(ALICE)));
        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe Pauline").build();
        model.setPerson(ALICE, renamedAlice);

        new ListByClassCommand("CS2103", "F10-2").execute(model);

        assertEquals(List.of(renamedAlice), model.getFilteredPersonList());
    }

    @Test
    public void execute_deletedStudent_doesNotDisplayStaleMembershipRecord() throws CommandException {
        model.addAcademicClass(new AcademicClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON)));
        model.deletePerson(ALICE);

        new ListByClassCommand("CS2103", "F10-2").execute(model);

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListByClassCommand("CS2103", "F10-2").execute(null));
    }

    @Test
    public void constructor_invalidArguments_throwsException() {
        assertThrows(NullPointerException.class, () -> new ListByClassCommand(null, "F10-2"));
        assertThrows(NullPointerException.class, () -> new ListByClassCommand("CS2103", null));
        assertThrows(IllegalArgumentException.class, () -> new ListByClassCommand("  ", "F10-2"));
        assertThrows(IllegalArgumentException.class, () -> new ListByClassCommand("CS2103", "  "));
    }

    @Test
    public void equals() {
        ListByClassCommand command = new ListByClassCommand("CS2103", "F10-2");
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListByClassCommand("cs2103", "f10-2")));
        assertFalse(command.equals(null));
        assertFalse(command.equals("CS2103"));
        assertFalse(command.equals(new ListByClassCommand("CS2100", "F10-2")));
        assertFalse(command.equals(new ListByClassCommand("CS2103", "F11-2")));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ListByClassCommand.class.getCanonicalName() + "{moduleCode=CS2103, className=F10-2}",
                new ListByClassCommand("CS2103", "F10-2").toString());
    }

    /**
     * Supplies membership fixtures through the public read contract until assignment support is integrated.
     * This does not exercise the assignment command or membership persistence.
     */
    private static class AcademicClassWithMembers extends AcademicClass {
        private final Set<Person> members;

        AcademicClassWithMembers(String moduleCode, String className, Set<Person> members) {
            super(new ModuleCode(moduleCode), new ClassName(className));
            this.members = Set.copyOf(members);
        }

        @Override
        public Set<Person> getStudents() {
            return members;
        }
    }
}
