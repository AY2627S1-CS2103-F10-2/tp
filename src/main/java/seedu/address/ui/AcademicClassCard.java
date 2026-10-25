package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.academicclass.AcademicClass;

/**
 * A UI component that displays information of an {@code AcademicClass}.
 */
public class AcademicClassCard extends UiPart<Region> {

    private static final String FXML = "AcademicClassListCard.fxml";

    private final AcademicClass academicClass;

    @FXML
    private Label id;
    @FXML
    private Text moduleCode;
    @FXML
    private Text className;
    @FXML
    private Label studentCount;

    /**
     * Creates an {@code AcademicClassCard} with the given {@code AcademicClass} and index to display.
     */
    public AcademicClassCard(AcademicClass academicClass, int displayedIndex) {
        super(FXML);
        this.academicClass = academicClass;
        id.setText(displayedIndex + ". ");
        moduleCode.setText(academicClass.getModuleCode().value + " ");
        className.setText(academicClass.getClassName().value);
        studentCount.textProperty().bind(Bindings.size(academicClass.getStudents()).asString("Students: %d"));
    }
}
