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

import seedu.address.commons.core.index.Index;
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
        AcademicClass academicClass = createClassWithMembers("CS2103", "F10-2", Set.of(BENSON, ALICE));
        model.addAcademicClass(academicClass);
        model.addAcademicClass(createClassWithMembers("CS2100", "F10-2", Set.of(DANIEL)));
        model.addAcademicClass(createClassWithMembers("CS2103", "F11-2", Set.of(DANIEL)));

        CommandResult result = new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals("Listed 2 students taking CS2103 (F10-2).", result.getFeedbackToUser());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
        assertEquals(Set.of(ALICE, BENSON), academicClass.getStudents());
        assertEquals(3, model.getAcademicClassList().size());
    }

    @Test
    public void execute_previousFilter_doesNotRestrictClassMembers() throws CommandException {
        model.addAcademicClass(createClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON)));
        model.updateFilteredPersonList(person -> person.isSamePerson(DANIEL));

        new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_caseAndWhitespaceVariations_matchesExactPair() throws CommandException {
        model.addAcademicClass(createClassWithMembers("CS2103", "F10-2", Set.of(ALICE)));

        new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_existingEmptyClass_displaysEmptyList() throws CommandException {
        model.addAcademicClass(new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2")));

        CommandResult result = new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals("Listed 0 students taking CS2103 (F10-2).", result.getFeedbackToUser());
    }

    @Test
    public void execute_missingClass_preservesPreviousView() {
        model.addAcademicClass(createClassWithMembers("CS2100", "F10-2", Set.of(ALICE)));
        model.updateFilteredPersonListAlphabetically(person -> person.isSamePerson(BENSON));

        assertThrows(CommandException.class, "The class index provided is invalid.", () ->
                new ListByClassCommand(Index.fromOneBased(2)).execute(model));

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sameModuleDifferentClass_throwsCommandException() {
        model.addAcademicClass(createClassWithMembers("CS2103", "F11-2", Set.of(ALICE)));

        assertThrows(CommandException.class, "The class index provided is invalid.", () ->
                new ListByClassCommand(Index.fromOneBased(2)).execute(model));

        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_updatedStudentDetails_matchesByStudentNumber() throws CommandException {
        model.addAcademicClass(createClassWithMembers("CS2103", "F10-2", Set.of(ALICE)));
        Person renamedAlice = new PersonBuilder(ALICE).withName("Zoe Pauline").build();
        model.setPerson(ALICE, renamedAlice);

        new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(renamedAlice), model.getFilteredPersonList());
    }

    @Test
    public void execute_deletedStudent_doesNotDisplayStaleMembershipRecord() throws CommandException {
        model.addAcademicClass(createClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON)));
        model.deletePerson(ALICE);

        new ListByClassCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        ListByClassCommand command = new ListByClassCommand(Index.fromOneBased(1));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListByClassCommand(null));
    }

    @Test
    public void equals() {
        ListByClassCommand command = new ListByClassCommand(Index.fromOneBased(1));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListByClassCommand(Index.fromOneBased(1))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("CS2103"));
        assertFalse(command.equals(new ListByClassCommand(Index.fromOneBased(2))));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ListByClassCommand.class.getCanonicalName()
                        + "{index=seedu.address.commons.core.index.Index{zeroBasedIndex=0}}",
                new ListByClassCommand(Index.fromOneBased(1)).toString());
    }

    /**
     * Creates a real academic class populated through the public membership API.
     */
    private static AcademicClass createClassWithMembers(String moduleCode, String className, Set<Person> members) {
        AcademicClass academicClass = new AcademicClass(new ModuleCode(moduleCode), new ClassName(className));
        members.forEach(academicClass::assignStudent);
        return academicClass;
    }
}
