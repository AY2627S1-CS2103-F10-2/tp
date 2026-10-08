package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.academicclass.AcademicClass;

/** Panel containing the list of academic classes. */
public class ClassListPanel extends UiPart<Region> {

    private static final String FXML = "ClassListPanel.fxml";

    @FXML
    private ListView<AcademicClass> classListView;

    /** Creates a class list panel backed by the given observable list. */
    public ClassListPanel(ObservableList<AcademicClass> classList) {
        super(FXML);
        classListView.setItems(classList);
        classListView.setCellFactory(listView -> new ClassListViewCell());
    }

    /** Custom cell that displays an academic class using a {@link ClassCard}. */
    private static class ClassListViewCell extends ListCell<AcademicClass> {
        @Override
        protected void updateItem(AcademicClass academicClass, boolean empty) {
            super.updateItem(academicClass, empty);

            if (empty || academicClass == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new ClassCard(academicClass).getRoot());
            }
        }
    }
}
