package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditClassCommand.EditClassDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.academicclass.exceptions.DuplicateAcademicClassException;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests class editing against the real model, memberships, observers and storage.
 */
public class EditClassCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final ModuleCode MODULE = new ModuleCode("CS2103");
    private static final ModuleCode NEW_MODULE = new ModuleCode("CS2101");
    private static final ClassName NAME = new ClassName("OB1");
    private static final ClassName NEW_NAME = new ClassName("OB5");

    private ModelManager model;
    private AcademicClass original;
    private AcademicClass other;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        original = new AcademicClass(MODULE, NAME);
        other = new AcademicClass(MODULE, new ClassName("OB2"));
        model.addAcademicClass(original);
        model.addAcademicClass(other);
        model.assignStudentToClass(ALICE, original);
        model.assignStudentToClass(BENSON, original);
        model.assignStudentToClass(ALICE, other);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditClassCommand(null, descriptor(null, NEW_NAME)));
        assertThrows(NullPointerException.class, () -> new EditClassCommand(FIRST, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(null, NEW_NAME).execute(null));
    }

    @Test
    public void execute_bothFieldsEdited_preservesMembershipsReferencesAndListPosition() throws Exception {
        CommandResult result = command(NEW_MODULE, NEW_NAME).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();

        assertEquals(new CommandResult(String.format(EditClassCommand.MESSAGE_EDIT_CLASS_SUCCESS, edited)), result);
        assertEquals(NEW_MODULE, edited.getModuleCode());
        assertEquals(NEW_NAME, edited.getClassName());
        assertNotSame(original, edited);
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
        assertTrue(edited.getStudents().stream().anyMatch(person -> person == ALICE));
        assertSame(other, model.getAcademicClassList().get(1));
        assertEquals(Set.of(ALICE), other.getStudents());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertTrue(model.findAcademicClass(MODULE, NAME).isEmpty());
        assertSame(edited, model.findAcademicClass(NEW_MODULE, NEW_NAME).orElseThrow());
    }

    @Test
    public void execute_moduleOnly_preservesNameAndMemberships() throws Exception {
        command(NEW_MODULE, null).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();
        assertEquals(NEW_MODULE, edited.getModuleCode());
        assertEquals(NAME, edited.getClassName());
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
    }

    @Test
    public void execute_classNameOnly_preservesModuleAndMemberships() throws Exception {
        command(null, NEW_NAME).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();
        assertEquals(MODULE, edited.getModuleCode());
        assertEquals(NEW_NAME, edited.getClassName());
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
    }

    @Test
    public void execute_sameNormalizedIdentity_preservesMemberships() throws Exception {
        command(new ModuleCode("cs2103"), new ClassName("ob1")).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();
        assertEquals(original, edited);
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
    }

    @Test
    public void execute_sameClassNameInDifferentModule_allowed() throws Exception {
        command(NEW_MODULE, other.getClassName()).execute(model);
        assertEquals(NEW_MODULE, model.getAcademicClassList().getFirst().getModuleCode());
        assertEquals(other.getClassName(), model.getAcademicClassList().getFirst().getClassName());
        assertSame(other, model.getAcademicClassList().get(1));
    }

    @Test
    public void execute_duplicateHiddenByFilter_rejectsWithoutChangingDataOrViews() {
        model.updateFilteredAcademicClassList(academicClass -> academicClass == original);
        model.updateFilteredPersonList(person -> person == BENSON);

        assertThrows(CommandException.class, EditClassCommand.MESSAGE_DUPLICATE_CLASS, () ->
                command(other.getModuleCode(), other.getClassName()).execute(model));

        assertSame(original, model.getAcademicClassList().getFirst());
        assertEquals(Set.of(ALICE, BENSON), original.getStudents());
        assertEquals(List.of(original), model.getFilteredAcademicClassList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        model.deletePerson(ALICE);
        assertEquals(Set.of(BENSON), original.getStudents());
    }

    @Test
    public void execute_outOfBoundsIndex_throwsClassSpecificError() {
        EditClassCommand command = new EditClassCommand(Index.fromOneBased(3), descriptor(null, NEW_NAME));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX, () ->
                command.execute(model));
        assertSame(original, model.getAcademicClassList().getFirst());
        assertEquals(Set.of(ALICE, BENSON), original.getStudents());
    }

    @Test
    public void execute_emptyDisplayedClassList_throwsClassSpecificError() {
        model.updateFilteredAcademicClassList(academicClass -> false);
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX, () ->
                command(null, NEW_NAME).execute(model));
        assertTrue(model.getFilteredAcademicClassList().isEmpty());
    }

    @Test
    public void execute_filteredIndex_editsDisplayedClassAndPreservesViews() throws Exception {
        model.updateFilteredAcademicClassList(academicClass -> academicClass == other);
        model.updateFilteredPersonListAlphabetically(person -> false);

        command(null, NEW_NAME).execute(model);

        assertSame(original, model.getAcademicClassList().getFirst());
        assertEquals(NEW_NAME, model.getAcademicClassList().get(1).getClassName());
        assertEquals(Set.of(ALICE), model.getAcademicClassList().get(1).getStudents());
        assertTrue(model.getFilteredAcademicClassList().isEmpty());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_afterListByClassWithThreshold_preservesThresholdOrderAttendanceContextAndUpdates()
            throws Exception {
        model.addPerson(DANIEL);
        model.assignStudentToClass(DANIEL, original);
        // ALICE: 4 dates (below t/5), BENSON: 5 dates (excluded by t/5), DANIEL: 1 date (below t/5)
        recordAttendances(model, ALICE, original, 4);
        recordAttendances(model, BENSON, original, 5);
        recordAttendances(model, DANIEL, original, 1);

        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE, DANIEL), model.getFilteredPersonList());

        command(null, NEW_NAME).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();

        // Only four-date (ALICE) and one-date (DANIEL) students remain displayed in alphabetical order
        assertEquals(List.of(ALICE, DANIEL), model.getFilteredPersonList());
        assertEquals(Optional.of(edited), model.getAttendanceDisplayClass());

        // Further attendance changes on the renamed class update the threshold-filtered view
        model.addAttendance(ALICE, edited, LocalDate.of(2024, 2, 10));
        assertEquals(List.of(DANIEL), model.getFilteredPersonList());

        model.removeAttendance(BENSON, edited, LocalDate.of(2024, 1, 5));
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_moduleCodeChangeWithThreshold_preservesThreshold() throws Exception {
        recordAttendances(model, ALICE, original, 4);
        recordAttendances(model, BENSON, original, 5);

        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        command(NEW_MODULE, null).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(Optional.of(edited), model.getAttendanceDisplayClass());
    }

    @Test
    public void execute_unrelatedClassEditAndClassFilterChange_preservesThresholdOnViewedClass() throws Exception {
        recordAttendances(model, ALICE, original, 4);
        recordAttendances(model, BENSON, original, 5);

        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Changing only the class-list filter preserves the active threshold on the student list
        model.updateFilteredAcademicClassListByStudent(ALICE.getStudentNumber());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Editing an unrelated class (other is at displayed index 2) preserves threshold on original
        new EditClassCommand(Index.fromOneBased(2), descriptor(null, NEW_NAME)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(Optional.of(original), model.getAttendanceDisplayClass());

        // Subsequent rename of the viewed class still preserves the threshold
        command(null, new ClassName("OB9")).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_rejectedEditWithThreshold_preservesThreshold() throws Exception {
        recordAttendances(model, ALICE, original, 4);
        recordAttendances(model, BENSON, original, 5);

        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Rejected duplicate class edit preserves active threshold
        assertThrows(CommandException.class, EditClassCommand.MESSAGE_DUPLICATE_CLASS, () ->
                command(other.getModuleCode(), other.getClassName()).execute(model));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Rejected invalid index edit preserves active threshold
        EditClassCommand invalidIndexCommand =
                new EditClassCommand(Index.fromOneBased(5), descriptor(null, NEW_NAME));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX, () ->
                invalidIndexCommand.execute(model));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        // Subsequent valid rename still applies the preserved threshold
        command(null, NEW_NAME).execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_thresholdClearedWhenLeavingClassViewOrPlainListByClass() throws Exception {
        recordAttendances(model, ALICE, original, 4);
        recordAttendances(model, BENSON, original, 5);

        // Plain listbyclass clears threshold
        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        new ListByClassCommand(FIRST).execute(model);
        command(null, NEW_NAME).execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());

        // Leaving via list clears threshold
        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        new ListCommand().execute(model);
        command(null, new ClassName("OB6")).execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());

        // Leaving via student add clears threshold
        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        new AddCommand(CARL).execute(model);
        command(null, new ClassName("OB7")).execute(model);
        assertEquals(List.of(ALICE, BENSON, CARL), model.getFilteredPersonList());

        // Leaving via student edit clears threshold
        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        EditCommand.EditPersonDescriptor editPersonDescriptor = new EditCommand.EditPersonDescriptor();
        editPersonDescriptor.setName(ALICE.getName());
        new EditCommand(FIRST, editPersonDescriptor).execute(model);
        command(null, new ClassName("OB8")).execute(model);
        assertEquals(List.of(ALICE, BENSON, CARL), model.getFilteredPersonList());

        // Leaving via clear resets everything
        new ListByClassCommand(FIRST, Optional.of(5)).execute(model);
        new ClearCommand().execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_afterListByClass_followsEditedClassFilter() throws Exception {
        new ListByClassCommand(Index.fromOneBased(1)).execute(model);
        command(null, NEW_NAME).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();
        assertEquals(Optional.of(edited), model.getAttendanceDisplayClass());
        model.unassignStudentFromClass(ALICE, edited);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(Set.of(BENSON), edited.getStudents());
    }

    @Test
    public void execute_viewingOtherClass_preservesSelectedClass() throws Exception {
        model.filterPersonsByClass(other);
        command(null, NEW_NAME).execute(model);
        model.assignStudentToClass(BENSON, other);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        model.unassignStudentFromClass(ALICE, other);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterFind_preservesNameFilter() throws Exception {
        model.filterPersonsByClass(original);
        model.updateFilteredPersonList(person -> person == BENSON);
        command(null, NEW_NAME).execute(model);
        model.unassignStudentFromClass(BENSON, model.getAcademicClassList().getFirst());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void setAcademicClass_duplicate_preservesActiveClassFilter() {
        model.filterPersonsByClass(original);
        assertThrows(DuplicateAcademicClassException.class, () ->
                model.setAcademicClass(original, other));
        model.unassignStudentFromClass(ALICE, original);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_renameTwice_followsLatestClassMembership() throws Exception {
        model.filterPersonsByClass(original);
        command(null, NEW_NAME).execute(model);
        command(NEW_MODULE, new ClassName("OB6")).execute(model);
        model.unassignStudentFromClass(ALICE, model.getAcademicClassList().getFirst());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_renamedClass_receivesStudentEditsAndDeletionsInsteadOfOldClass() throws Exception {
        command(null, NEW_NAME).execute(model);
        AcademicClass edited = model.getAcademicClassList().getFirst();
        Person updatedAlice = new PersonBuilder(ALICE).withStudentNumber("A8888883K").build();

        model.setPerson(ALICE, updatedAlice);

        assertTrue(edited.getStudents().stream().anyMatch(person -> person == updatedAlice));
        assertEquals(Set.of(ALICE, BENSON), original.getStudents());
        model.deletePerson(updatedAlice);
        assertEquals(Set.of(BENSON), edited.getStudents());
        assertTrue(other.getStudents().isEmpty());
        assertEquals(Set.of(ALICE, BENSON), original.getStudents());
    }

    @Test
    public void execute_renameTwice_unregistersIntermediateClass() throws Exception {
        command(null, NEW_NAME).execute(model);
        AcademicClass intermediate = model.getAcademicClassList().getFirst();
        command(NEW_MODULE, new ClassName("OB6")).execute(model);
        model.deletePerson(ALICE);
        assertEquals(Set.of(ALICE, BENSON), intermediate.getStudents());
        assertEquals(Set.of(BENSON), model.getAcademicClassList().getFirst().getStudents());
    }

    @Test
    public void execute_modifiedOriginalDescriptor_doesNotChangeCommand() throws Exception {
        EditClassDescriptor edits = descriptor(null, NEW_NAME);
        EditClassCommand command = new EditClassCommand(FIRST, edits);
        edits.setClassName(other.getClassName());
        command.execute(model);
        assertEquals(NEW_NAME, model.getAcademicClassList().getFirst().getClassName());
    }

    @Test
    public void execute_savedAndReloaded_preservesIdentityAndMemberships(@TempDir Path directory) throws Exception {
        command(NEW_MODULE, NEW_NAME).execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        AcademicClass edited = reloaded.findAcademicClass(NEW_MODULE, NEW_NAME).orElseThrow();
        assertEquals(Set.of(ALICE, BENSON), edited.getStudents());
        assertTrue(reloaded.findAcademicClass(MODULE, NAME).isEmpty());
        Person storedAlice = reloaded.findPersonByStudentNumber(ALICE.getStudentNumber()).orElseThrow();
        assertTrue(edited.getStudents().stream().anyMatch(person -> person == storedAlice));
        reloaded.deletePerson(storedAlice);
        assertEquals(Set.of(BENSON), edited.getStudents());
    }

    @Test
    public void equals_comparesIndexAndDescriptor() {
        EditClassCommand command = command(null, NEW_NAME);
        assertEquals(command, command);
        assertEquals(command(null, NEW_NAME), command);
        assertFalse(command.equals(null));
        assertFalse(command.equals("editc"));
        assertFalse(command.equals(new EditClassCommand(Index.fromOneBased(2), descriptor(null, NEW_NAME))));
        assertFalse(command.equals(command(NEW_MODULE, null)));
    }

    @Test
    public void toString_containsTargetAndEdits() {
        EditClassDescriptor edits = descriptor(NEW_MODULE, NEW_NAME);
        String expected = EditClassCommand.class.getCanonicalName() + "{index=" + FIRST
                + ", editClassDescriptor=" + edits + "}";
        assertEquals(expected, new EditClassCommand(FIRST, edits).toString());
    }

    private static void recordAttendances(
            ModelManager model, Person person, AcademicClass academicClass, int count) {
        for (int day = 1; day <= count; day++) {
            model.addAttendance(person, academicClass, LocalDate.of(2024, 1, day));
        }
    }

    private EditClassCommand command(ModuleCode moduleCode, ClassName className) {
        return new EditClassCommand(FIRST, descriptor(moduleCode, className));
    }

    private EditClassDescriptor descriptor(ModuleCode moduleCode, ClassName className) {
        EditClassDescriptor edits = new EditClassDescriptor();
        edits.setModuleCode(moduleCode);
        edits.setClassName(className);
        return edits;
    }
}
