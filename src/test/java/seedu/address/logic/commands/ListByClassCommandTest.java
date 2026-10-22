package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    public void execute_withThreshold_filtersBoundaryCountsAndSortsAlphabetically() throws CommandException {
        AddressBook fullBook = new AddressBookBuilder()
                .withPerson(ELLE).withPerson(DANIEL).withPerson(CARL)
                .withPerson(BENSON).withPerson(ALICE).build();
        ModelManager thresholdModel = new ModelManager(fullBook, new UserPrefs());
        AcademicClass targetClass = createClassWithMembers(
                "CS2103", "F10-2", Set.of(ALICE, BENSON, CARL, DANIEL));
        thresholdModel.addAcademicClass(targetClass);

        // ALICE: 0 dates (no attendance record at all)
        // BENSON: 4 dates (below threshold 5 -> included)
        // CARL: 5 dates (at threshold 5 -> excluded)
        // DANIEL: 6 dates (above threshold 5 -> excluded)
        // ELLE: 0 dates, not assigned to class -> excluded
        recordAttendances(thresholdModel, BENSON, targetClass, 4);
        recordAttendances(thresholdModel, CARL, targetClass, 5);
        recordAttendances(thresholdModel, DANIEL, targetClass, 6);

        CommandResult result = new ListByClassCommand(Index.fromOneBased(1), Optional.of(5))
                .execute(thresholdModel);

        assertEquals(List.of(ALICE, BENSON), thresholdModel.getFilteredPersonList());
        assertEquals("Listed 2 students taking CS2103 (F10-2) with fewer than 5 recorded attendance dates.",
                result.getFeedbackToUser());
        assertEquals(Optional.of(targetClass), thresholdModel.getAttendanceDisplayClass());
    }

    @Test
    public void execute_withThreshold_ignoresAttendanceFromOtherClasses() throws CommandException {
        AcademicClass targetClass = createClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON));
        AcademicClass otherClass = createClassWithMembers("CS2100", "B01", Set.of(ALICE, BENSON));
        model.addAcademicClass(targetClass);
        model.addAcademicClass(otherClass);

        // ALICE has 6 attendances in otherClass but only 2 in targetClass -> included for t/5
        recordAttendances(model, ALICE, otherClass, 6);
        recordAttendances(model, ALICE, targetClass, 2);
        // BENSON has 5 attendances in targetClass -> excluded for t/5
        recordAttendances(model, BENSON, targetClass, 5);

        CommandResult result = new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).execute(model);

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals("Listed 1 students taking CS2103 (F10-2) with fewer than 5 recorded attendance dates.",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_withThreshold_supportsDisplayedIndexTargetingAndIndependentClassFilter()
            throws CommandException {
        AcademicClass firstClass = createClassWithMembers("CS2100", "B01", Set.of(DANIEL));
        AcademicClass secondClass = createClassWithMembers("CS2103", "F10-2", Set.of(BENSON, ALICE, DANIEL));
        model.addAcademicClass(firstClass);
        model.addAcademicClass(secondClass);

        recordAttendances(model, ALICE, secondClass, 5);
        recordAttendances(model, BENSON, secondClass, 4);
        recordAttendances(model, DANIEL, secondClass, 1);

        // Filter class list to only classes of ALICE (which leaves only secondClass at displayed index 1)
        model.updateFilteredAcademicClassListByStudent(ALICE.getStudentNumber());
        assertEquals(List.of(secondClass), model.getFilteredAcademicClassList());

        // Target displayed class index 1 with threshold 5
        new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).execute(model);

        // Class list filter remains independent, and student list has BENSON (index 1) and DANIEL (index 2)
        assertEquals(List.of(secondClass), model.getFilteredAcademicClassList());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());

        // Subsequent command targeting displayed student index 2 acts on DANIEL
        new AddAttendanceCommand(Index.fromOneBased(2), Index.fromOneBased(1), LocalDate.of(2024, 3, 1))
                .execute(model);
        assertFalse(model.addAttendance(DANIEL, secondClass, LocalDate.of(2024, 3, 1)));
    }

    @Test
    public void execute_withThreshold_dynamicallyUpdatesOnAttendanceAndMembershipChanges()
            throws CommandException {
        AcademicClass targetClass = createClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON));
        model.addAcademicClass(targetClass);
        recordAttendances(model, ALICE, targetClass, 4);
        recordAttendances(model, BENSON, targetClass, 5);

        new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Adding 5th attendance for ALICE crosses threshold 5 -> removed from displayed list
        model.addAttendance(ALICE, targetClass, LocalDate.of(2024, 2, 10));
        assertEquals(List.of(), model.getFilteredPersonList());

        // Deleting 5th attendance for BENSON drops count to 4 -> added to displayed list
        model.removeAttendance(BENSON, targetClass, LocalDate.of(2024, 1, 5));
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        // Assigning DANIEL (0 attendances) -> added to displayed list in alphabetical order
        model.assignStudentToClass(DANIEL, targetClass);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());

        // Unassigning BENSON -> removed from displayed list
        model.unassignStudentFromClass(BENSON, targetClass);
        assertEquals(List.of(DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_plainListByClassAfterThreshold_removesThresholdRestriction() throws CommandException {
        AcademicClass targetClass = createClassWithMembers("CS2103", "F10-2", Set.of(ALICE, BENSON));
        model.addAcademicClass(targetClass);
        recordAttendances(model, ALICE, targetClass, 2);
        recordAttendances(model, BENSON, targetClass, 5);

        new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        CommandResult plainResult = new ListByClassCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals("Listed 2 students taking CS2103 (F10-2).", plainResult.getFeedbackToUser());
    }

    @Test
    public void execute_missingClass_preservesPreviousView() throws CommandException {
        AcademicClass targetClass = createClassWithMembers("CS2100", "F10-2", Set.of(ALICE, BENSON));
        model.addAcademicClass(targetClass);
        recordAttendances(model, ALICE, targetClass, 2);
        recordAttendances(model, BENSON, targetClass, 5);
        new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).execute(model);

        assertThrows(CommandException.class, "The class index provided is invalid.", () ->
                new ListByClassCommand(Index.fromOneBased(2), Optional.of(3)).execute(model));

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
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
        assertThrows(NullPointerException.class, () -> new ListByClassCommand(null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new ListByClassCommand(Index.fromOneBased(1), null));
    }

    @Test
    public void equals() {
        ListByClassCommand command = new ListByClassCommand(Index.fromOneBased(1));
        ListByClassCommand commandWithThreshold =
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(5));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListByClassCommand(Index.fromOneBased(1))));
        assertTrue(command.equals(new ListByClassCommand(Index.fromOneBased(1), Optional.empty())));
        assertTrue(commandWithThreshold.equals(
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(5))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("CS2103"));
        assertFalse(command.equals(new ListByClassCommand(Index.fromOneBased(2))));
        assertFalse(command.equals(commandWithThreshold));
        assertFalse(commandWithThreshold.equals(
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(3))));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ListByClassCommand.class.getCanonicalName()
                        + "{index=seedu.address.commons.core.index.Index{zeroBasedIndex=0}, "
                        + "threshold=Optional.empty}",
                new ListByClassCommand(Index.fromOneBased(1)).toString());
        assertEquals(ListByClassCommand.class.getCanonicalName()
                        + "{index=seedu.address.commons.core.index.Index{zeroBasedIndex=0}, "
                        + "threshold=Optional[5]}",
                new ListByClassCommand(Index.fromOneBased(1), Optional.of(5)).toString());
    }

    /**
     * Creates a real academic class populated through the public membership API.
     */
    private static AcademicClass createClassWithMembers(String moduleCode, String className, Set<Person> members) {
        AcademicClass academicClass = new AcademicClass(new ModuleCode(moduleCode), new ClassName(className));
        members.forEach(academicClass::assignStudent);
        return academicClass;
    }

    /**
     * Records {@code count} distinct attendance dates in January 2024 for {@code person} in {@code academicClass}.
     */
    private static void recordAttendances(
            ModelManager model, Person person, AcademicClass academicClass, int count) {
        for (int day = 1; day <= count; day++) {
            model.addAttendance(person, academicClass, LocalDate.of(2024, 1, day));
        }
    }
}

