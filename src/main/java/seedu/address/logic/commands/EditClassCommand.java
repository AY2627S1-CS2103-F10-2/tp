package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODULE_CODE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.academicclass.AcademicClass;
import seedu.address.model.academicclass.ClassName;
import seedu.address.model.academicclass.ModuleCode;

/**
 * Edits an academic class while preserving its student memberships.
 */
public class EditClassCommand extends Command {

    public static final String COMMAND_WORD = "editclass";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the academic class identified "
            + "by the index number used in the displayed class list. "
            + "Existing values will be overwritten by the input values. "
            + "Student memberships will be preserved.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_MODULE_CODE + "MODULE_CODE] "
            + "[" + PREFIX_CLASS_NAME + "CLASS_NAME]\n"
            + "At least one field must be provided.\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_MODULE_CODE + "CS2103 "
            + PREFIX_CLASS_NAME + "F10";

    public static final String MESSAGE_EDIT_CLASS_SUCCESS = "Edited class: %1$s";
    public static final String MESSAGE_DUPLICATE_CLASS = "A class with these details already exists!";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    private static final Logger logger = LogsCenter.getLogger(EditClassCommand.class);

    private final Index index;
    private final EditClassDescriptor editClassDescriptor;

    /**
     * Creates a command to edit the academic class at the specified displayed index with the supplied edit descriptor.
     *
     * @param index of the academic class in the displayed class list to edit
     * @param editClassDescriptor details to edit the class with
     */
    public EditClassCommand(Index index, EditClassDescriptor editClassDescriptor) {
        requireNonNull(index);
        requireNonNull(editClassDescriptor);

        this.index = index;
        this.editClassDescriptor = new EditClassDescriptor(editClassDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<AcademicClass> lastShownList = model.getFilteredAcademicClassList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CLASS_DISPLAYED_INDEX);
        }

        AcademicClass classToEdit = lastShownList.get(index.getZeroBased());
        AcademicClass editedClass = createEditedAcademicClass(classToEdit, editClassDescriptor);

        if (!classToEdit.isSameClass(editedClass) && model.hasAcademicClass(editedClass)) {
            logger.fine("Class edit rejected: another class has the requested identity.");
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }

        model.setAcademicClass(classToEdit, editedClass);
        logger.info("Edited academic class from " + classToEdit + " to " + editedClass);

        return new CommandResult(String.format(MESSAGE_EDIT_CLASS_SUCCESS, editedClass));
    }

    /**
     * Creates a class with the edited identity and a copy of the original student memberships.
     */
    private static AcademicClass createEditedAcademicClass(AcademicClass classToEdit,
            EditClassDescriptor editClassDescriptor) {
        assert classToEdit != null;

        ClassName updatedClassName = editClassDescriptor.getClassName()
                .orElse(classToEdit.getClassName());

        ModuleCode updatedModuleCode = editClassDescriptor.getModuleCode()
                .orElse(classToEdit.getModuleCode());

        return new AcademicClass(updatedModuleCode, updatedClassName, classToEdit);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditClassCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editClassDescriptor.equals(otherEditCommand.editClassDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editClassDescriptor", editClassDescriptor)
                .toString();
    }

    /**
     * Stores edits to a class's identity. Each present field replaces the corresponding original value.
     */
    public static class EditClassDescriptor {
        private ModuleCode moduleCode;
        private ClassName className;

        /**
         * Creates a descriptor without any edits.
         */
        public EditClassDescriptor() {}

        /**
         * Copies the fields of the given descriptor. Field values are immutable.
         */
        public EditClassDescriptor(EditClassDescriptor toCopy) {
            setClassName(toCopy.className);
            setModuleCode(toCopy.moduleCode);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(className, moduleCode);
        }

        public void setClassName(ClassName className) {
            this.className = className;
        }

        public Optional<ClassName> getClassName() {
            return Optional.ofNullable(className);
        }

        public void setModuleCode(ModuleCode moduleCode) {
            this.moduleCode = moduleCode;
        }

        public Optional<ModuleCode> getModuleCode() {
            return Optional.ofNullable(moduleCode);
        }
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditClassDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(className, otherEditPersonDescriptor.className)
                    && Objects.equals(moduleCode, otherEditPersonDescriptor.moduleCode);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("className", className)
                    .add("moduleCode", moduleCode)
                    .toString();
        }
    }
}
