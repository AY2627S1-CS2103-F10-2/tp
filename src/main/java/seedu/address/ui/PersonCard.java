package seedu.address.ui;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label studentNumber;
    @FXML
    private Label email;
    @FXML
    private Label telegram;
    @FXML
    private Label attendance;
    @FXML
    private FlowPane details;
    @FXML
    private VBox attendanceSection;
    @FXML
    private VBox attendanceMonths;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, null);
    }

    /** Creates a card and optionally shows this student's attendance for a class. */
    public PersonCard(Person person, int displayedIndex, AcademicClass attendanceClass) {
        this(person, displayedIndex, attendanceClass, List.of());
    }

    /** Creates a card and optionally shows this student's attendance for a class. */
    public PersonCard(Person person, int displayedIndex, AcademicClass attendanceClass,
            List<Attendance> attendanceRecords) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        studentNumber.setText(person.getStudentNumber().value);
        email.setText(person.getEmail().value);
        email.setWrapText(true);
        email.setMaxWidth(Double.MAX_VALUE);
        if (person.getTelegram().isPresent()) {
            telegram.setText("@" + person.getTelegram().get().value);
            telegram.setMaxWidth(Double.MAX_VALUE);
        } else {
            telegram.setVisible(false);
            telegram.setManaged(false);
        }
        if (attendanceClass == null) {
            attendanceSection.setVisible(false);
            attendanceSection.setManaged(false);
            attendance.setVisible(false);
            attendance.setManaged(false);
        } else {
            attendanceSection.setVisible(true);
            attendanceSection.setManaged(true);
            attendance.setVisible(true);
            attendance.setManaged(true);
            updateAttendance(attendanceClass, attendanceRecords);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label tagLabel = new Label(tag.tagName);
                    tagLabel.setWrapText(true);
                    tagLabel.setMinWidth(0);
                    tags.getChildren().add(tagLabel);
                });
        tags.setManaged(!tags.getChildren().isEmpty());
        tags.setVisible(!tags.getChildren().isEmpty());
    }

    private void updateAttendance(AcademicClass attendanceClass, List<Attendance> attendanceRecords) {
        Attendance attendanceRecord = attendanceRecords.stream()
                .filter(record -> record.getStudent().isSamePerson(person)
                        && record.getAcademicClass().isSameClass(attendanceClass))
                .findFirst()
                .orElse(null);
        int attendanceCount = attendanceRecord == null ? 0 : attendanceRecord.getAttendanceCount();
        attendance.setText(String.format("Attendance \u00B7 %d %s", attendanceCount,
                attendanceCount == 1 ? "day" : "days"));
        attendanceMonths.getChildren().clear();

        if (attendanceCount == 0) {
            Label emptyMessage = new Label("No dates recorded");
            emptyMessage.getStyleClass().add("attendance-empty");
            attendanceMonths.getChildren().add(emptyMessage);
            return;
        }

        Map<YearMonth, List<LocalDate>> datesByMonth = new TreeMap<>();
        attendanceRecord.getAttendedDates().forEach(date ->
                datesByMonth.computeIfAbsent(YearMonth.from(date), ignored -> new ArrayList<>()).add(date));

        datesByMonth.forEach((month, dates) -> attendanceMonths.getChildren().add(createMonthRow(month, dates)));
    }

    private HBox createMonthRow(YearMonth month, List<LocalDate> dates) {
        HBox monthRow = new HBox(8);
        monthRow.setAlignment(Pos.TOP_LEFT);
        monthRow.setMinWidth(0);

        Label monthLabel = new Label(month.format(MONTH_FORMATTER));
        monthLabel.getStyleClass().add("attendance-month");
        monthLabel.setMinWidth(76);

        FlowPane datePills = new FlowPane(5, 4);
        datePills.getStyleClass().add("attendance-dates");
        datePills.setMinWidth(0);
        datePills.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(datePills, Priority.ALWAYS);

        dates.forEach(date -> {
            Label datePill = new Label(date.format(DAY_FORMATTER));
            datePill.getStyleClass().add("attendance-date");
            datePills.getChildren().add(datePill);
        });

        monthRow.getChildren().addAll(monthLabel, datePills);
        return monthRow;
    }
}
