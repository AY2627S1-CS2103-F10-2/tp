package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.academicclass.AcademicClass;

/** A UI component that displays an academic class. */
public class ClassCard extends UiPart<Region> {

    private static final String FXML = "ClassListCard.fxml";

    @FXML
    private HBox cardPane;
    @FXML
    private Label className;
    @FXML
    private Label moduleCode;

    /** Creates a class card for the given academic class. */
    public ClassCard(AcademicClass academicClass) {
        super(FXML);
        className.setText(academicClass.getClassName().value);
        moduleCode.setText(academicClass.getModuleCode().value);
    }
}
