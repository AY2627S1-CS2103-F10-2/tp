# TAssist usability manual testing

These instructions cover `improve-ui-usability`, based on team master containing
`refresh-ui-layout`. Use Java 25 and a desktop session. The same checklist is
available as plain text in [UiUsabilityTestingCommands.txt](UiUsabilityTestingCommands.txt).

## 1. Terminal setup

In a checkout containing the local branch, preserve unrelated work before switching:

```sh
git switch improve-ui-usability
./gradlew check coverage
./gradlew -I config/ui-usability-demo.gradle run
```

On Linux, use `xvfb-run ./gradlew check coverage` for automated checks. The launcher
stores demo data and preferences in `_temp/improve-ui-usability/session/`, separate
from both normal application data and branch A's demo session. Enter app commands
one line at a time, pressing Enter after each line unless a step says otherwise.
Run `clear` only in this isolated demo window.

## 2. App setup and initial counts

Execute in the app:

```text
clear
addclass m/CS2103 c/F10
addclass m/CS2103 c/F11
addclass m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
add sn/DEMO0001 n/Alice Tan e/alice@example.com tg/alice_demo t/tutorial
add sn/DEMO0002 n/Ben Lim e/ben@example.com t/project
add sn/DEMO0003 n/Cara Ng e/cara@example.com t/tutorial
assign sn/DEMO0001 m/CS2103 c/F10
assign sn/DEMO0002 m/CS2103 c/F10
assign sn/DEMO0003 m/CS2103 c/F11
assign sn/DEMO0001 m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
```

Expect `Classes — 3 shown / 3 total` and `Students — 3 shown`. Card membership
counts remain 2, 1 and 1. Keep the input/output at the top, compact students, slate
theme from branch A. Both student and class rows are now passive. The split places
Students on the left and Classes on the right to match `STUDENT_INDEX CLASS_INDEX` commands.
Classes receive more default width, while Students remain readable and resizable.
There is no extra sentence repeating the displayed student count.
Class and student indices should have matching font, weight, colour and spacing.

## 3. Executed filters and zero-result students

Execute these commands and check the corresponding headers:

```text
listbystudent 1
listclass
find Alice
list
listbyclass 1
list
```

Expected in order:
- Classes: 2 shown / 3 total; Students: 3 shown.
- Classes: 3 shown / 3 total; Students: 3 shown.
- Students: 1 shown; classes unchanged.
- Students: 3 shown; classes unchanged.
- Students: 2 shown (Alice and Ben); classes unchanged.
- Students: 3 shown; classes unchanged.

Execute `find Nobody`. Expect `Students — 0 shown` and `No students shown.` in
the left list. Execute `list` to restore all three students.

Type `find Alice`, `listbystudent 1` and `listbyclass 3` without Enter, replacing
one draft with the next. Counts, lists, attendance, scroll position and output must
stay unchanged. Erase the final draft.

## 4. Class totals, empty filters and membership changes

Execute:

```text
add sn/DEMO0004 n/Drew Ong e/drew@example.com
listbystudent 4
addclass m/CS2100 c/EMPTY
```

Expect Students: 4 shown. After `listbystudent 4`, expect Classes: 0 shown / 3 total
and `No classes shown.`. Adding the empty class changes the class total to 4 while
shown remains 0, because the class filter is still for Drew.

```text
listclass
delete 4
deleteclass m/CS2100 c/EMPTY
listbystudent 1
unassign 1 2
assign sn/DEMO0001 m/CS2101 c/TUTORIAL-GROUP-WITH-A-LONG-NAME
listclass
```

Expected in order:
- Classes: 4 shown / 4 total.
- Students: 3 shown.
- Classes: 3 shown / 3 total.
- Classes: 2 shown / 3 total (Alice's classes).
- Classes: 1 shown / 3 total. The unassigned class leaves this filtered list;
  no different class row is revealed in its place.
- Classes: 2 shown / 3 total as Alice rejoins the long-name class.
- Classes: 3 shown / 3 total.

## 5. Successful class-index commands reveal their target

Execute:

```text
addclass m/CS2103 c/SCROLL01
addclass m/CS2103 c/SCROLL02
addclass m/CS2103 c/SCROLL03
addclass m/CS2103 c/SCROLL04
addclass m/CS2103 c/SCROLL05
addclass m/CS2103 c/SCROLL06
addclass m/CS2103 c/SCROLL07
addclass m/CS2103 c/SCROLL08
addclass m/CS2103 c/SCROLL09
addclass m/CS2103 c/SCROLL10
addclass m/CS2103 c/SCROLL11
addclass m/CS2103 c/SCROLL12
assign sn/DEMO0001 m/CS2103 c/SCROLL12
listbyclass 15
```

Expect Classes: 15 shown / 15 total, Students: 1 shown, and class 15 visible without
selection or a row highlight. The shown count uses the displayed class list; the
total always uses the complete class collection.

For each following scrolling check, first return the Classes list to the top:
Tab from input through output and Students to Classes, and press Home. Shift+Tab
three times returns to input. From Classes, Tab also wraps forward to input.
On a Mac keyboard without Home/End, use Fn+Left/Fn+Right.

Execute `editclass 15 c/SCROLL12UPDATED`. Expect the edited last row to become
visible, with Students still showing Alice. Execute `list` to restore all students.
Return Classes to the top before each command below:

```text
addattendance 1 15 d/2020-01-03
deleteattendance 1 15 d/2020-01-03
unassign 1 15
```

Each successful command should reveal class 15. The class remains displayed after
this unassignment because `listclass` is active. Attendance retains its existing
comma-separated label presentation when viewed with `listbyclass`.

Return Classes to the top. Execute `addattendance 1 15 d/2020-01-03` again: it fails
because Alice is unassigned. Expect an error with no class scrolling or data/count
changes. `listbyclass 9999` should also fail without scrolling.

Type `listbyclass 15` without Enter. It must not scroll or change counts or data.
Erase the draft. Clicking student or class cards must not select a row, execute a
command, change either list or alter counts. Clicking may focus the list; only the
active list should have the focus border, without a highlight on an individual row.

## 6. Minimum size and saved dimensions

On a monitor with enough usable space, resize the window: it must stop at 720 by
600. On smaller usable screens, each minimum dimension is capped at the available
space. Drag the split divider toward each side. Headers and counts should remain
readable, wrapping when needed, and student cards should retain the layout and
wrapping from branch A.

Execute `exit`, then edit only this demo file:
`_temp/improve-ui-usability/session/preferences.json`.
Set `guiSettings.windowWidth` to 450 and `guiSettings.windowHeight` to 500,
leaving the JSON structure valid. Relaunch:

```sh
./gradlew -I config/ui-usability-demo.gradle run
```

Expect a window at least 720 by 600 when the monitor has enough usable space, even
though the saved dimensions are smaller. Saved sizes and positions are clamped to
the selected monitor's usable bounds. Disconnected or entirely off-screen saved
windows recover on the primary monitor.
Exit again and inspect the demo preferences: the saved dimensions must meet these
screen-aware minimums. Relaunch for the following checks.

### Small screens, scaling and monitor changes

For a physical scaling check, set the desktop resolution to 1280 by 720 and scaling
to 150% using the operating system's display settings, then launch the isolated
demo again. The logical desktop is approximately 853 by 480 before taskbars and
other reserved space. JavaFX reports the usable bounds after scaling; do not apply
another scaling factor to the saved dimensions.

Expect the whole window to fit the usable screen, including its title bar and
footer. Its minimum height must shrink below 600 if necessary. Repeat with saved
dimensions of 1040 by 760 and then 450 by 500: both launches must fit the usable
screen. Keep the original JSON structure and other settings intact.

At this resolution and scaling:

- Execute `find Alice` and `list`; verify that input, feedback and counts work.
- Tab from input to output, then Students and Classes. All four controls should
  remain visible and reachable.
- In Classes, use Home, End and page keys to inspect all 15 classes. Rows must
  remain passive, and executing `listbyclass 15` must reveal its class.
- Execute `list` and scroll Students to reach every card and its details.
- Check that output remains readable and scrollable when feedback is long.
- Adjust the split divider and confirm that both lists remain usable.

If two monitors with different usable sizes are available, drag the window from
the larger monitor to the smaller one. Once most of the window is on the smaller
monitor, it should fit that monitor and use its screen-aware minimums. Move it back
and verify that the normal 720 by 600 minimum returns. Ordinary dragging within one
monitor must remain possible. Also check a changed resolution or disconnected
monitor while the app is open. Restore your original desktop settings afterward.

Automated tests use equivalent logical bounds of approximately 853 by 480 and
853 by 430, including reserved desktop space. These tests do not replace the
physical operating-system scaling and multi-monitor checks above.

## 7. Passive lists, scrolling and empty data

Before clearing data, Tab from input through output, Students and Classes. Check
that the visible focus border moves between the lists without selecting any row.
For each list with more cards than fit, verify Up, Down, Home, End, Page Up and
Page Down, mouse-wheel scrolling and scrollbar dragging. Every card must remain
reachable without changing data, filters or counts. Click cards in both lists and
confirm that no selection highlight or command action appears. F1 must still open
Help from either focused list.

Execute `clear` only in the isolated app. Expect:
- `Classes — 0 shown / 0 total` with `No classes shown.`
- `Students — 0 shown` with `No students shown.`

Tab into each empty list and press End, Home, arrow and page keys. Expect no
exceptions; Tab and Shift+Tab still traverse the controls, and F1 still opens Help.
No student or class rows become selected. Close Help, execute `exit`, relaunch the
isolated app and confirm the same zero counts and empty messages persist.

## Automated checks and optional captures

`./gradlew check coverage` runs the full suite and Checkstyle. Regression coverage
includes live counts during filters and membership/data changes, zero-result lists,
screen-aware restored and minimum sizes, monitor transitions, small-screen keyboard
access, mouse/keyboard scrolling without selection, successful class-index
scrolling, failed commands and targets that disappear under the active class filter.

Optional JavaFX captures on a desktop:

```sh
TASSIST_CAPTURE_UI=1 ./gradlew test --tests 'seedu.address.ui.WorkspaceTest' --rerun-tasks
```

Images are in `build/ui-preview/usability-*.png` and `layout-*.png`.
Automated tests use temporary storage. Normal launch with normal data remains:

```sh
./gradlew run
```
