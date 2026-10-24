# TAssist layout refresh manual testing

These checks apply to `refresh-ui-layout`, based on the team's master. Use Java 25
with JavaFX and a desktop session (on Linux, run automated checks with `xvfb-run`).
The copyable command blocks and expected results are also in
[UiLayoutTestingCommands.txt](UiLayoutTestingCommands.txt).

## Start an isolated session

From a checkout containing the local branch, preserve any unrelated work before
switching, then run:

```sh
git switch refresh-ui-layout
./gradlew check coverage
./gradlew -I config/ui-layout-demo.gradle run
```

The launcher reuses the reference branch's isolated-run approach, with this
branch's own directory: `_temp/refresh-ui-layout/session/`. Its data and preferences
are separate from a normal `./gradlew run`. Run the following `clear` only in the
isolated window. Enter application commands one at a time, pressing Enter after
each line. Repeating the setup resets only this demo session.

## 1. Populate classes and students

```text
clear
addclass m/CS2103 c/F10
addclass m/CS2103 c/F11
addclass m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
add sn/DEMO0001 n/Alice Tan With A Long Student Name e/alice.with.a.long.email.address@example.com tg/alice_demo t/tutorial t/project
add sn/DEMO0002 n/Ben Lim e/ben@example.com tg/ben_demo t/project
add sn/DEMO0003 n/Cara Ng e/cara@example.com t/tutorial
assign sn/DEMO0001 m/CS2103 c/F10
assign sn/DEMO0002 m/CS2103 c/F10
assign sn/DEMO0003 m/CS2103 c/F11
assign sn/DEMO0001 m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
```

Expect the input above the output at the top. Below them, classes occupy vertical
rows on the left and students occupy the right. The footer shows the save location.
Class indices are 1–3, with module codes, names and `Students: 2`, `Students: 1`,
`Students: 1`. The student list is alphabetical: Alice, Ben, Cara.

Drag the divider both ways. Classes have approximately 45% of the initial width;
both columns resize and scroll independently. Resize from a wide window to the
usual 740 px width, then the existing 450 px minimum. Long class and student names,
email addresses and tags wrap within their cards. All contact details remain
readable; Cara has no blank Telegram line. Scroll to inspect cards below the fold.

Click class rows, including their text and count. The students and output must
stay unchanged, with no selected-class highlight. Class rows offer no click
instructions. Membership counts are the existing card counts, with no header counts.

## 2. Typing and executed filtering

Type each draft below **without Enter**, replace it with the next draft, and
finally erase it:

```text
find Alice
listbyclass 1
listbystudent 1
addattendance 1 1 d/2020-01-03
listbyclass 9999
```

The displayed students, classes, attendance and command output must remain
unchanged throughout. Now execute:

```text
listbystudent 1
listclass
listbyclass 1
list
find Alice
list
```

Expect respectively: classes F10 and the long-name class; all three classes;
Alice and Ben with their existing attendance labels; all three students; only
Alice; all three students. Indices follow the currently displayed lists.

Execute `listbyclass 9999`. Expect an error in the output and error styling in
the input, with the same displayed data. Editing its text clears error styling
without changing data. Use Up/Down to recall and edit successful commands; the
lists change only when a recalled command is executed.

## 3. Existing attendance and membership regression

```text
listclass
listbyclass 1
addattendance 1 1 d/2020-01-01
addattendance 1 1 d/2020-01-02
addattendance 2 1 d/2020-01-03
```

Expect Alice's ordinary attendance label to contain
`2020-01-01, 2020-01-02` and Ben's label to contain `2020-01-03`.
Dates retain their existing comma-separated presentation and wrapping.

```text
listbyclass 1 t/2
listbyclass 1
deleteattendance 2 1 d/2020-01-03
```

Expect the threshold command to show only Ben, with displayed index 1. The next
command restores Alice and Ben; deletion restores Ben's existing
`No attendance recorded` message. Typing `listbyclass 2` without Enter leaves the
current students and attendance visible. Erase that draft.

```text
unassign sn/DEMO0002 m/CS2103 c/F10
listbyclass 1
assign sn/DEMO0002 m/CS2103 c/F10
listbyclass 1
```

Expect the F10 card count to change to 1 and back to 2, and executed `listbyclass`
to show the corresponding members. The other classes' counts stay at 1.

## 4. Theme, focus and persistence

Inspect the input, output, both card lists, File/Help menus and footer: slate-blue
surfaces, light text, consistent spacing and subtle borders should be readable.
Tab through the input, output, Classes list and Students list. Focused controls
and student rows show a visible blue focus indicator. The Classes list has a
focus border around the list; its rows remain unselected.
Verify keyboard menu navigation and inspect the opened menu's focused item.

Press F1 while the input is focused, and also open Help from the menu. Expect the
same slate palette and readable Help text. Tab to Copy URL, check its focus border,
and activate it. Close Help, then execute `exit` and relaunch:

```sh
./gradlew -I config/ui-layout-demo.gradle run
```

Execute `listclass` and `listbyclass 1`. Expect three classes, Alice and Ben in F10,
Alice's two comma-separated dates, and Ben's `No attendance recorded` message.

## 5. Keyboard scrolling with overflowing classes

After the persistence check, add enough classes to extend below the viewport:

```text
addclass m/CS2103 c/KEYBOARD-01
addclass m/CS2103 c/KEYBOARD-02
addclass m/CS2103 c/KEYBOARD-03
addclass m/CS2103 c/KEYBOARD-04
addclass m/CS2103 c/KEYBOARD-05
addclass m/CS2103 c/KEYBOARD-06
addclass m/CS2103 c/KEYBOARD-07
addclass m/CS2103 c/KEYBOARD-08
addclass m/CS2103 c/KEYBOARD-09
addclass m/CS2103 c/KEYBOARD-10
addclass m/CS2103 c/KEYBOARD-11
addclass m/CS2103 c/KEYBOARD-12
```

With the input focused, press Tab to reach output, then Tab again to reach
Classes. Its border should indicate focus. Use Page Down and Page Up to move
through the classes, Arrow Down and Arrow Up to scroll by a row, and End and Home
to reach the bottom and top. Repeated keys at either boundary should be harmless.
No class row should become selected or highlighted, and the displayed students,
attendance and command output should remain unchanged. Tab should continue to
Students; Shift+Tab should return to Classes, then to output.

## Automated validation and optional captures

`./gradlew check coverage` runs the tests and Checkstyle. The UI suite checks the
assembled split layout, keyboard scrolling without class selection, narrow-card
wrapping, typing without filtering, executed filters and the original attendance
labels. On a desktop,
optional captures from the real JavaFX scenes can be generated with:

```sh
TASSIST_CAPTURE_UI=1 ./gradlew test --tests 'seedu.address.ui.WorkspaceTest' --rerun-tasks
```

Images are written to `build/ui-preview/layout-*.png`. The tests use temporary
storage and do not alter normal or demo application data.
