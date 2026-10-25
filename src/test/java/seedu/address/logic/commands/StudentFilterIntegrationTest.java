package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests student mutations against real filtered model views.
 */
public class StudentFilterIntegrationTest {
    private final ModelManager model = new ModelManager();
    private final AcademicClass academicClass =
            new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));

    @BeforeEach
    public void setUp() {
        model.addPerson(BENSON);
        model.addPerson(ALICE);
        model.addAcademicClass(academicClass);
        model.assignStudentToClass(ALICE, academicClass);
        model.assignStudentToClass(BENSON, academicClass);
    }

    @Test
    public void execute_addMatchingStudent_preservesSearch() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Meier")));

        new AddCommand(DANIEL).execute(model);

        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_addNonmatchingStudent_preservesEmptySearch() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Nobody")));

        new AddCommand(DANIEL).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE, DANIEL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addUnassignedStudent_preservesClassViewAndAttendance() throws Exception {
        model.filterPersonsByClassAndDisplayAttendance(academicClass, Optional.empty());

        new AddCommand(DANIEL).execute(model);

        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        assertEquals(Optional.of(academicClass), model.getAttendanceDisplayClass());
    }

    @Test
    public void execute_editNameNoLongerMatching_preservesEmptySearch() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName("Zoe Pauline").build()).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(new PersonBuilder(ALICE).withName("Zoe Pauline").build(),
                model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void execute_editClassMemberName_preservesSortedViewAndAttendance() throws Exception {
        model.filterPersonsByClassAndDisplayAttendance(academicClass, Optional.empty());
        Person edited = new PersonBuilder(ALICE).withName("Zoe Pauline").build();

        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName("Zoe Pauline").build()).execute(model);

        assertEquals(List.of(BENSON, edited), model.getFilteredPersonList());
        assertEquals(Optional.of(academicClass), model.getAttendanceDisplayClass());
    }

    @Test
    public void setPerson_newStudentNumber_refreshesClassMembership() {
        model.filterPersonsByClass(academicClass);
        Person edited = new PersonBuilder(ALICE).withStudentNumber("A0999999Z").build();

        model.setPerson(ALICE, edited);

        assertEquals(List.of(edited, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void setPerson_newStudentNumber_preservesAttendanceThresholdAfterRefresh() {
        model.addAttendance(ALICE, academicClass, LocalDate.of(2020, 1, 1));
        model.filterPersonsByClassAndDisplayAttendance(academicClass, Optional.of(1));
        Person edited = new PersonBuilder(ALICE).withStudentNumber("A0999999Z").build();

        model.setPerson(ALICE, edited);
        model.refreshFilteredPersonList();

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(Optional.of(academicClass), model.getAttendanceDisplayClass());
    }

    @Test
    public void execute_deleteFilteredStudent_doesNotRevealOtherStudents() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_deleteClassMember_preservesAttendanceContext() throws Exception {
        model.filterPersonsByClassAndDisplayAttendance(academicClass, Optional.empty());

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(Optional.of(academicClass), model.getAttendanceDisplayClass());
    }

    @Test
    public void execute_invalidEditIndex_preservesSearchAndData() {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        assertThrows(CommandException.class, () -> new EditCommand(INDEX_SECOND_PERSON,
                new EditPersonDescriptorBuilder().withName("Zoe").build()).execute(model));

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ALICE), model.getAddressBook().getPersonList());
    }
}
