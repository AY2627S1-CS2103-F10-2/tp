# Command hints: manual testing

Branch: `standardize-command-guidance`.

This guide checks the command hint strip only. Typing changes guidance;
commands still run through the existing Enter action. Existing parser and
execution errors remain in the command output. Counts, window sizing,
class scrolling and attendance presentation are outside this change.

## Start with isolated data

Use the project's Java 25 setup. Build a JAR, copy it into a new folder,
and run that copy. Packaged TAssist stores data beside its JAR, so this
keeps the manual session separate from your normal data.

macOS/Linux, from the repository root:

```sh
./gradlew shadowJar
hints_test_dir=$(mktemp -d "${TMPDIR:-/tmp}/tassist-hints.XXXXXX")
cp build/libs/TAssist.jar "$hints_test_dir/TAssist.jar"
java -jar "$hints_test_dir/TAssist.jar"
```

Windows PowerShell, from the repository root:

```powershell
.\gradlew.bat shadowJar
$hintsTestDir = Join-Path $env:TEMP ("tassist-hints-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $hintsTestDir
Copy-Item build\libs\TAssist.jar $hintsTestDir
java -jar (Join-Path $hintsTestDir "TAssist.jar")
```

## Default and contextual guidance

1. On launch, check that a highlighted strip is visible directly beneath
   the input and above the output. It should say:
   `Type a command, then press Enter to run it.`
2. Type each command word below without pressing Enter. Check the syntax
   against the table. Unbracketed arguments are required; square brackets
   indicate optional arguments; `...` means repeatable arguments.

| Command | Expected syntax |
| --- | --- |
| `add` | `add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]...` |
| `edit` | `edit INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]...` |
| `delete` | `delete INDEX` |
| `find` | `find KEYWORD [MORE_KEYWORDS]...` |
| `list` | `list` |
| `addclass` | `addclass m/MODULE_CODE c/CLASS_NAME` |
| `editclass` | `editclass INDEX [m/MODULE_CODE] [c/CLASS_NAME]` |
| `deleteclass` | `deleteclass INDEX` |
| `listclass` | `listclass` |
| `listbyclass` | `listbyclass INDEX [t/THRESHOLD]` |
| `listbystudent` | `listbystudent INDEX` |
| `assign` | `assign STUDENT_INDEX CLASS_INDEX` |
| `unassign` | `unassign STUDENT_INDEX CLASS_INDEX` |
| `addattendance` | `addattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD` |
| `deleteattendance` | `deleteattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD` |
| `help` | `help` |
| `clear` | `clear` |
| `exit` | `exit` |

3. Check that `edit` and `editclass` explain that at least one field must
   be provided. Commands with no required arguments say none are needed.
4. Check that contextual hints include `Press Enter to run.`
   For `deleteclass`, the index refers to the current displayed class list.
   For `assign`, the student index comes first, followed by the class index
   from their respective displayed lists. Student deletion uses `delete INDEX`.
   Compare `find` and the six single-index hints with the User Guide and command usage;
   their parameter names must agree.
5. Type `del`: check for `Matching commands: delete  |  deleteclass  |  deleteattendance`.
   Command names should use brighter monospace text, with the reminder on
   a separate, quieter line. Type `li` to check the matching list commands.
   Type `add`: its exact syntax should appear rather than suggestions for
   longer command words.
6. Type `LIST` or `unknown`: guidance should point to `help`, without an
   error message or new error styling. Clear the input or enter spaces:
   the default reminder should return and the strip should remain visible.

## Typing must leave the workspace unchanged

1. Note the displayed students, classes, attendance text and command output.
2. Type each draft below without pressing Enter:

   ```text
   find Alice
   listbyclass 1
   listbyclass 1 t/2
   listbystudent 1
   edit 1
   deleteclass 1
   assign 1 1
   unassign 1 1
   addattendance 1 1 d/2020-01-03
   deleteattendance 1 1 d/2020-01-03
   listbyclass 999999
   clear
   ```

3. Only the hint and input text should change. Lists, attendance text and
   output should retain their previous content. Typing must not scroll to
   a class, show data previews, or mark arguments as valid/invalid.
4. Compare the isolated `data/addressbook.json` before and after typing,
   if the file already exists: its contents must remain unchanged.

## Enter, errors and command history

1. Type `find HintsNoMatch` and press Enter. The existing command should
   filter the students and update the output. The input should clear and
   the default hint should return. Run `list` to restore the student list.
2. Type `listbyclass` and press Enter. The existing parser error should
   appear in the output. The input and syntax guidance should remain.
3. Type `listbyclass 999999` and press Enter. The existing invalid-index
   error should appear in the output; the hint must remain syntax guidance.
4. Run `list` successfully, then type `edit 1` without Enter. Press Up to
   recall `list`, then Down to restore `edit 1`. Hints should follow each
   text change; output and lists should not change during navigation.
5. With the input focused, Tab should still reach the output next. F1
   should still open Help through the existing keyboard shortcut.

## Wrapping and appearance

1. Resize to the narrowest supported window width. Type `edit 1`, the
   longest syntax hint, and check that all text wraps within the strip.
2. Confirm that the strip has a slate-blue highlight, readable text and
   a distinct `HINT` caption. It must not overlap input, output or lists.
3. Widen the window, then narrow it again. Text should reflow without
   clipping or retaining unnecessary height. Clearing the input should
   restore the shorter default strip.

## Automated checks

```sh
./gradlew check coverage shadowJar
```

On Windows, use `gradlew.bat`. On headless Linux, use
`xvfb-run ./gradlew check coverage shadowJar` for JavaFX tests.

Focused suites: `CommandHintsTest`, `CommandBoxTest`, and `WorkspaceTest`.
The plain-text companion is `CommandHintsTesting.txt`.
