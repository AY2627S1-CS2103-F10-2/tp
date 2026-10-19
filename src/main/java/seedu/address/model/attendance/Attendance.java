package seedu.address.model.attendance;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.person.Person;

/**
 * Records attended dates for one student-class association.
 * The owning application model is responsible for membership and association uniqueness.
 */
public class Attendance {
    private final Person student;
    private final AcademicClass academicClass;
    private final Clock clock;
    private final Set<LocalDate> attendedDates = new TreeSet<>();
    private final Set<LocalDate> readOnlyDates = Collections.unmodifiableSet(attendedDates);

    /** Creates empty attendance using the computer's local calendar date. */
    public Attendance(Person student, AcademicClass academicClass) {
        this(student, academicClass, Clock.systemDefaultZone());
    }

    /**
     * Creates empty attendance using the supplied clock to determine today.
     * All arguments must be non-null.
     */
    public Attendance(Person student, AcademicClass academicClass, Clock clock) {
        requireAllNonNull(student, academicClass, clock);
        this.student = student;
        this.academicClass = academicClass;
        this.clock = clock;
    }

    /**
     * Restores previously recorded dates without comparing them with today's date.
     * A timezone change or clock rollback must not invalidate saved history.
     * Storage must validate date syntax and reject duplicates before calling this method.
     * New user entries must use {@link #addDate(LocalDate)} instead.
     *
     * @throws NullPointerException if an argument or a recorded date is null
     */
    public static Attendance fromRecordedDates(Person student, AcademicClass academicClass,
            Set<LocalDate> recordedDates) {
        requireNonNull(recordedDates);
        Attendance attendance = new Attendance(student, academicClass);
        for (LocalDate date : recordedDates) {
            attendance.attendedDates.add(requireNonNull(date));
        }
        return attendance;
    }

    public Person getStudent() {
        return student;
    }

    public AcademicClass getAcademicClass() {
        return academicClass;
    }

    /** Returns a live, read-only view of attended dates in chronological order. */
    public Set<LocalDate> getAttendedDates() {
        return readOnlyDates;
    }

    /** Returns the count derived from the unique attended dates. */
    public int getAttendanceCount() {
        return attendedDates.size();
    }

    /** Copies recorded history to the supplied endpoints without revalidating historical dates. */
    public Attendance copyFor(Person student, AcademicClass academicClass) {
        Attendance copy = new Attendance(student, academicClass, clock);
        copy.attendedDates.addAll(attendedDates);
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Attendance otherAttendance)) {
            return false;
        }
        return student.equals(otherAttendance.student)
                && academicClass.equals(otherAttendance.academicClass)
                && attendedDates.equals(otherAttendance.attendedDates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student, academicClass, attendedDates);
    }

    /**
     * Records a date, returning false if it is already recorded.
     * @throws NullPointerException if the date is null.
     * @throws IllegalArgumentException if the date is after today.
     */
    public boolean addDate(LocalDate date) {
        validateDate(date);
        return attendedDates.add(date);
    }

    /**
     * Removes only the specified date, returning false if it is not recorded.
     * @throws NullPointerException if the date is null.
     * @throws IllegalArgumentException if the date is after today.
     */
    public boolean removeDate(LocalDate date) {
        validateDate(date);
        return attendedDates.remove(date);
    }

    private void validateDate(LocalDate date) {
        requireNonNull(date);
        if (date.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Attendance date must not be in the future.");
        }
    }
}
