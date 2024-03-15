package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SplitString {
    public static void main(String[] args) {
        String sentence = "I can't wait. for the start of the weekend .";

        sentence = sentence.trim();
        String[] words = sentence.split("(?=[.,!?])|(?<=[.,!?])|\\s+");

        // Filter out empty strings using Stream
        ArrayList<String> result = Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println(result);
        System.out.println(result.size());
    }
}

