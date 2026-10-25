package seedu.address.ui;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.academicclass.AcademicClass;

/**
 * A UI component that displays information of an {@code AcademicClass}.
 */
public class AcademicClassCard extends UiPart<Region> {

    private static final String FXML = "AcademicClassListCard.fxml";

    private final AcademicClass academicClass;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private FlowPane classHeading;
    @FXML
    private Label moduleCode;
    @FXML
    private Label className;
    @FXML
    private Label studentCount;

    /**
     * Creates an {@code AcademicClassCard} with the given {@code AcademicClass} and index to display.
     */
    public AcademicClassCard(AcademicClass academicClass, int displayedIndex) {
        super(FXML);
        this.academicClass = academicClass;
        id.setText(displayedIndex + ". ");
        moduleCode.setText(academicClass.getModuleCode().value);
        className.setText(academicClass.getClassName().value);
        classHeading.prefWrapLengthProperty().bind(classHeading.widthProperty());
        // Keep each heading part within the card, including a single long class name.
        for (Label part : List.of(moduleCode, className)) {
            part.maxWidthProperty().bind(classHeading.widthProperty());
            part.minHeightProperty().bind(Bindings.createDoubleBinding(() -> part.prefHeight(classHeading.getWidth()),
                    classHeading.widthProperty(), part.fontProperty(), part.textProperty(), part.insetsProperty()));
        }
        studentCount.textProperty().bind(Bindings.size(academicClass.getStudents()).asString("Students: %d"));
    }
}
