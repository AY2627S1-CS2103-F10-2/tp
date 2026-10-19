package seedu.address.model.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/** Tests date-based attendance for an individual student-class association. */
public class AttendanceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private final AcademicClass academicClass = new AcademicClass(new ModuleCode("CS2103"), new ClassName("F10-2"));
    private final Attendance attendance = new Attendance(ALICE, academicClass, CLOCK);

    @Test
    public void constructor_validPair_startsEmpty() {
        assertEquals(ALICE, attendance.getStudent());
        assertEquals(academicClass, attendance.getAcademicClass());
        assertEquals(Set.of(), attendance.getAttendedDates());
        assertEquals(0, attendance.getAttendanceCount());
    }

    @Test
    public void constructor_nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Attendance(null, academicClass, CLOCK));
        assertThrows(NullPointerException.class, () -> new Attendance(ALICE, null, CLOCK));
        assertThrows(NullPointerException.class, () -> new Attendance(ALICE, academicClass, null));
    }

    @Test
    public void addDate_pastAndToday_recordsDatesAndDerivesCount() {
        assertTrue(attendance.addDate(TODAY.minusDays(7)));
        assertTrue(attendance.addDate(TODAY));
        assertEquals(Set.of(LocalDate.of(2026, 10, 12), TODAY), attendance.getAttendedDates());
        assertEquals(2, attendance.getAttendanceCount());
    }

    @Test
    public void addDate_duplicate_preservesDates() {
        attendance.addDate(TODAY);
        assertFalse(attendance.addDate(TODAY));
        assertEquals(Set.of(TODAY), attendance.getAttendedDates());
        assertEquals(1, attendance.getAttendanceCount());
    }

    @Test
    public void addDate_future_preservesExistingDates() {
        attendance.addDate(TODAY);
        assertThrows(IllegalArgumentException.class, () -> attendance.addDate(TODAY.plusDays(1)));
        assertEquals(Set.of(TODAY), attendance.getAttendedDates());
    }

    @Test
    public void addDate_clockZone_determinesToday() {
        Attendance utcAttendance = new Attendance(ALICE, academicClass, CLOCK.withZone(ZoneId.of("UTC")));
        assertTrue(attendance.addDate(TODAY));
        assertThrows(IllegalArgumentException.class, () -> utcAttendance.addDate(TODAY));
        assertTrue(utcAttendance.addDate(TODAY.minusDays(1)));
    }

    @Test
    public void removeDate_existing_removesOnlySpecifiedDate() {
        attendance.addDate(TODAY.minusDays(1));
        attendance.addDate(TODAY);
        assertTrue(attendance.removeDate(TODAY));
        assertEquals(Set.of(TODAY.minusDays(1)), attendance.getAttendedDates());
        assertEquals(1, attendance.getAttendanceCount());
        assertTrue(attendance.removeDate(TODAY.minusDays(1)));
        assertEquals(0, attendance.getAttendanceCount());
    }

    @Test
    public void removeDate_missing_preservesDates() {
        attendance.addDate(TODAY);
        assertFalse(attendance.removeDate(TODAY.minusDays(1)));
        assertEquals(Set.of(TODAY), attendance.getAttendedDates());
    }

    @Test
    public void removeDate_future_preservesDates() {
        attendance.addDate(TODAY);
        assertThrows(IllegalArgumentException.class, () -> attendance.removeDate(TODAY.plusDays(1)));
        assertEquals(Set.of(TODAY), attendance.getAttendedDates());
    }

    @Test
    public void changeDate_null_preservesDates() {
        attendance.addDate(TODAY);
        assertThrows(NullPointerException.class, () -> attendance.addDate(null));
        assertThrows(NullPointerException.class, () -> attendance.removeDate(null));
        assertEquals(Set.of(TODAY), attendance.getAttendedDates());
    }

    @Test
    public void getAttendedDates_mutation_rejectedButViewReflectsChanges() {
        Set<LocalDate> dates = attendance.getAttendedDates();
        assertThrows(UnsupportedOperationException.class, () -> dates.add(TODAY));
        attendance.addDate(TODAY);
        assertEquals(Set.of(TODAY), dates);
        assertThrows(UnsupportedOperationException.class, () -> dates.remove(TODAY));
        assertThrows(UnsupportedOperationException.class, dates::clear);
        assertEquals(1, attendance.getAttendanceCount());
        attendance.removeDate(TODAY);
        assertEquals(Set.of(), dates);
    }

    @Test
    public void getAttendedDates_addedOutOfOrder_iteratesChronologically() {
        attendance.addDate(TODAY);
        attendance.addDate(TODAY.minusDays(7));
        attendance.addDate(TODAY.minusDays(1));
        assertEquals(List.of(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 18), TODAY),
                List.copyOf(attendance.getAttendedDates()));
    }

    @Test
    public void equals_comparesStudentClassAndDates() {
        attendance.addDate(TODAY);
        Attendance equivalent = new Attendance(ALICE, academicClass, CLOCK);
        equivalent.addDate(TODAY);
        Attendance otherStudent = new Attendance(BENSON, academicClass, CLOCK);
        otherStudent.addDate(TODAY);
        AcademicClass otherClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        Attendance otherMembership = new Attendance(ALICE, otherClass, CLOCK);
        otherMembership.addDate(TODAY);

        assertTrue(attendance.equals(attendance));
        assertEquals(attendance, equivalent);
        assertEquals(equivalent, attendance);
        assertFalse(attendance.equals(null));
        assertFalse(attendance.equals("attendance"));
        assertFalse(attendance.equals(otherStudent));
        assertFalse(attendance.equals(otherMembership));
        equivalent.removeDate(TODAY);
        assertFalse(attendance.equals(equivalent));
    }

    @Test
    public void hashCode_equalHistoriesWithDifferentClocks_returnsSameHash() {
        Attendance equivalent = new Attendance(ALICE, academicClass, CLOCK.withZone(ZoneId.of("UTC")));
        attendance.addDate(TODAY.minusDays(1));
        equivalent.addDate(TODAY.minusDays(1));
        assertEquals(attendance, equivalent);
        assertEquals(attendance.hashCode(), equivalent.hashCode());
    }

    @Test
    public void copyFor_fixedClock_preservesDateValidation() {
        Clock historicalClock = Clock.fixed(Instant.parse("2000-01-01T16:30:00Z"), ZoneId.of("Asia/Singapore"));
        LocalDate historicalToday = LocalDate.of(2000, 1, 2);
        Attendance original = new Attendance(ALICE, academicClass, historicalClock);
        original.addDate(historicalToday.minusDays(1));

        Attendance copy = original.copyFor(BENSON, academicClass);

        assertEquals(Set.of(historicalToday.minusDays(1)), copy.getAttendedDates());
        assertTrue(copy.addDate(historicalToday));
        assertThrows(IllegalArgumentException.class, () -> copy.addDate(historicalToday.plusDays(1)));
        assertEquals(Set.of(historicalToday.minusDays(1), historicalToday), copy.getAttendedDates());
        assertEquals(Set.of(historicalToday.minusDays(1)), original.getAttendedDates());
    }

    @Test
    public void addDate_differentAssociations_keepsDatesSeparate() {
        Attendance otherStudent = new Attendance(BENSON, academicClass, CLOCK);
        AcademicClass otherClass = new AcademicClass(new ModuleCode("CS2101"), new ClassName("T01"));
        Attendance otherMembership = new Attendance(ALICE, otherClass, CLOCK);
        attendance.addDate(TODAY);
        assertEquals(0, otherStudent.getAttendanceCount());
        assertEquals(0, otherMembership.getAttendanceCount());
    }
}
