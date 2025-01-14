package com.marvel.marveljourney.util;

import org.passay.*;
import org.passay.dictionary.*;
import org.passay.dictionary.sort.ArraysSort;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class PasswordValidatorUtil {

    private final PasswordValidator validator;

    public PasswordValidatorUtil() throws FileNotFoundException, IOException {
        validator = new PasswordValidator(Arrays.asList(
            new LengthRule(8, 30),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new WhitespaceRule(),
            new DictionaryRule(new WordListDictionary(WordLists.createFromReader(
                new FileReader[] { new FileReader("src/main/resources/common-passwords.txt") },
                false,
                new ArraysSort()
            )))
        ));
    }

    public boolean validate(String password) {
        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();
    }

    public List<String> getMessages(String password) {
        RuleResult result = validator.validate(new PasswordData(password));
        return validator.getMessages(result);
    }
}