package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Lists all academic classes in insertion order.
 */
public class ListClassCommand extends Command {

    public static final String COMMAND_WORD = "listclass";
    public static final String MESSAGE_USAGE = "No arguments are required for this command.\n" +
    "Simply type 'listclass' to list all academic classes in insertion order.";
    public static final String MESSAGE_SUCCESS = "Listed all classes.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
