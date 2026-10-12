package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    private Label className;
    @FXML
    private Label moduleCode;

    /**
     * Creates an {@code AcademicClassCard} with the given {@code AcademicClass} and index to display.
     */
    public AcademicClassCard(AcademicClass academicClass, int displayedIndex) {
        super(FXML);
        this.academicClass = academicClass;
        id.setText(displayedIndex + ". ");
        className.setText(academicClass.getClassName().value);
        moduleCode.setText("Module: " + academicClass.getModuleCode().value);
    }
}
