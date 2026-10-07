package seedu.address.model.academicclass.exceptions;

/**
 * Signals that the operation will result in classes with the same module code and class name.
 */
public class DuplicateAcademicClassException extends RuntimeException {
    public DuplicateAcademicClassException() {
        super("Operation would result in duplicate academic classes");
    }
}
