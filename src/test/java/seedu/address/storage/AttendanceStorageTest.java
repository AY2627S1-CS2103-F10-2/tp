package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Tests attendance through the real JSON storage boundary. */
public class AttendanceStorageTest {
    private static final LocalDate DATE = LocalDate.of(2000, 1, 1);
    @TempDir
    public Path temporaryFolder;

    private final AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));

    @Test
    public void saveAndRead_unassignedHistory_preservesDatesAndCanonicalReferences() throws Exception {
        AddressBook source = createAddressBook();
        source.assignStudentToClass(ALICE, academicClass);
        source.addAttendance(ALICE, academicClass, DATE);
        source.addAttendance(ALICE, academicClass, DATE.plusDays(1));
        source.unassignStudentFromClass(ALICE, academicClass);
        AddressBook restored = roundTrip(source);
        assertEquals(source, restored);
        assertEquals(Set.of(DATE, DATE.plusDays(1)), restored.getAttendanceList().get(0).getAttendedDates());
        assertSame(restored.getPersonList().get(0), restored.getAttendanceList().get(0).getStudent());
        assertSame(restored.getAcademicClassList().get(0), restored.getAttendanceList().get(0).getAcademicClass());
        assertTrue(restored.getAcademicClassList().get(0).getStudents().isEmpty());
    }

    @Test
    public void saveAndRead_emptyAssociation_preservesZeroCount() throws Exception {
        AddressBook source = createAddressBook();
        source.addAttendance(ALICE, academicClass, DATE);
        source.removeAttendance(ALICE, academicClass, DATE);
        AddressBook restored = roundTrip(source);
        assertEquals(source, restored);
        assertEquals(1, restored.getAttendanceList().size());
        assertEquals(0, restored.getAttendanceList().get(0).getAttendanceCount());
    }

    @Test
    public void saveAndRead_afterStudentEdit_preservesHistory() throws Exception {
        AddressBook source = createAddressBook();
        source.addAttendance(ALICE, academicClass, DATE);
        Person edited = new PersonBuilder(ALICE).withStudentNumber("A9999999X").build();
        source.setPerson(ALICE, edited);
        AddressBook restored = roundTrip(source);
        assertEquals(edited, restored.getAttendanceList().get(0).getStudent());
        assertEquals(Set.of(DATE), restored.getAttendanceList().get(0).getAttendedDates());
    }

    @Test
    public void saveAndRead_afterAssignedStudentEdit_preservesCanonicalReferences() throws Exception {
        AddressBook source = createAddressBook();
        source.assignStudentToClass(ALICE, academicClass);
        source.addAttendance(ALICE, academicClass, DATE);
        Person edited = new PersonBuilder(ALICE).withStudentNumber("A9999999X").withName("Updated Alice").build();

        source.setPerson(ALICE, edited);

        assertEquals(Set.of(edited), academicClass.getStudents());
        assertSame(edited, academicClass.getStudents().iterator().next());
        assertSame(edited, source.getAttendanceList().get(0).getStudent());
        assertEquals(Set.of(DATE), source.getAttendanceList().get(0).getAttendedDates());

        AddressBook restored = roundTrip(source);
        Person storedStudent = restored.findPersonByStudentNumber(edited.getStudentNumber()).orElseThrow();
        AcademicClass storedClass = restored.getAcademicClassList().get(0);
        assertEquals(edited, storedStudent);
        assertTrue(restored.findPersonByStudentNumber(ALICE.getStudentNumber()).isEmpty());
        assertEquals(Set.of(storedStudent), storedClass.getStudents());
        assertSame(storedStudent, storedClass.getStudents().iterator().next());
        assertEquals(1, restored.getAttendanceList().size());
        assertSame(storedStudent, restored.getAttendanceList().get(0).getStudent());
        assertSame(storedClass, restored.getAttendanceList().get(0).getAcademicClass());
        assertEquals(Set.of(DATE), restored.getAttendanceList().get(0).getAttendedDates());
    }

    @Test
    public void saveAndRead_afterEndpointDeletion_doesNotRestoreHistory() throws Exception {
        AddressBook source = createAddressBook();
        source.addAttendance(ALICE, academicClass, DATE);
        source.removePerson(ALICE);
        assertEquals(List.of(), roundTrip(source).getAttendanceList());
        source.addPerson(ALICE);
        source.addAttendance(ALICE, academicClass, DATE);
        source.removeAcademicClass(academicClass);
        assertEquals(List.of(), roundTrip(source).getAttendanceList());
    }

    @Test
    public void toModelType_legacyFileWithoutAttendance_loadsEmptyHistory() throws Exception {
        String json = "{\"persons\":[],\"classes\":[{\"moduleCode\":\"CS2103\",\"className\":\"F10-2\"}]}";
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(1, restored.getAcademicClassList().size());
        assertEquals(List.of(), restored.getAttendanceList());
    }

    @Test
    public void toModelType_invalidDates_rejectsRecord() {
        for (String date : Arrays.asList(null, "", "2000-02-30", "2000-1-01", "01/01/2000",
                "2000-01-01T00:00:00", "9999-12-31")) {
            JsonAdaptedAttendance record = new JsonAdaptedAttendance(ALICE.getStudentNumber().value,
                    "CS2103", "F10-2", Arrays.asList(date));
            assertThrows(IllegalValueException.class, () -> dataWithAttendance(List.of(record)).toModelType());
        }
    }

    @Test
    public void toModelType_missingFields_rejectsRecord() {
        for (JsonAdaptedAttendance record : List.of(
                new JsonAdaptedAttendance(null, "CS2103", "F10-2", List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, null, "F10-2", List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, "CS2103", null, List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, "CS2103", "F10-2", null))) {
            assertThrows(IllegalValueException.class, () -> dataWithAttendance(List.of(record)).toModelType());
        }
    }

    @Test
    public void toModelType_invalidOrMissingEndpoints_rejectsRecord() {
        for (JsonAdaptedAttendance record : List.of(
                new JsonAdaptedAttendance("bad", "CS2103", "F10-2", List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, "!", "F10-2", List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, "CS2103", "!", List.of()),
                new JsonAdaptedAttendance("A9999999X", "CS2103", "F10-2", List.of()),
                new JsonAdaptedAttendance(ALICE.getStudentNumber().value, "CS2101", "T01", List.of()))) {
            assertThrows(IllegalValueException.class, () -> dataWithAttendance(List.of(record)).toModelType());
        }
    }

    @Test
    public void toModelType_duplicateDates_rejectsRecord() {
        JsonAdaptedAttendance record = new JsonAdaptedAttendance(ALICE.getStudentNumber().value,
                "CS2103", "F10-2", List.of("2000-01-01", "2000-01-01"));
        assertThrows(IllegalValueException.class, () -> dataWithAttendance(List.of(record)).toModelType());
    }

    @Test
    public void toModelType_duplicatePairsIgnoringCase_rejectsRecords() {
        JsonAdaptedAttendance first = new JsonAdaptedAttendance(ALICE.getStudentNumber().value,
                "CS2103", "F10-2", List.of("2000-01-01"));
        JsonAdaptedAttendance second = new JsonAdaptedAttendance(ALICE.getStudentNumber().value.toLowerCase(),
                "cs2103", "f10-2", List.of("2000-01-02"));
        assertThrows(IllegalValueException.class, () -> dataWithAttendance(List.of(first, second)).toModelType());
    }

    @Test
    public void toModelType_nullRecord_rejectsRecord() {
        assertThrows(IllegalValueException.class, () -> dataWithAttendance(
                Arrays.asList((JsonAdaptedAttendance) null)).toModelType());
    }

    @Test
    public void serializationSnapshot_laterModelChange_doesNotChangeSavedHistory() throws Exception {
        AddressBook source = createAddressBook();
        source.addAttendance(ALICE, academicClass, DATE);
        JsonSerializableAddressBook snapshot = new JsonSerializableAddressBook(source);
        source.removeAttendance(ALICE, academicClass, DATE);
        assertEquals(Set.of(DATE), snapshot.toModelType().getAttendanceList().get(0).getAttendedDates());
    }

    private JsonSerializableAddressBook dataWithAttendance(List<JsonAdaptedAttendance> records) {
        return new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE)),
                List.of(new JsonAdaptedAcademicClass(academicClass)), records);
    }

    @Test
    public void read_invalidAttendance_reportsLoadingFailureWithoutChangingFile() throws Exception {
        Path path = temporaryFolder.resolve("invalid-attendance.json");
        JsonAdaptedAttendance invalid = new JsonAdaptedAttendance(ALICE.getStudentNumber().value,
                "CS2103", "F10-2", List.of("2000-02-30"));
        JsonUtil.saveJsonFile(dataWithAttendance(List.of(invalid)), path);
        String original = Files.readString(path);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(original, Files.readString(path));
    }

    @Test
    public void saveAndRead_earliestModelDate_preservesHistory() throws Exception {
        AddressBook source = createAddressBook();
        source.addAttendance(ALICE, academicClass, LocalDate.MIN);
        assertEquals(source, roundTrip(source));
    }

    private AddressBook createAddressBook() {
        AddressBook source = new AddressBook();
        source.addPerson(ALICE);
        source.addAcademicClass(academicClass);
        return source;
    }

    private AddressBook roundTrip(AddressBook source) throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("attendance.json"));
        storage.saveAddressBook(source);
        return new AddressBook(storage.readAddressBook().orElseThrow());
    }
}
