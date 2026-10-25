package seedu.address.logic;

import java.util.List;
import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the complete list of persons. */
    ObservableList<Person> getPersonList();

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the class whose attendance is displayed beside the person list, if any. */
    Optional<AcademicClass> getAttendanceDisplayClass();

    /** Returns attendance records used to render class attendance counts. */
    List<Attendance> getAttendanceList();

    /** Returns an unmodifiable view of the list of academic classes */
    ObservableList<AcademicClass> getAcademicClassList();

    /** Returns an unmodifiable view of the filtered academic class list. */
    ObservableList<AcademicClass> getFilteredAcademicClassList();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
