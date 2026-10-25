package seedu.address.ui;

import java.util.List;
import java.util.stream.Collectors;

/** Syntax guidance for drafts, without parsing arguments or accessing application data. */
final class CommandHints {
    static final String DEFAULT_HINT = "Type a command, then press Enter to run it.";

    // Keep guidance in sync with the command parsers; argument validation remains there.
    private static final List<String> SYNTAX = List.of(
            "add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]...",
            "edit STUDENT_INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]...",
            "delete STUDENT_INDEX", "find NAME [MORE_NAMES]...", "list",
            "addclass m/MODULE_CODE c/CLASS_NAME",
            "editclass CLASS_INDEX [m/MODULE_CODE] [c/CLASS_NAME]",
            "deleteclass CLASS_INDEX", "listclass",
            "listbyclass CLASS_INDEX [t/THRESHOLD]", "listbystudent STUDENT_INDEX",
            "assign STUDENT_INDEX CLASS_INDEX",
            "unassign STUDENT_INDEX CLASS_INDEX",
            "addattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD",
            "deleteattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD",
            "help", "clear", "exit");

    private CommandHints() {
    }

    /** Returns syntax for a command word; incomplete or invalid arguments do not change the guidance. */
    static String forInput(String draft) {
        String command = draft.strip().split("\\s+", 2)[0];
        if (command.isEmpty()) {
            return DEFAULT_HINT;
        }
        for (String syntax : SYNTAX) {
            if (syntax.equals(command)) {
                return syntax + "\nNo arguments needed. Press Enter to run.";
            }
            if (syntax.startsWith(command + " ")) {
                String legend = syntax.contains("[")
                        ? "Unbracketed arguments are required; [ ] optional; ... repeatable. "
                        : "Arguments are required. ";
                String editNote = command.equals("edit") || command.equals("editclass")
                        ? "Choose at least one field to edit. " : "";
                return syntax + "\n" + legend + editNote + "Press Enter to run.";
            }
        }
        String matches = SYNTAX.stream().map(syntax -> syntax.split(" ", 2)[0])
                .filter(word -> word.startsWith(command)).collect(Collectors.joining("  |  "));
        return matches.isEmpty() ? "Type help, then press Enter for the command list."
                : "Matching commands: " + matches + "\nComplete a command, then press Enter to run it.";
    }
}
