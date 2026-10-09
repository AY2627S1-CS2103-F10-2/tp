package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

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
