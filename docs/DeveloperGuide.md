---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="800" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores all `AcademicClass` objects in a `UniqueAcademicClassList`. Each class is identified by its `ModuleCode` and `ClassName` and references its assigned students as `Person` objects. Students can belong to multiple classes.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* exposes the academic classes selected by the current filter as an unmodifiable `ObservableList<AcademicClass>` for the UI to observe.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Listing students by class

The `listbyclass` command displays students assigned to the class at a one-based index in the currently displayed
class list, not necessarily the full stored class list.
It changes the student view, not the stored student records, class memberships, or the current class view.

The following sequence diagram traces `listbyclass 1`, assuming CS2103 (F10-2) is the first displayed class
and saving succeeds.
It starts at `LogicManager`, omitting the UI caller. The input strings are defined in the diagram note.
`m:ModelManager` is accessed through the `Model` interface.

<puml src="diagrams/ListByClassSequenceDiagram.puml" alt="Successful ListByClass execution from index parsing through displayed class selection, student filtering, attendance display setup and result creation" />

1. `LogicManager` passes `commandText` to `AddressBookParser`, which creates a `ListByClassCommandParser`
   and calls `parse(args)`. The command parser uses `ParserUtil#parseIndex(args)` to validate the positive integer
   and creates a `ListByClassCommand` with the resulting `Index`.
2. `LogicManager` calls `execute(model)` on the command. The command obtains the currently displayed classes through
   `Model#getFilteredAcademicClassList()`, checks the index against the list size, and selects the class using
   `lastShownList.get(index.getZeroBased())`.
3. The command calls `Model#filterPersonsByClass(academicClass)`. `ModelManager` installs a membership predicate
   and uses `PersonNameComparator` to display matching students alphabetically. Name comparison ignores case,
   with original spelling as a tie-breaker. The underlying student storage order is unchanged.
4. The command calls `Model#setAttendanceDisplayClass(academicClass)` to select the class used for attendance display.
   `ModelManager` refreshes the student view so the displayed attendance corresponds to that class.
5. The command obtains the displayed student list and uses its size and the class identity to construct the success message:
   `Listed N students taking CS2103 (F10-2).` It returns a `CommandResult` to `LogicManager`.
6. `LogicManager` saves the address book before returning the result, even though this command only changes the view.
   The view filter itself is not persisted.

The diagram summarises index parsing and class selection in notes. It omits model-internal filtering and refresh calls,
logging (including its extra student-list access), list-size and class-field access, message formatting, and storage calls
to keep the successful path readable.

**Other outcomes:** Missing, non-integer, zero, negative, or overflowing indices cause a `ParseException` before
command creation. A positive index beyond the displayed class list (including when that list is empty) causes
a `CommandException` before the student filter or attendance display changes. A valid class with no students succeeds:
the student view becomes empty and the result reports zero students. If saving fails after filtering,
`LogicManager` reports a `CommandException`; the already-updated view is not rolled back.

### Listing all classes

The `listclass` command restores the complete academic-class list by applying
`PREDICATE_SHOW_ALL_ACADEMIC_CLASSES`. It takes no arguments and leaves the
student list unchanged.

The following sequence diagram shows the successful execution of `listclass`.
Parsing helpers, model-internal predicate updates, result construction, and
storage calls are shown at a high level or omitted for clarity.

<puml src="diagrams/ListClassSequenceDiagram.puml" alt="Successful listclass execution through the Logic and Model components" />

### Arrow key command history navigation

Arrow key navigation lets users recall previously executed commands without executing them again. `CommandBox` delegates
history storage and navigation to its `CommandHistory`, which holds two separate lists and tracks the draft and the
current browsing position.

Up moves towards older entries and Down moves towards the draft (newer entries). 
Navigation stops at either boundary, and empty history leaves the input unchanged. 
Modified arrow keys (such as Shift+Up or Ctrl+Down) do not trigger history navigation. 
History lasts only for the current session and does not survive between restarts.

<puml src="diagrams/CommandHistoryObjects.puml" alt="CommandBox linked to CommandHistory, its draft and position, and its original and working command lists" />

| Field | Purpose |
| --- | --- |
| `commands` | Stores up to 20 successful commands in execution order, oldest first. Editing past commands does not change these entries. |
| `workingCommands` | Holds a working copy of the history. Temporary edits remain available until the next non-blank command submission, whether it succeeds or fails. |
| `draft` | Preserves the input from before the user started browsing history. Moving Down past the newest entry restores it. It is separate from the lists because it has not been successfully executed. |
| `position` | Identifies the selected entry in `workingCommands`, using a zero-based index. A value equal to the list size represents the draft. |

In the diagram, `position = 2` selects `list`. With four history entries, position 4 represents the draft. Moving Down past `find Alice` restores `addclass m/CS2103 n/f`.

#### Preserving temporary edits with two command lists

`commands` preserves the successfully executed commands. `workingCommands` is a separate list that preserves temporary
edits while browsing history. These partial object diagrams show only the two lists, with entries numbered from zero.

**Step 1: Recall.** After executing `help`, `find Carol`, `list`, and `find Alice`, the user presses Up to recall `find Alice`.
Both lists contain the same commands.

<puml src="diagrams/CommandHistoryBeforeEdit.puml" alt="CommandHistory with identical original and working lists before editing a recalled command" />

**Step 2: Edit and navigate away.** The user changes the input to `find Bob` without executing it, then presses Up to recall
`list`. Before moving, `saveCurrentInput()` saves `find Bob` in `workingCommands`. The original list still contains `find Alice`.

<puml src="diagrams/CommandHistoryAfterEdit.puml" alt="CommandHistory with a temporary edit in the working list while the original list remains unchanged" />

Pressing Down now recalls **`find Bob`**, so the user can continue editing it. Using a single list would either overwrite
the original history entry or lose the temporary edit. After a successful or failed submission, `resetNavigation()`
rebuilds `workingCommands` from `commands`, discarding temporary browsing edits.

A successful submission first adds the command to `commands` and clears the draft. A failed submission preserves the
failed input as the draft without adding it to history. Both cases reset `position` to the draft position.

### Adding an academic class

#### Implementation

The `addclass` command creates an empty `AcademicClass`, identified by its `ModuleCode` and `ClassName`. `AddressBook` stores academic classes in a `UniqueAcademicClassList`, which rejects duplicate module code and class name pairs.

Given below is an example usage scenario for `addclass m/CS2103 c/F10-2`, assuming the class does not already exist.

Step 1. The user enters `addclass m/CS2103 c/F10-2`. `LogicManager` passes this command text to `AddressBookParser`, which creates an `AddClassCommandParser`.

Step 2. The command parser checks the arguments and uses `ParserUtil` to construct the `ModuleCode` and `ClassName`. It creates an empty `AcademicClass` and wraps it in an `AddClassCommand`.

Step 3. `LogicManager` executes the command. The command calls `Model#hasAcademicClass()` to check for an existing class with the same identity, then calls `Model#addAcademicClass()` when the class is absent.

The following sequence diagram shows how this operation goes through the `Logic` component. `commandText` is `"addclass m/CS2103 c/F10-2"`, `arguments` is `" m/CS2103 c/F10-2"`, and `a` is the class created by the command parser. The parsing helper calls, creation of `a`, and construction of the `CommandResult` are omitted for clarity.

<puml src="diagrams/AddClassSequenceDiagram.puml" alt="Adding an academic class through the Logic component" />

Step 4. `ModelManager` delegates the addition to `AddressBook`. `AddressBook` copies the current academic class list, appends the new class, and passes the updated list to `UniqueAcademicClassList#setAcademicClasses()`. It then registers the class as a student observer so its membership can track student updates.

Similarly, the model sequence diagram below expands the `addAcademicClass(a)` call from the logic diagram:

<puml src="diagrams/AddClassSequenceDiagram-Model.puml" alt="Adding an academic class through the Model component" />

Step 5. The command returns a successful `CommandResult` with the message `New class added: CS2103-F10-2`. `LogicManager` saves the updated address book through `Storage` before returning the result to the UI. Storage calls are omitted from the logic diagram.

<box type="info" seamless>

**Note:** The logic diagram shows both successful addition and duplicate-class rejection. The model diagram expands only the successful addition. Invalid arguments cause a `ParseException` before the command is created. If `Model#hasAcademicClass()` returns `true`, the command throws a `CommandException` without adding the class. Neither failure reaches the save operation.
</box>

### Deleting an academic class

The `deleteclass` command identifies an existing class by its one-based index in the currently displayed class list. `AddressBookParser` delegates argument validation to `DeleteClassCommandParser`, which uses `ParserUtil.parseIndex()` and creates a `DeleteClassCommand` containing an `Index`. At execution time, the command retrieves `Model#getFilteredAcademicClassList()`, checks the index against its size, and calls `Model#deleteAcademicClass()` with the selected stored class. This is the same observable list used by the class panel, so selection respects the current class filter and display order. Invalid indices are rejected without changing the model.

The following diagram shows a successful run of `deleteclass 1`, assuming at least one class is displayed. The first displayed class is removed and the returned feedback identifies the deleted class. The existing model deletion path removes its assignments and attendance records while retaining student records. Remaining displayed classes are renumbered automatically. Parsing helpers and result construction are omitted for clarity; `LogicManager` saves the updated address book before returning the result to the UI. No storage schema or model interface change is required for index-based selection.

<puml src="diagrams/DeleteClassSequenceDiagram-Logic.puml" alt="Deleting an academic class through the Logic component" />

Invalid arguments cause a `ParseException` before command creation. If the class is absent, the command throws a `CommandException` without deleting anything. These error paths are omitted from the diagram.

### Assigning and unassigning students

`AssignCommand` and `UnassignCommand` change a student's membership in one academic class without
adding or deleting student records or classes. `AssignCommand` identifies records by student number,
module code and class name, including records hidden by filters. `UnassignCommand` instead accepts
student and class indices in their respective displayed lists; hidden records cannot be selected.

#### Assigning a student

For example, `assign sn/A1111111H m/CS2103 c/F10` assigns an existing student to an existing class.
The following sequence diagram starts at `LogicManager#execute(commandText)` and shows successful
parsing, command creation and execution. Here, `commandText` is the example above and `args` is
the argument string after the command word. Constructor arguments are abbreviated as `...`;
the parser passes the validated student number, module code and class name to the command.
`m:ModelManager` is accessed through the `Model` interface. The `Assign membership (model)`
reference frame is expanded in the next diagram.

<puml src="diagrams/AssignSequenceDiagram.puml" alt="Successful execution of AssignCommand, resolving the stored student and class through Model before assigning membership" />

1. `LogicManager` asks `AddressBookParser` to parse the input. It creates an `AssignCommandParser`,
   which validates the arguments and constructs an `AssignCommand`.
2. `LogicManager` executes the returned command, which resolves the stored `Person` and
   `AcademicClass` through the model's lookup methods.
3. The command calls `Model#assignStudentToClass(person, academicClass)`.
4. On successful membership addition, the command returns a `CommandResult` to `LogicManager`,
   which saves the address book before returning the result to its caller.

The following model-level sequence expands the membership update. The return value is `true`
for this scenario because the student was not already a member.

<puml src="diagrams/AssignSequenceDiagram-Model.puml" alt="ModelManager delegates assignment to AddressBook and AcademicClass adds the student to its membership set" />

`ModelManager` delegates to `AddressBook`, which checks the stored-object preconditions using
assertions before calling `AcademicClass#assignStudent(person)`. The class uses a set to prevent
duplicate membership. If the student is already assigned, the model returns `false` and
`AssignCommand` throws a `CommandException` instead of returning success feedback.

#### Unassigning a student

For example, `unassign 2 1` removes the second displayed student from the first displayed class.
The following sequence diagram starts at `LogicManager#execute(commandText)`, where `commandText`
is the example above. It assumes that both records exist and the student belongs to the class.
The parser validates exactly two positive indices using `ParserUtil#parseIndices`.
The command resolves the student through `Model#getFilteredPersonList()` and the class through
`Model#getFilteredAcademicClassList()`, checking each index against its displayed list size.
Membership changes go through the `Model` interface.

<puml src="diagrams/UnassignSequenceDiagram.puml" alt="Successful execution of UnassignCommand, resolving displayed student and class indices before requesting removal through Model" />

1. `LogicManager` asks `AddressBookParser` to parse the input. It creates an `UnassignCommandParser`,
   which validates the arguments and constructs an `UnassignCommand`.
2. `LogicManager` executes the returned command, which validates both displayed indices and
   resolves the selected student and class.
3. The command calls `Model#unassignStudentFromClass(person, academicClass)` and checks whether
   the model reports a successful removal.
4. A successful removal produces a `CommandResult`, which `LogicManager` returns after saving
   the address book.

The `Unassign membership (model)` reference frame is expanded below. Only the selected class's
student set changes; the student record and memberships in other classes remain intact.

<puml src="diagrams/UnassignSequenceDiagram-Model.puml" alt="ModelManager delegates unassignment to AddressBook and AcademicClass removes the student from its membership set" />

If the student is not assigned to the class, `AcademicClass#unassignStudent(person)` returns `false`
without changing membership. This result propagates through the model to the command, which throws
a `CommandException`. Successful removal refreshes membership-dependent
student and class filters without resetting the active filters or ordering. Indices may shift as
records disappear from filtered views. Attendance history is retained for correction or reassignment.

#### Validation and failure paths

The activity diagram below complements the successful-execution sequence diagrams. Assignment
and unassignment resolve records differently: assignment uses stored identifiers, while unassignment
uses displayed indices. Both validate the selected records before updating membership and saving.

<puml src="diagrams/AssignUnassignActivityDiagram.puml" alt="Assign and Unassign workflow showing invalid input, missing student or class, unchanged membership, save failure and successful completion" />

The guards distinguish successful and rejected paths. An unchanged membership means that the
student is already assigned for `assign`, or is not assigned for `unassign`. In either case, the
command reports an error before `LogicManager` reaches the saving step. A failure to save after
a successful membership change is different: the in-memory change has already occurred.

#### Validation, errors and persistence

The sequence diagrams focus on successful execution and omit individual parser validation calls,
assertion lookups, formatting helpers and exception construction to keep the interactions readable.

* The assignment parser rejects missing, repeated or unknown prefixes and invalid identifier values.
  The unassignment parser rejects anything other than exactly two positive integer indices.
  Both report `ParseException` before constructing the command.
* Assignment rejects empty lookup results with `CommandException`. Unassignment reports an invalid
  displayed student or class index with `CommandException`, including when the relevant view is empty.
  Both resolve the student before the class, before attempting a membership update.
* A duplicate assignment or an absent membership is rejected without changing memberships.
* On successful command execution, `LogicManager` saves the address book before returning the
  result to its caller. Saving occurs outside the scope of these diagrams. If saving fails,
  `LogicManager` reports a `CommandException`; the in-memory membership change is not rolled back.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a Teaching Assistant (TA) managing multiple modules and tutorial or lab groups.
* has a need to manage a significant number of student contacts, track assignment submissions, and record attendance.
* can type fast
* needs to maintain clear boundaries between the different cohorts and modules they teach.
* can type fast and prefers typing over mouse interactions to navigate the application.
* is reasonably comfortable using Command Line Interface (CLI) apps and command shortcuts.

**Value proposition**: helps Teaching Assistants organise all their modules, student contacts, and assignment details in a centralized application significantly faster than a typical mouse-driven GUI app.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a … | I want to … | So that I can… |
|---|---|---|---|
| `* * *` | TA | create student profiles with their contact information | maintain an up-to-date record of the students I teach |
| `* * *` | TA | edit a student’s information | correct inaccurate or outdated records without recreating their profile |
| `* * *` | TA | remove a student from a specific module without affecting their records in other modules | stop tracking students who have dropped that module |
| `* * *` | TA | search for students by name across all modules or within a selected module | quickly locate the correct student during a class |
| `* * *` | TA | filter students by module and tutorial or lab group identifier | quickly view the students in a particular class |
| `* * *` | TA | create assignments with deadlines under a specific module | track the work expected from students in that module |
| `* * *` | TA | record whether an individual student has submitted an assignment | distinguish submitted work from outstanding submissions |
| `* * *` | TA | record whether each submission has been marked and view those still awaiting marking | resume grading where I left off |
| `* * *` | TA | receive clear feedback when I enter an invalid command or perform an invalid action | understand the problem and correct it |
| `* *` | new user | access an in-app guide explaining its features, commands and usage examples | understand its capabilities and learn to use it without external assistance |
| `* *` | TA managing several modules | create modules and their tutorial or lab groups | organise the different classes I teach |
| `* *` | TA managing several modules | assign students to their respective modules and tutorial or lab groups | keep different cohorts clearly organised |
| `* *` | TA | assign custom tags to students | record relevant learning needs and tailor my teaching approach |
| `* *` | TA | view a student’s communication handles, such as NUS email or Telegram | contact them without searching other platforms |
| `* *` | TA | copy the email addresses of students in a selected module or class group to my clipboard | send announcements or lesson materials through my email client |
| `* *` | TA | view a student’s assignment records alongside the relevant module information | review their progress within the correct module |
| `* *` | TA | filter students within a selected module by their submission status for a specific assignment | identify students with outstanding submissions |
| `* *` | TA | record grades for each student’s assignments | review their performance and identify areas where they may need additional support |
| `* *` | TA | view upcoming assignment deadlines across my modules | remind students about approaching deadlines |
| `* *` | TA | record each student’s attendance for weekly tutorial or lab sessions | maintain participation records for grading and follow-up |
| `* *` | experienced user | define shortcuts for frequently used commands | perform repetitive tasks with less typing |
| `* *` | TA | view and recall previously entered commands | reuse them without retyping them |
| `* *` | returning TA | archive a completed module and its associated student and assignment records | keep my active workspace focused on the current semester while preserving historical records |
| `*` | first-time user | explore sample student records and assignments across multiple modules and clear the sample data when ready | understand the application before entering real records |
| `*` | TA | import a CSV student roster into a specified module and tutorial or lab group | populate my classes without entering each student individually |
| `*` | TA | bulk-delete unwanted student records after confirmation | clear records efficiently without deleting students individually |
| `*` | TA who is also a student | distinguish between modules I teach and modules I take | keep my teaching and learning contacts separate |
| `*` | TA | view the contact details of the professor in charge of each module | conveniently raise teaching-related queries |
| `*` | TA | batch-update an assignment’s submission status for an entire tutorial or lab group | perform routine submission checks efficiently |
| `*` | TA | import assignment submission records from a CSV file into the relevant module and assignment | avoid updating every student’s status manually |
| `*` | TA | export a selected module’s student grades and submission records as a CSV file | share them with the module coordinator without manually copying the data |
| `*` | TA | receive an end-of-day notification listing students recorded as absent from my lab sessions | follow up with them about their absence |
| `*` | TA arranging consultations | use my calendar to automatically share available consultation times with students | reduce scheduling conflicts and back-and-forth communication |
| `*` | TA who prefers typing | navigate and operate the application using only the keyboard | complete tasks without switching to the mouse |
| `*` | TA who frequently enters commands | use command autocompletion | enter commands more quickly and with fewer typing errors |
| `*` | TA | switch between light and dark modes | use the application comfortably in different lighting conditions |                                         |

*{More to be added}*

### Use cases

#### **Create Student**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)  
**Guarantee:** If creation fails, existing data remains unchanged.  
**Main Success Scenario (MSS):**

1. TA requests to create a student, providing the student number, name,
   email address and, optionally, Telegram username.
2. TAssist creates the student profile and confirms successful creation.
3. TAssist displays the updated student list, including the newly created student.

Use case ends.  
**Extensions:**

- **1a. Required details are missing, or the supplied details are invalid.**
  - 1a1. TAssist informs TA of the error and the expected input.
  - Use case ends.

- **1b. The student number, email address or supplied Telegram username matches an existing student's corresponding detail.**
  - 1b1. TAssist informs TA of the duplicate detail and rejects the creation request.
  - Use case ends.

- **1c. TAssist is unable to save the new student.**
  - 1c1. TAssist informs TA that the student could not be saved and asks TA to try again.
  - Use case ends.

#### **Create Class**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)  
**Guarantee:** If creation fails, existing data remains unchanged.

**Main Success Scenario (MSS):**

1. TA requests to create a class, providing its module code and class name.
2. TAssist creates the class and confirms successful creation.
3. TAssist displays the updated class list, including the newly created class.

Use case ends.

**Extensions:**

- **1a. Required details are missing, or the supplied details are invalid.**
  - 1a1. TAssist informs TA of the error and the expected input.
  - Use case ends.

- **1b. A class with the same module code and class name already exists.**
  - 1b1. TAssist informs TA that the class already exists and rejects the creation request.
  - Use case ends.

- **1c. TAssist is unable to save the new class.**
  - 1c1. TAssist informs TA that the class could not be saved and asks TA to try again.
  - Use case ends.

#### **Search Student**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)

**Main Success Scenario (MSS):**

1. TA requests to search for students by providing one or more name keywords.
2. TAssist displays the matching students.

Use case ends.

**Extensions:**

- **1a. The search keywords are missing or invalid.**
  - 1a1. TAssist informs TA of the error and the expected input.
  - Use case ends.

- **1b. No students match the search keywords.**
  - 1b1. TAssist displays an empty student list and informs TA that no students were found.
  - Use case ends.

#### **Assign Student to Class**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)  
**Guarantee:** A student is assigned to the same class at most once.

**Main Success Scenario (MSS):**

1. TA requests to assign a student to a class, providing the student number,
   module code and class name.
2. TAssist assigns the student to the specified class and automatically saves the assignment.
3. TAssist returns a confirmation of the successful assignment, identifying the student and the class.

Use case ends.

**Extensions:**

- **1a. Required details are missing, values are empty or invalid, or repeated, unrecognised or extra details are supplied.**
  - 1a1. TAssist informs TA of the error and the required details, without changing any assignments.
  - Use case ends.

- **1b. No student matches the supplied student number.**
  - 1b1. TAssist informs TA that the student does not exist, identifying the supplied student number.
  - 1b2. TAssist rejects the request without changing any assignments.
  - Use case ends.

- **1c. No class matches the supplied module code and class name.**
  - 1c1. TAssist informs TA that the class does not exist, identifying the supplied module code and class name.
  - 1c2. TAssist rejects the request without changing any assignments.
  - Use case ends.

- **1d. The student is already assigned to the specified class.**
  - 1d1. TAssist keeps the existing assignment without creating a duplicate.
  - 1d2. TAssist informs TA that the student is already assigned, identifying the student number and class.
  - Use case ends.

#### **Unassign Student from Class**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)  
**Guarantee:** The student profile, class and all other class assignments remain unchanged.

**Main Success Scenario (MSS):**

1. TA requests to unassign a student from a class, providing the student index and class index
   in their respective displayed lists.
2. TAssist removes the student's assignment to the specified class and automatically saves the change.
3. TAssist returns a confirmation that the student is no longer assigned to the class, identifying the student and the class.

Use case ends.

**Extensions:**

- **1a. Required details are missing, or unrecognised or extra details are supplied.**
  - 1a1. TAssist informs TA of the error and the required details, without changing any assignments.
  - Use case ends.

- **1b. The student index is outside the displayed student list.**
  - 1b1. TAssist reports an invalid displayed student index.
  - 1b2. TAssist rejects the request without changing any assignments.
  - Use case ends.

- **1c. The class index is outside the displayed class list.**
  - 1c1. TAssist reports an invalid displayed class index.
  - 1c2. TAssist rejects the request without changing any assignments.
  - Use case ends.

- **1d. The student is not assigned to the specified class.**
  - 1d1. TAssist reports that the student is not assigned to the class without changing any data.
  - Use case ends.

- **2a. TAssist is unable to save the change.**
  - 2a1. TAssist informs TA that the removal of the assignment could not be saved.
  - Use case ends.

#### **List Students by Class**

**System:** TAssist  
**Actor:** Teaching Assistant (TA)  
**Guarantee:** Student profiles, classes and class assignments remain unchanged.

**Main Success Scenario (MSS):**

1. TA requests to list the students assigned to a class, providing its module code and class name.
2. TAssist displays the names of all students assigned to the specified class in alphabetical order,
   identifying the module code and class name.

Use case ends.

**Extensions:**

- **1a. Required details are missing or blank, or the request is invalid.**
  - 1a1. TAssist informs TA of the error and the required details.
  - Use case ends.

- **1b. No class matches the supplied module code and class name.**
  - 1b1. TAssist informs TA that the class does not exist and asks TA to create the class or specify an existing class.
  - Use case ends.

- **1c. The specified class exists but has no assigned students.**
  - 1c1. TAssist displays an empty student list for the specified module code and class name.
  - Use case ends.

### Non-Functional Requirements

| ID | Category | Non-Functional Requirement |
| --- | --- | --- |
| NFR01 | Usability | TAssist should be optimised for users who are comfortable with typing, with typed commands as the primary means of interacting with the application. |
| NFR02 | Performance | TAssist should respond to normal user commands within 2 seconds under typical usage conditions. |
| NFR03 | Capacity | TAssist should support at least 500 students across 20 classes and 10 modules while maintaining normal command response times. |
| NFR04 | Data Persistence | Every successful operation that modifies application data should be automatically saved to local storage so that the latest data remains available after the application is restarted. |
| NFR05 | Data Integrity | A failed or invalid operation should not result in partially modified or inconsistent application data. Existing valid data should remain unchanged if the operation cannot be completed successfully. |
| NFR06 | Error Handling | When an invalid command or input is entered, TAssist should provide a clear error message that identifies the problem and, where appropriate, indicates the expected command format. |
| NFR07 | Single-User Operation | TAssist should operate as a single-user application and should not support multiple users accessing the same application data during normal operation. |
| NFR08 | Data Storage | TAssist data should be stored locally in a human-editable text file and should not rely on a database management system. |
| NFR09 | Platform Compatibility | TAssist should work on Windows, Linux, and macOS without relying on operating-system-specific functionality. |
| NFR10 | Runtime Compatibility | TAssist should be compatible with Java 25. |
| NFR11 | Portability | TAssist should run without requiring an installer and should be distributed as a single JAR file, or a single ZIP file if additional files are unavoidable. |
| NFR12 | Offline Availability | Core TAssist functionality should not depend on the team's own remote server and should remain usable without an Internet connection. |
| NFR13 | Display Compatibility | The GUI should work without resolution-related inconvenience at 1920×1080 or above at 100%/125% scaling, and all functionality should remain usable at 1280×720 or above at 150% scaling. |

### Glossary

| Term | Definition |
| --- | --- |
| TAssist | The application developed to help teaching assistants manage student contacts and information across the classes they teach. |
| TA | Teaching Assistant. The primary target user of TAssist. |
| Student | A person whose contact and class information is managed by a TA using TAssist. |
| Student Number | The unique institutional identifier used by TAssist to identify a student. |
| Module | An academic course taught by the TA, identified by its module code, such as `CS2103`. A module may contain one or more classes. |
| Class | A tutorial or lab group belonging to a module, identified by a class name such as `F10-2`. A class is uniquely identified by its combination of module and class name. |
| Student Profile | The information stored about a student, such as their student number, name, email address, Telegram username, and class associations. |
| Communication Handle | Contact information used to communicate with a student, such as their email address or Telegram username. |
| CLI | Command-Line Interface. The text-based interface through which the user enters commands in TAssist. |
| Command | A textual instruction entered into TAssist to perform an operation, such as `addstudent`, `find`, or `assign`. |
| Command Prefix | A prefix used to specify a parameter in a command, such as `sn/` for student number, `m/` for module, or `c/` for class. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Assigning a student to a class

1. Successful assignment and live count updates

   1. Prerequisites: Use student numbers, emails, and a class that do not already exist. Run
      `add sn/A1111111H n/Alice Tan e/alice.assign@example.com`,
      `add sn/A2222222J n/Bob Lim e/bob.assign@example.com`, and `addclass m/CS2103 c/F-10`.
      Expected: The new class shows `Students: 0`.

   1. Test case: `assign sn/A1111111H m/CS2103 c/F-10`<br>
      Expected: Success feedback identifies the student and class; the count immediately becomes 1.

   1. Test case: `find Nobody`, then `assign c/ f-10 sn/ a2222222j m/ cs2103`<br>
      Expected: Assignment succeeds despite the filter, the count becomes 2, and the student list stays empty.

1. Duplicate assignment and errors

   1. Test case: `assign sn/A1111111H m/CS2103 c/F-10` again<br>
      Expected: Already-assigned feedback; the count remains 2.

   1. Test cases: `assign sn/A9999999Z m/CS2103 c/F-10` and `assign sn/A1111111H m/CS9999 c/F-10`
      (assuming those identifiers do not exist)<br>
      Expected: Student-not-found and class-not-found feedback respectively; memberships remain unchanged.

   1. Incorrect commands to try: `assign sn/random-numbers m/CS2103 c/F-10`,
      `assign sn/A1111111H m/CS2103`, `assign sn/A1111111H m/CS2103 c/`,
      `assign sn/A1111111H sn/A1111111H m/CS2103 c/F-10`, and
      `assign sn/A1111111H m/CS2103 c/F-10 x/extra`<br>
      Expected: Validation, missing-parameter, empty-value, duplicate-prefix, and unknown-prefix errors
      respectively. Memberships and the count remain unchanged.

1. Persistence

   1. Close and reopen the app after the successful assignments.<br>
      Expected: The class still shows 2 students. Its `studentNumbers` JSON list contains each assigned
      student number once. Repeating either assignment reports that the student is already assigned.

### Listing classes by student

1. Add students and classes using unused identifiers:
   `add sn/A0999801X n/AlphaDemoAlice e/alpha.demo.alice@example.com`,
   `add sn/A0999802Y n/AlphaDemoBob e/alpha.demo.bob@example.com`,
   `addclass m/CS2103 c/DEMO-T01`, and `addclass m/CS2100 c/DEMO-L01`.
2. Assign Alice to both classes using `assign sn/A0999801X m/CS2103 c/DEMO-T01` and
   `assign sn/A0999801X m/CS2100 c/DEMO-L01`.
3. Run `find AlphaDemoAlice`, then `listbystudent 1`.
   Expected: both classes appear in creation order; feedback reports two classes and Alice's identity.
   The student search results remain unchanged.
4. Run `listbystudent 0`, `listbystudent 1 extra`, and `listbystudent 2`.
   Expected: invalid-command or invalid-index feedback; both views remain unchanged.
5. Run `unassign 1 1`.
   Expected: only CS2100 DEMO-L01 remains in the Classes panel.
6. Run `find AlphaDemoBob`, then `listbystudent 1`.
   Expected: zero matching classes and an empty Classes panel.
7. Run `listclass` to restore all classes, and `list` to restore all students.
   Restart the app and repeat Alice's lookup to verify that memberships persist while display filters reset.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
