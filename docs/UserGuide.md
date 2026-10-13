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

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. The app shows students and classes in separate sections.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all students.

   * `add sn/A0123456X n/John Doe e/johnd@example.com tg/john_doe` : Adds a student named `John Doe`.

   * `addclass m/CS2103T c/F10-2` : Creates a class named `F10-2` for module `CS2103T`.

   * `delete 3` : Deletes the 3rd student shown in the current list.

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
  For example, `add` and `edit` accept multiple `t/TAG` values, but `addclass` accepts exactly one `m/MODULE_CODE` and one `c/CLASS_NAME`.

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
* Names are sorted ignoring case, with original spelling used to break ties.
* An existing class with no students shows an empty student list and a count of zero.
* A nonexistent class produces an error without changing the current student list.
* This command changes only the displayed student list, not student records, class memberships, or the class list.
* Use `list` to show all students again. `list` and `find` display results in their original insertion order.

Examples:

* `listbyclass m/CS2103 c/F10-2`
* `listbyclass c/f10-2 m/cs2103`

The result message includes the student count and class, for example: `Listed 2 students taking CS2103 (F10-2).`

Classes created with `addclass` start empty. Use `assign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`
to assign an existing student before listing the class. Memberships are saved across restarts.

**Known limitation:** Updating class memberships when assigned students are edited or deleted is deferred to v1.4.

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
* `deleteclass m/MODULE_CODE c/CLASS_NAME`
* `listclass`
* `unassign sn/STUDENT_NUMBER m/MODULE_CODE c/CLASS_NAME`

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
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**List students by class** | `listbyclass m/MODULE_CODE c/CLASS_NAME`<br> e.g., `listbyclass m/CS2103 c/F10-2`
**Help**   | `help`
**Exit**   | `exit`
