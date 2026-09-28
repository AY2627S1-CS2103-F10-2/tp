package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_anyValue_preservesValue() {
        assertEquals("Likes baseball", new Remark("Likes baseball").value);
        assertEquals("", new Remark("").value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes baseball");

        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Likes baseball")));
        assertFalse(remark.equals(new Remark("Likes swimming")));
        assertFalse(remark.equals(1));
        assertFalse(remark.equals(null));
    }
}
