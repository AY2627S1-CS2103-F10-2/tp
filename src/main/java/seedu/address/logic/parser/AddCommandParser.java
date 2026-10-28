package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM;

import java.util.Set;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentNumber;
import seedu.address.model.person.Telegram;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ParserUtil.rejectUnknownPrefixes(args,
                PREFIX_STUDENT_NUMBER, PREFIX_NAME, PREFIX_EMAIL, PREFIX_TELEGRAM, PREFIX_TAG);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_STUDENT_NUMBER, PREFIX_NAME, PREFIX_EMAIL, PREFIX_TELEGRAM, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        ParserUtil.requirePrefix(argMultimap, PREFIX_STUDENT_NUMBER, "STUDENT_NUMBER");
        ParserUtil.requirePrefix(argMultimap, PREFIX_NAME, "NAME");
        ParserUtil.requirePrefix(argMultimap, PREFIX_EMAIL, "EMAIL");

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT_NUMBER, PREFIX_NAME, PREFIX_EMAIL, PREFIX_TELEGRAM);
        StudentNumber studentNumber = ParserUtil.parseStudentNumber(argMultimap.getValue(PREFIX_STUDENT_NUMBER).get());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Telegram telegram = null;
        if (argMultimap.getValue(PREFIX_TELEGRAM).isPresent()) {
            telegram = ParserUtil.parseTelegram(argMultimap.getValue(PREFIX_TELEGRAM).get());
        }
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(studentNumber, name, email, telegram, tagList);

        return new AddCommand(person);
    }

}
