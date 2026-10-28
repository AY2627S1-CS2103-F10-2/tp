package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.testutil.PersonBuilder;

/** Verifies displayed-index selection, independent filtering, and live class memberships. */
public class ListByStudentCommandTest {

    private final AcademicClass first = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
    private final AcademicClass second = new AcademicClass(new ModuleCode("CS2100"), new ClassName("L03"));
    private final AcademicClass third = new AcademicClass(new ModuleCode("CS2103"), new ClassName("T01"));
    private final ModelManager model = createModel();

    private ModelManager createModel() {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(BENSON, ALICE, DANIEL));
        first.assignStudent(ALICE);
        second.assignStudent(BENSON);
        third.assignStudent(ALICE);
        addressBook.setAcademicClasses(List.of(first, second, third));
        return new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_studentWithMultipleClasses_filtersClassesWithoutChangingStoredData() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandResult result = new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        assertEquals(List.of(first, third), model.getFilteredAcademicClassList());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
        assertEquals("Listed 2 classes for Alice Pauline (A0123456X).", result.getFeedbackToUser());
        assertEquals(Set.of(ALICE), first.getStudents());
    }

    @Test
    public void execute_studentWithOneClass_usesSingularFeedback() throws Exception {
        CommandResult result = new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(second), model.getFilteredAcademicClassList());
        assertEquals("Listed 1 class for " + BENSON.getName() + " (" + BENSON.getStudentNumber() + ").",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredStudents_usesDisplayedIndexAndSearchesAllClasses() throws Exception {
        model.updateFilteredPersonList(person -> person.isSamePerson(ALICE));
        model.updateFilteredAcademicClassList(academicClass -> academicClass.equals(second));

        new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(first, third), model.getFilteredAcademicClassList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortedStudents_usesDisplayedOrder() throws Exception {
        model.updateFilteredPersonListAlphabetically(person -> true);

        new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(first, third), model.getFilteredAcademicClassList());
        assertEquals(ALICE, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_unassignedStudent_showsEmptyClassList() throws Exception {
        model.updateFilteredPersonList(person -> person.isSamePerson(DANIEL));

        CommandResult result = new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(), model.getFilteredAcademicClassList());
        assertEquals(String.format(ListByStudentCommand.MESSAGE_SUCCESS, 0, DANIEL.getName(),
                DANIEL.getStudentNumber()), result.getFeedbackToUser());
        assertEquals(3, model.getAcademicClassList().size());
    }

    @Test
    public void execute_invalidDisplayedIndex_preservesBothViews() throws Exception {
        model.updateFilteredPersonList(person -> person.isSamePerson(ALICE));
        new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        CommandException exception = assertThrows(CommandException.class, ()
                -> new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model));

        assertEquals(Messages.getInvalidStudentIndexMessage(
                        INDEX_SECOND_PERSON, model.getFilteredPersonList().size()), exception.getMessage());
        assertEquals(List.of(first, third), model.getFilteredAcademicClassList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyStudentList_rejectsIndexWithoutChangingClasses() {
        model.updateFilteredPersonList(person -> false);

        assertThrows(CommandException.class, () -> new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model));
        assertEquals(List.of(first, second, third), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_brokenModelFilter_detectedByAssertion() {
        assertTrue(ListByStudentCommand.class.desiredAssertionStatus(), "Run this test with assertions enabled");
        ModelManager brokenModel = new ModelManager(model.getAddressBook(), new UserPrefs()) {
            @Override
            public void updateFilteredAcademicClassListByStudent(StudentNumber studentNumber) {
                // Simulate an internal filter defect that leaves unrelated classes visible.
            }
        };

        assertThrows(AssertionError.class, () -> new ListByStudentCommand(INDEX_SECOND_PERSON).execute(brokenModel));
    }

    @Test
    public void execute_membershipChanges_refreshesSelectedStudentClasses() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        model.unassignStudentFromClass(ALICE, first);
        assertEquals(List.of(third), model.getFilteredAcademicClassList());

        model.assignStudentToClass(ALICE, second);
        assertEquals(List.of(second, third), model.getFilteredAcademicClassList());

        model.assignStudentToClass(BENSON, first);
        assertEquals(List.of(second, third), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_deleteAndRecreateMatchingClass_usesCurrentMembershipsAndCreationOrder() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        model.deleteAcademicClass(first);
        assertEquals(List.of(third), model.getFilteredAcademicClassList());

        AcademicClass replacement = new AcademicClass(first.getModuleCode(), first.getClassName());
        model.addAcademicClass(replacement);
        assertEquals(List.of(third), model.getFilteredAcademicClassList());

        model.assignStudentToClass(ALICE, replacement);
        assertEquals(List.of(third, replacement), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_editSelectedStudentNumber_keepsSelectedStudentAndMemberships() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);
        Person editedAlice = new PersonBuilder(ALICE).withStudentNumber("A0999999Z").withName("Alice Tan").build();

        model.setPerson(ALICE, editedAlice);

        assertEquals(List.of(first, third), model.getFilteredAcademicClassList());
        model.unassignStudentFromClass(editedAlice, first);
        assertEquals(List.of(third), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_deleteSelectedStudent_clearsClassViewWithoutDeletingClasses() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        model.deletePerson(ALICE);

        assertEquals(List.of(), model.getFilteredAcademicClassList());
        assertEquals(List.of(first, second, third), model.getAcademicClassList());
    }

    @Test
    public void execute_listClass_restoresAllClassesAndClearsStudentFilter() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        new ListClassCommand().execute(model);
        model.unassignStudentFromClass(ALICE, first);

        assertEquals(List.of(first, second, third), model.getFilteredAcademicClassList());
    }

    @Test
    public void execute_newStudentSelection_replacesPreviousClassFilter() throws Exception {
        new ListByStudentCommand(INDEX_SECOND_PERSON).execute(model);

        new ListByStudentCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(second), model.getFilteredAcademicClassList());
    }

    @Test
    public void nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new ListByStudentCommand(null));
        assertThrows(NullPointerException.class, () -> new ListByStudentCommand(INDEX_FIRST_PERSON).execute(null));
        assertThrows(NullPointerException.class, () -> model.updateFilteredAcademicClassListByStudent(null));
    }

    @Test
    public void equalsAndToString_compareTargetIndex() {
        ListByStudentCommand command = new ListByStudentCommand(INDEX_FIRST_PERSON);
        assertEquals(command, command);
        assertEquals(command, new ListByStudentCommand(INDEX_FIRST_PERSON));
        assertNotEquals(command, new ListByStudentCommand(INDEX_SECOND_PERSON));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
        assertEquals(ListByStudentCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}",
                command.toString());
    }
}
