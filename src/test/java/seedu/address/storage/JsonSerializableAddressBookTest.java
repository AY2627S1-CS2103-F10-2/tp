package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndRead_historyWithSpecialCharacters_preservesStringsAndData() throws Exception {
        AddressBook source = new AddressBook();
        source.addPerson(ALICE);
        List<String> commands = List.of("find \"Alice\"", "find C:\\temp\\file",
                "find \"],\"persons\":[],\"injected\":\"", "find line\nnext\r\t\b\f"
                        + (char) 0 + (char) 27, "find ?? ??");
        source.setCommandHistory(commands);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("escaped-history.json"));
        storage.saveAddressBook(source);

        var restored = storage.readAddressBook().orElseThrow();
        assertEquals(commands, restored.getCommandHistory());
        assertEquals(List.of(ALICE), restored.getPersonList());
    }

    @Test
    public void toModelType_legacyFile_hasEmptyHistory() throws Exception {
        JsonSerializableAddressBook data = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).orElseThrow();
        assertEquals(List.of(), data.toModelType().getCommandHistory());
    }

    @Test
    public void toModelType_invalidHistory_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(), List.of(),
                List.of("list", " "));
        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void saveAndRead_memberships_preservesStoredStudentReferences() throws Exception {
        AddressBook source = new AddressBook();
        source.addPerson(ALICE);
        source.addPerson(BENSON);
        AcademicClass first = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AcademicClass second = new AcademicClass(new ModuleCode("CS2101"), new ClassName("W10"));
        AcademicClass empty = new AcademicClass(new ModuleCode("ST2334"), new ClassName("T01"));
        source.addAcademicClass(first);
        source.addAcademicClass(second);
        source.addAcademicClass(empty);
        source.assignStudentToClass(ALICE, first);
        source.assignStudentToClass(BENSON, first);
        source.assignStudentToClass(ALICE, first);
        source.assignStudentToClass(ALICE, second);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("assigned.json"));
        storage.saveAddressBook(source);

        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());
        AcademicClass restoredFirst = restored.findAcademicClass(first.getModuleCode(), first.getClassName())
                .orElseThrow();
        AcademicClass restoredSecond = restored.findAcademicClass(second.getModuleCode(), second.getClassName())
                .orElseThrow();
        assertEquals(Set.of(ALICE, BENSON), restoredFirst.getStudents());
        assertEquals(Set.of(ALICE), restoredSecond.getStudents());
        assertTrue(restored.findAcademicClass(empty.getModuleCode(), empty.getClassName())
                .orElseThrow().getStudents().isEmpty());
        for (Person person : restoredFirst.getStudents()) {
            assertSame(restored.findPersonByStudentNumber(person.getStudentNumber()).orElseThrow(), person);
        }
        assertSame(restored.findPersonByStudentNumber(ALICE.getStudentNumber()).orElseThrow(),
                restoredSecond.getStudents().iterator().next());
    }

    @Test
    public void toModelType_legacyClassWithoutMembership_loadsEmptyClass() throws Exception {
        String json = "{\"persons\":[],\"classes\":[{\"moduleCode\":\"CS2103T\",\"className\":\"F10-2\"}]}";
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(1, restored.getAcademicClassList().size());
        assertTrue(restored.getAcademicClassList().get(0).getStudents().isEmpty());
    }

    @Test
    public void toModelType_repeatedMembership_resolvesToOneStudent() throws Exception {
        JsonAdaptedAcademicClass academicClass = new JsonAdaptedAcademicClass("CS2103T", "F10-2",
                List.of(ALICE.getStudentNumber().value, "a0123456x"));
        AddressBook restored = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE)),
                List.of(academicClass)).toModelType();
        assertEquals(Set.of(ALICE), restored.getAcademicClassList().get(0).getStudents());
    }

    @Test
    public void toModelType_invalidMembership_throwsIllegalValueException() {
        for (String number : List.of("", "bad-id", "1234")) {
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE)),
                    List.of(new JsonAdaptedAcademicClass("CS2103T", "F10-2", List.of(number))));
            assertThrows(IllegalValueException.class, JsonAdaptedAcademicClass.MESSAGE_INVALID_STUDENT_NUMBER,
                    data::toModelType);
        }
        JsonSerializableAddressBook nullNumber = new JsonSerializableAddressBook(List.of(),
                List.of(new JsonAdaptedAcademicClass("CS2103T", "F10-2", java.util.Arrays.asList((String) null))));
        assertThrows(IllegalValueException.class, JsonAdaptedAcademicClass.MESSAGE_INVALID_STUDENT_NUMBER,
                nullNumber::toModelType);
    }

    @Test
    public void toModelType_unknownMembership_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE)),
                List.of(new JsonAdaptedAcademicClass("CS2103T", "F10-2", List.of("A9999999Z"))));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedAcademicClass.MESSAGE_STUDENT_NOT_FOUND, "A9999999Z"), data::toModelType);
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateEmailsIgnoringCase_throwsIllegalValueException() {
        Person first = new PersonBuilder().withStudentNumber("A1111111X").withEmail("alice@example.com").build();
        Person second = new PersonBuilder().withStudentNumber("A2222222X").withEmail("ALICE@example.com").build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_duplicateTelegramIgnoringCase_throwsIllegalValueException() {
        Person first = new PersonBuilder().withStudentNumber("A1111111X").withEmail("alice@example.com")
                .withTelegram("alice_tan").build();
        Person second = new PersonBuilder().withStudentNumber("A2222222X").withEmail("bob@example.com")
                .withTelegram("@ALICE_TAN").build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)), List.of());

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_validAcademicClasses_success() throws Exception {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        JsonSerializableAddressBook dataFromFile = new JsonSerializableAddressBook(List.of(),
                List.of(new JsonAdaptedAcademicClass(academicClass)));

        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(List.of(academicClass), addressBookFromFile.getAcademicClassList());
    }

    @Test
    public void toModelType_nullAcademicClasses_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = new JsonSerializableAddressBook(List.of(), null);

        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(List.of(), addressBookFromFile.getAcademicClassList());
    }

    @Test
    public void toModelType_duplicateAcademicClasses_throwsIllegalValueException() {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        JsonSerializableAddressBook dataFromFile = new JsonSerializableAddressBook(List.of(),
                List.of(new JsonAdaptedAcademicClass(academicClass), new JsonAdaptedAcademicClass(academicClass)));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_ACADEMIC_CLASS,
                dataFromFile::toModelType);
    }

    @Test
    public void jsonSerializableAddressBook_fromSource_preservesAcademicClasses() throws Exception {
        AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103T"), new ClassName("F10-2"));
        AddressBook source = new AddressBook();
        source.addAcademicClass(academicClass);
        JsonSerializableAddressBook jsonSerializableAddressBook = new JsonSerializableAddressBook(source);

        AddressBook addressBookFromSource = jsonSerializableAddressBook.toModelType();

        assertEquals(List.of(academicClass), addressBookFromSource.getAcademicClassList());
    }

}
