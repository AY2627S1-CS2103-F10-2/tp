package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;

public class AddClassCommandTest {

    private static final AcademicClass CS2103_F10 = new AcademicClass(new ModuleCode("CS2103"),
            new ClassName("F10-2"));
    private static final AcademicClass ST2334_T24 = new AcademicClass(new ModuleCode("ST2334"), new ClassName("T24"));

    @Test
    public void constructor_nullAcademicClass_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddClassCommand(null));
    }

    @Test
    public void execute_academicClassAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingAcademicClassAdded modelStub = new ModelStubAcceptingAcademicClassAdded();

        CommandResult commandResult = new AddClassCommand(CS2103_F10).execute(modelStub);

        assertEquals(String.format(AddClassCommand.MESSAGE_SUCCESS, CS2103_F10), commandResult.getFeedbackToUser());
        assertEquals(List.of(CS2103_F10), modelStub.academicClassesAdded);
    }

    @Test
    public void execute_duplicateAcademicClass_throwsCommandException() {
        AddClassCommand addClassCommand = new AddClassCommand(CS2103_F10);
        ModelStub modelStub = new ModelStubWithAcademicClass(CS2103_F10);

        assertThrows(CommandException.class, AddClassCommand.MESSAGE_DUPLICATE_CLASS, () ->
                addClassCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        AddClassCommand addCs2103Command = new AddClassCommand(CS2103_F10);
        AddClassCommand addSt2334Command = new AddClassCommand(ST2334_T24);

        // same object -> returns true
        assertTrue(addCs2103Command.equals(addCs2103Command));

        // same values -> returns true
        AddClassCommand addCs2103CommandCopy = new AddClassCommand(CS2103_F10);
        assertTrue(addCs2103Command.equals(addCs2103CommandCopy));

        // different types -> returns false
        assertFalse(addCs2103Command.equals(1));

        // null -> returns false
        assertFalse(addCs2103Command.equals(null));

        // different academic class -> returns false
        assertFalse(addCs2103Command.equals(addSt2334Command));
    }

    @Test
    public void toStringMethod() {
        AddClassCommand addClassCommand = new AddClassCommand(CS2103_F10);
        String expected = AddClassCommand.class.getCanonicalName() + "{toAdd=" + CS2103_F10 + "}";
        assertEquals(expected, addClassCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook addressBook) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasAcademicClass(AcademicClass academicClass) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addAcademicClass(AcademicClass academicClass) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<AcademicClass> getAcademicClassList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single academic class.
     */
    private class ModelStubWithAcademicClass extends ModelStub {
        private final AcademicClass academicClass;

        ModelStubWithAcademicClass(AcademicClass academicClass) {
            requireNonNull(academicClass);
            this.academicClass = academicClass;
        }

        @Override
        public boolean hasAcademicClass(AcademicClass academicClass) {
            requireNonNull(academicClass);
            return this.academicClass.equals(academicClass);
        }
    }

    /**
     * A Model stub that always accepts the academic class being added.
     */
    private class ModelStubAcceptingAcademicClassAdded extends ModelStub {
        final ArrayList<AcademicClass> academicClassesAdded = new ArrayList<>();

        @Override
        public boolean hasAcademicClass(AcademicClass academicClass) {
            requireNonNull(academicClass);
            return academicClassesAdded.contains(academicClass);
        }

        @Override
        public void addAcademicClass(AcademicClass academicClass) {
            requireNonNull(academicClass);
            academicClassesAdded.add(academicClass);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }
}
