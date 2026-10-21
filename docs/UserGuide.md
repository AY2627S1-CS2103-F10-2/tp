---
  layout: default.md
  title: "TAssist User Guide"
  pageNav: 3
---

# TAssist User Guide

TAssist is a **desktop application for NUS Teaching Assistants who need to manage the students they tutor, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). It gives TAs quick access to student contacts and information, helping them manage student learning outcomes from a central application.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103-F10-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TAssist.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar TAssist.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. The app shows students and classes in separate sections.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all students.

   * `add sn/A0123456X n/John Doe e/johnd@example.com tg/john_doe` : Adds a student named `John Doe`.

   * `addclass m/CS2103T c/F10-2` : Creates a class named `F10-2` for module `CS2103T`.

   * `delete 3` : Deletes the 3rd student shown in the current list.

   * `deleteclass m/CS2103T c/F10-2` : Deletes the class named `F10-2` for module `CS2103T`.

   * `clear` : Deletes all students and classes.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add sn/STUDENT_NUMBER n/NAME`, replace `STUDENT_NUMBER` with a value such as `A0123456X` and `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `sn/STUDENT_NUMBER n/NAME e/EMAIL`, `e/EMAIL sn/STUDENT_NUMBER n/NAME` is also acceptable.

* Prefixes identify the kind of information you are providing.<br>
  For example, `n/` is used for student names, `m/` for module codes, and `c/` for class names.

* Repeated prefixes are accepted only when the command explicitly allows multiple values.<br>
  For example, `add` and `edit` accept multiple `t/TAG` values, but `addclass` and `deleteclass` accept exactly one `m/MODULE_CODE` and one `c/CLASS_NAME`.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a student: `add`

Adds a student to TAssist.

Format: `add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]... `

<box type="tip" seamless>

**Tip:** A student can have any number of tags, including zero. Tags are supported by this build although they are not part of the current MVP feature list.
</box>

* `STUDENT_NUMBER` must contain 5 to 20 letters or digits, with no spaces or special characters. It is stored and displayed in uppercase, and must be unique.
* `NAME` must contain only letters, digits, and spaces, and must start with a letter or digit. Leading and trailing spaces are removed.
* `EMAIL` must be in the format `local-part@domain`, with no spaces. Email comparisons are case-insensitive for duplicate checks.
* `TELEGRAM`, if provided, may start with `@`. After removing `@`, it must contain 5 to 32 letters, digits, or underscores. It must be unique, ignoring case.
* Parameters can be supplied in any order.

Examples:
* `add sn/A0123456X n/Alice Tan e/alice@u.nus.edu tg/alice_tan`
* `add sn/A0234567Y n/Benjamin Lim e/benjamin@u.nus.edu`
* `add e/chloe@u.nus.edu sn/A0345678Z tg/@chloe_ng n/Chloe Ng`

Expected output:
* `New person added: Alice Tan; Student Number: A0123456X; Email: alice@u.nus.edu; Telegram: @alice_tan; Tags:`

Common errors:
* Duplicate student number: `A student with this student number already exists.`
* Duplicate email: `A student with this email already exists.`
* Duplicate Telegram username: `A student with this Telegram username already exists.`
* Invalid student number: `Invalid student number. Student numbers must contain 5–20 letters or digits with no spaces.`
* Invalid name: `Names should only contain alphanumeric characters and spaces, and should not be blank`
* Invalid Telegram username: `Invalid Telegram username. Usernames must contain 5–32 letters, digits, or underscores.`

### Listing all students: `list`

Shows a list of all students in TAssist.

Format: `list`

Expected output: `Listed all persons.`

The student list displays each student's name, student number, email, optional Telegram username, and tags.

### Listing students by class: `listbyclass`

Shows only students assigned to the specified module and class, alphabetically by name using the existing student cards.

Format: `listbyclass m/MODULE_CODE c/CLASS_NAME`

* Both parameters are required exactly once and can appear in either order. Unknown parameters are rejected.
* Surrounding spaces are ignored. Module and class names must match an existing pair, ignoring case.
* Module codes must start with an ASCII letter and contain only ASCII letters or digits.
* Class names must contain at least one ASCII letter or digit, and may contain only ASCII letters, digits, or hyphens.
* Empty or invalid values produce the same validation errors as `addclass` and `assign`, without changing the student list.
* Names are sorted ignoring case, with original spelling used to break ties.
* An existing class with no students shows an empty student list and a count of zero.
* A nonexistent class produces an error without changing the current student list.
* Deleting the class currently being displayed empties the student list without deleting student records.
  If you recreate the same module/class pair, subsequent assignments update this view using the new class membership.
* This command changes only the displayed student list, not student records, class memberships, or the class list.
* Use `list` to show all students again. `list` and `find` display results in their original insertion order.

Examples:

* `listbyclass m/CS2103 c/F10-2`
* `listbyclass c/f10-2 m/cs2103`

The result message includes the student count and class, for example: `Listed 2 students taking CS2103 (F10-2).`

Classes created with `addclass` start empty. Use `assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`
to assign an existing student before listing the class. Memberships are saved across restarts.

Example walkthrough (use unused student numbers, email addresses, and a new class):

```text
add sn/A0999901X n/Zoe Test e/zoe.test@example.com
add sn/A0999902Y n/Amy Test e/amy.test@example.com
addclass m/CS2103 c/TEST-103
assign sn/A0999901X m/CS2103 c/TEST-103
assign sn/A0999902Y m/CS2103 c/TEST-103
listbyclass m/CS2103 c/TEST-103
```

The student list shows only Amy Test followed by Zoe Test, with the message
`Listed 2 students taking CS2103 (TEST-103).` Other students and the class list are unchanged.
After closing and reopening TAssist, run `listbyclass m/CS2103 c/TEST-103` again to display the saved
memberships alphabetically. The filtered view itself is not saved. Run `list` to return to all students.

**Known limitation:** Updating class memberships when assigned students are edited or deleted is deferred to v1.4.
Avoid editing student numbers or deleting assigned students for now: stale membership references can prevent
the saved data from loading on restart. Back up your data before experimenting with these operations.

### Creating a class: `addclass`

Creates an academic class. Created classes appear in the class list section of the UI.

Format: `addclass m/MODULE_CODE c/CLASS_NAME`

* `MODULE_CODE` is the module that the class belongs to.
* `CLASS_NAME` is the class name for that module.
* The `m/` and `c/` parameters can be supplied in any order.
* Each class is uniquely identified by its module code and class name pair.
* Duplicate classes with the same module code and class name are not allowed.
* Module codes and class names are normalized before being saved.
* `MODULE_CODE` must begin with a letter and contain only letters or digits.
* `CLASS_NAME` must contain at least one letter or digit, and may contain only letters, digits, or hyphens.

Examples:
* `addclass m/CS2103T c/F10-2`
* `addclass c/T24 m/ST2334`

Expected output:
* `New class added: CS2103T-F10-2`

Common errors:
* Missing module code: `Missing required parameter: m/MODULE_CODE`
* Missing class name: `Missing required parameter: c/CLASS_NAME`
* Repeated module code: `Parameter m/ was specified more than once.`
* Repeated class name: `Parameter c/ was specified more than once.`
* Unknown parameter: `Unknown parameter: PREFIX`
* Duplicate class: `A class with this name for this module already exists`
* Invalid module code: `Module code must contain letters or digits`
* Invalid class name: `Class names can only contain letters, digits, or '-'`

### Assigning a student to a class: `assign`

Assigns an existing student to an existing academic class.

Format: `assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`

* All three parameters are required and must be supplied exactly once. They can appear in any order.
* Leading and trailing spaces are removed from parameter values.
* Student numbers, module codes, and class names are matched case-insensitively. Hyphens matter: `F10` and `F-10` identify different classes.
* `STUDENT_NUMBER` must contain 5 to 20 letters or digits, without spaces or special characters.
* `MODULE_CODE` must begin with a letter and contain only letters or digits.
* `CLASS_NAME` must contain at least one letter or digit, and may contain only letters, digits, or hyphens.
* The student and class must already exist. A student hidden by the current search can still be assigned; assignment preserves the displayed student list.
* A student may belong to multiple classes. Repeating an assignment reports that the student is already assigned and leaves the membership unchanged.

Examples:
* `assign sn/A1111111H m/CS2103 c/F-10` assigns student `A1111111H` to class `F-10` for module `CS2103`.
* `assign c/ f10-2 sn/ a0234567y m/ cs2103t` assigns student `A0234567Y` to class `F10-2` for module `CS2103T`.

Expected output:
* `Student (A1111111H) has been assigned to CS2103-F-10.`

Common errors:
* Missing parameter: `You're missing some parameters. The correct format is assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`
* Student not found: `Student (A1234567H) cannot be found in the address book.`
* Class not found: `Class (CS2100-F10) cannot be found in the address book.`
* Student already assigned: `Student (A1234567H) is already assigned to class CS2101-L1.`
* Repeated parameter: `Multiple values specified for the following single-valued field(s):` followed by the repeated prefixes.
* Unknown parameter: `Unknown parameter: PREFIX`
* Unexpected text before the parameters: `Invalid command format!` followed by the command usage.
* Empty or invalid student number, module code, or class name: the corresponding validation error from `add` or `addclass`.

Rejected commands leave memberships and student counts unchanged.

### Recording attendance: `addattendance`

Records one attended date for a student in a particular module and class.

Format: `addattendance sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME d/YYYY-MM-DD`

* The student and class must exist, and the student must currently be assigned to that class.
* Supply each of `sn/`, `m/`, `c/` and `d/` exactly once, in any order. Unknown prefixes are rejected.
* Outer whitespace in values is trimmed. Student numbers, module codes and class names are case-insensitive.
* Use a valid calendar date with a four-digit year, two-digit month and two-digit day.
  Today and past dates are accepted; future dates are rejected using your computer's local date.
* Recording the same date twice for the same student and class is rejected without changing the existing record.
  Other students and classes have separate attendance histories.
* Recording attendance does not change your current student or class list view.
* Successful records are saved automatically and retained after restarting TAssist.
  Unassigning a student keeps their history, but new dates cannot be recorded until they are assigned again.
  Deleting the student or class removes the associated attendance history.

Example (after creating the student and class):

1. `assign sn/A0123456X m/CS2103 c/F10-2`
2. `addattendance sn/A0123456X m/CS2103 c/F10-2 d/2024-02-29`

TAssist displays a confirmation message when the date is recorded. This command does not add an attendance display
to student cards.

### Editing a student: `edit`

Edits an existing student in TAssist.

Format: `edit INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]... `

* Edits the student at the specified `INDEX`. The index refers to the index number shown in the displayed student list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the student's existing tags are removed; adding tags is not cumulative.
* To remove all of a student's tags, enter `t/` without a tag after it.
* Student number, email, and Telegram username must remain unique after editing.

Examples:
*  `edit 1 tg/john_doe e/johndoe@example.com` edits the Telegram username and email address of the 1st student.
*  `edit 2 sn/A0234567Y n/Benjamin Lim t/` edits the student number and name of the 2nd student, and clears all existing tags.

### Locating students by name: `find`

Finds students whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Students matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a student: `delete`

Deletes the specified student from TAssist.

Format: `delete INDEX`

* Deletes the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd student in TAssist.
* `find Betsy` followed by `delete 1` deletes the 1st student in the results of the `find` command.

Expected output:
* `Deleted person: Alice Tan; Student Number: A0123456X; Email: alice@u.nus.edu; Telegram: @alice_tan; Tags:`

<box type="warning" seamless>

The current build deletes students by displayed list index, not by student number. The MVP command `deletestudent sn/STUDENT_NUMBER` is not available in this build.
</box>

### Deleting a class: `deleteclass`

Deletes the class identified by its module code and class name from TAssist.

Format: `deleteclass m/MODULE_CODE c/CLASS_NAME`

* Both parameters are required, must have nonempty values, and must each be supplied exactly once. They can be supplied in either order.
* `MODULE_CODE` must begin with a letter and contain only letters or digits, as for `addclass`.
* `CLASS_NAME` must contain at least one letter or digit, and may contain only letters, digits, or hyphens, as for `addclass`.
* The module code and class name must match the same existing class. Matching ignores letter case and leading or trailing spaces.
* Searches all classes, regardless of the current student list filter. No list index is required.
* Removes the class from the class list and saves the change automatically.
* Any student assignments to the deleted class are removed with it. The students remain in TAssist, and other classes and their assignments are unchanged.
* If no matching class exists, an error is shown and no data is changed.

Examples (assuming the corresponding classes exist):

* `deleteclass m/CS2103T c/F10-2` deletes class `F10-2` for module `CS2103T`.
* `deleteclass c/t24 m/st2334` deletes class `T24` for module `ST2334`, using reversed parameter order and lowercase values.

Expected output for the first example:

* `Deleted class: CS2103T-F10-2`

Common errors:

* Missing `m/` or `c/`, or extra text before the parameters: `Invalid command format!`, followed by the command usage.
* Empty module code: `Module code cannot be empty`
* Empty class name: `Class name cannot be empty`
* Invalid module code, such as `2103` or `CS-2103`: `Module code must contain letters or digits`
* Invalid class name, such as `T 24` or `---`: `Class names can only contain letters, digits, or '-'`
* Repeated module code: `Multiple values specified for the following single-valued field(s): m/`
* Repeated class name: `Multiple values specified for the following single-valued field(s): c/`
* Unknown parameter, such as `x/`: `Unknown parameter: x/`
* No matching class: `No class with module code CS2103T and class name F10-2 exists.`

Invalid module codes and class names are rejected before searching for a class. Valid values that do not identify an existing class result in a "no matching class" error.

### Unassigning a student from an academic class: `unassign`

Removes a student's membership from one academic class. Use this when a student leaves a class but should remain in the address book or in other classes.

Format: `unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`

**Before you begin:** The student and academic class must exist, and the student must already be assigned to that class.

* All three parameters are required and must each appear exactly once. You can enter them in any order.
* `STUDENT_NUMBER` must contain 5–20 letters or digits, without spaces.
* `MODULE_CODE` must start with a letter and contain only letters or digits.
* `CLASS_NAME` may contain letters, digits, and hyphens (`-`), and must contain at least one letter or digit.
* Identifiers are matched exactly, ignoring letter case. For example, `cs2103` matches `CS2103`, but class name `F10` does not match `F-10`.
* The command uses the student number, not an index in the displayed list. It can unassign a student hidden by a previous `find` command.
* Only membership in the specified class is removed. The student record, academic class, and memberships in other classes remain unchanged.
* The current person filter and displayed order remain unchanged.

Examples

Suppose student `A1111111H` is assigned to class `F10` in module `CS2103`. Run:

```text
unassign sn/A1111111H m/CS2103 c/F10
```

Expected feedback:

```text
Student (A1111111H) has been unassigned from CS2103-F10.
```

You can also change the parameter order and use lowercase identifiers:

```text
unassign c/f10 m/cs2103 sn/a1111111h
```

Both commands target the same membership. Run either command once; repeating it after successful removal reports that the student is no longer assigned to the class.

Common errors:

* Missing parameter: `You're missing some parameters. The correct format is unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`
* Student not found: `Student (A1234567H) cannot be found in the address book.`
* Class not found: `Class (CS2100-F10) cannot be found in the address book.`
* Student not assigned to the class: `Student (A1234567H) is not assigned to class CS2101-L1.` This also occurs when repeating a successful unassignment.
* Repeated parameter: `Multiple values specified for the following single-valued field(s):` followed by the repeated prefixes.
* Unknown parameter: `Unknown parameter: PREFIX`
* Unexpected text before the parameters: `Invalid command format!` followed by the command usage.
* Empty or invalid student number, module code, or class name: the corresponding validation error from `add` or `addclass`.

Rejected commands leave memberships and student counts unchanged.

### Clearing all entries: `clear`

Clears all students and classes from TAssist.

Format: `clear`

Expected output: `Address book has been cleared!`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TAssist automatically saves data after every successful command. You do not need to save manually.

Data is saved to `[JAR file location]/data/addressbook.json`. If TAssist cannot save the data, it displays an error such as `Could not save data due to the following error: ...` or `Could not save data to file ... due to insufficient permissions to write to the file or the folder.`

### Editing the data file

TAssist data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, TAssist starts with an empty address book at the next run. The invalid file remains on disk until you run a command (TAssist saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause TAssist to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

### Features not yet available in this build

The following MVP commands are planned or specified, but are not currently accepted by this build:

* `addstudent sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM]`: use `add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM]` instead.
* `deletestudent sn/STUDENT_NUMBER`: use `delete INDEX` instead.
* `liststudent`: use `list` instead.
* `listclass`

If any of these command words are entered, TAssist displays `Unknown command.`

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TAssist home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]... ` <br> e.g., `add sn/A0123456X n/James Ho e/jamesho@example.com tg/james_ho t/CS2103`
**Add Class** | `addclass m/MODULE_CODE c/CLASS_NAME` <br> e.g., `addclass m/CS2103T c/F10-2`
**Assign** | `assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME` <br> e.g., `assign sn/A0123456X m/CS2103T c/F10-2`
**Clear**  | `clear`
**Add attendance** | `addattendance sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME d/YYYY-MM-DD`<br> e.g., `addattendance sn/A0123456X m/CS2103 c/F10-2 d/2024-02-29`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Delete Class** | `deleteclass m/MODULE_CODE c/CLASS_NAME`<br> e.g., `deleteclass m/CS2103T c/F10-2`
**Edit**   | `edit INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**List students by class** | `listbyclass m/MODULE_CODE c/CLASS_NAME`<br> e.g., `listbyclass m/CS2103 c/F10-2`
**Unassign** | `unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`<br> e.g., `unassign sn/A1111111H m/CS2103 c/F10`
**Help**   | `help`
**Exit**   | `exit`
