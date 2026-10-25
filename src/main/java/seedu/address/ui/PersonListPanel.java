package seedu.address.ui;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.attendance.Attendance;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Supplier<Optional<AcademicClass>> attendanceClassSupplier;
    private final Supplier<List<Attendance>> attendanceListSupplier;

    @FXML
    private ListView<Person> personListView;

    /** Creates a person list whose cards can display the currently selected class attendance. */
    public PersonListPanel(ObservableList<Person> personList,
            Supplier<Optional<AcademicClass>> attendanceClassSupplier,
            Supplier<List<Attendance>> attendanceListSupplier) {
        super(FXML);
        this.attendanceClassSupplier = attendanceClassSupplier;
        this.attendanceListSupplier = attendanceListSupplier;
        personListView.setItems(personList);
        Label emptyMessage = new Label("No students shown.");
        emptyMessage.setWrapText(true);
        personListView.setPlaceholder(emptyMessage);
        personList.addListener((ListChangeListener<Person>) change -> {
            logger.fine("Refreshing person cards after the displayed person list changed.");
            personListView.refresh();
        });
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1,
                        attendanceClassSupplier.get().orElse(null), attendanceListSupplier.get()).getRoot());
            }
        }
    }

}
