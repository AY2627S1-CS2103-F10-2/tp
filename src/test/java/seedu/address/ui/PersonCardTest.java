package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // JavaFX is already initialized by another UI test.
        }
    }

    @Test
    public void constructor_personWithRemark_displaysRemark() throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRemark("Likes baseball").build();
        FutureTask<Label> createCard = new FutureTask<>(() -> {
            PersonCard personCard = new PersonCard(person, 1);
            return (Label) personCard.getRoot().lookup("#remark");
        });
        Platform.runLater(createCard);

        Label remarkLabel = createCard.get();
        assertNotNull(remarkLabel);
        assertEquals("Likes baseball", remarkLabel.getText());
    }
}
