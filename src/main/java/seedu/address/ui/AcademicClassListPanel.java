package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.academicclass.AcademicClass;

/**
 * Panel containing the list of academic classes.
 */
public class AcademicClassListPanel extends UiPart<Region> {
    private static final String FXML = "AcademicClassListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(AcademicClassListPanel.class);

    @FXML
    private ListView<AcademicClass> academicClassListView;

    /**
     * Creates an {@code AcademicClassListPanel} with the given {@code ObservableList}.
     */
    public AcademicClassListPanel(ObservableList<AcademicClass> academicClassList) {
        super(FXML);
        academicClassListView.setItems(academicClassList);
        academicClassListView.setCellFactory(listView -> new AcademicClassListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of an {@code AcademicClass}
     * using an {@code AcademicClassCard}.
     */
    class AcademicClassListViewCell extends ListCell<AcademicClass> {
        @Override
        protected void updateItem(AcademicClass academicClass, boolean empty) {
            super.updateItem(academicClass, empty);

            if (empty || academicClass == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new AcademicClassCard(academicClass, getIndex() + 1).getRoot());
            }
        }
    }
}
