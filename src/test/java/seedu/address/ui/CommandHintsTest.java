package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;

public class CommandHintsTest {
    @Test
    public void blankInput_showsDefaultGuidance() {
        for (String draft : List.of("", "  ", "\t\n")) {
            assertEquals(CommandHints.DEFAULT_HINT, CommandHints.forInput(draft));
        }
    }

    @Test
    public void existingCommands_showSyntaxMatchingAcceptedParserInput() throws Exception {
        // Each syntax has a corresponding accepted input, including optional and repeatable arguments.
        String[][] commands = {
            {"add sn/STUDENT_NUMBER n/NAME e/EMAIL [tg/TELEGRAM] [t/TAG]...",
                "add sn/A0123456X n/Alice Tan e/alice@example.com tg/alice_tan t/friends t/tutorial"},
            {"edit STUDENT_INDEX [sn/STUDENT_NUMBER] [n/NAME] [e/EMAIL] [tg/TELEGRAM] [t/TAG]...",
                "edit 1 sn/A0123456X n/Alice Tan e/alice@example.com tg/ t/friends t/tutorial"},
            {"delete STUDENT_INDEX", "delete 1"},
            {"find NAME [MORE_NAMES]...", "find Alice Benson"},
            {"list", "list"},
            {"addclass m/MODULE_CODE c/CLASS_NAME", "addclass m/CS2103 c/F10"},
            {"editclass CLASS_INDEX [m/MODULE_CODE] [c/CLASS_NAME]", "editclass 1 m/CS2103 c/F10"},
            {"deleteclass CLASS_INDEX", "deleteclass 1"},
            {"listclass", "listclass"},
            {"listbyclass CLASS_INDEX [t/THRESHOLD]", "listbyclass 1 t/2"},
            {"listbystudent STUDENT_INDEX", "listbystudent 1"},
            {"assign STUDENT_INDEX CLASS_INDEX", "assign 1 2"},
            {"unassign STUDENT_INDEX CLASS_INDEX", "unassign 1 2"},
            {"addattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD", "addattendance 1 2 d/2020-01-03"},
            {"deleteattendance STUDENT_INDEX CLASS_INDEX d/YYYY-MM-DD", "deleteattendance 1 2 d/2020-01-03"},
            {"help", "help"}, {"clear", "clear"}, {"exit", "exit"}
        };
        AddressBookParser parser = new AddressBookParser();
        for (String[] command : commands) {
            parser.parseCommand(command[1]);
            String word = command[1].split(" ")[0];
            String hint = CommandHints.forInput(word);
            assertTrue(hint.startsWith(command[0]), word);
            assertTrue(hint.endsWith("Press Enter to run."), word);
            assertEquals(hint, CommandHints.forInput("  " + command[1] + "  "), word);
            if (command[0].contains("[")) {
                assertTrue(hint.contains("Unbracketed arguments are required; [ ] optional; ... repeatable."));
            } else if (command[0].contains(" ")) {
                assertTrue(hint.contains("Arguments are required."));
            } else {
                assertTrue(hint.contains("No arguments needed."));
            }
        }
        for (String edit : List.of("edit", "editclass")) {
            assertTrue(CommandHints.forInput(edit).contains("Choose at least one field to edit."));
        }
    }

    @Test
    public void partialAndUnknownWords_offerGuidanceWithoutValidation() {
        assertEquals("Matching commands: list  |  listclass  |  listbyclass  |  listbystudent\n"
                + "Complete a command, then press Enter to run it.", CommandHints.forInput("li"));
        assertEquals("Matching commands: delete  |  deleteclass  |  deleteattendance\n"
                + "Complete a command, then press Enter to run it.", CommandHints.forInput("del"));
        assertTrue(CommandHints.forInput("add").startsWith("add sn/"), "Exact words take precedence");
        for (String unknown : List.of("LIST", "unknown", "listbyclassx")) {
            assertEquals("Type help, then press Enter for the command list.", CommandHints.forInput(unknown));
        }
        for (String draft : List.of("listbyclass 0", "listbyclass abc", "listbyclass 999999999999999999")) {
            assertEquals(CommandHints.forInput("listbyclass"), CommandHints.forInput(draft));
            assertFalse(CommandHints.forInput(draft).contains("Invalid"));
        }
    }
}
