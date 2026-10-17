package com.example.morse.service;

import java.util.HashMap;
import java.util.Map;

public final class MorseCodec {
    private static final Map<Character, String> TO_MORSE = new HashMap<>();
    private static final Map<String, Character> FROM_MORSE = new HashMap<>();

    static {
        add('A', ".-"); add('B', "-..."); add('C', "-.-."); add('D', "-.."); add('E', ".");
        add('F', "..-."); add('G', "--."); add('H', "...."); add('I', ".."); add('J', ".---");
        add('K', "-.-"); add('L', ".-.."); add('M', "--"); add('N', "-."); add('O', "---");
        add('P', ".--."); add('Q', "--.-"); add('R', ".-."); add('S', "..."); add('T', "-");
        add('U', "..-"); add('V', "...-"); add('W', ".--"); add('X', "-..-"); add('Y', "-.--"); add('Z', "--..");
        add('0', "-----"); add('1', ".----"); add('2', "..---"); add('3', "...--"); add('4', "....-");
        add('5', "....."); add('6', "-...."); add('7', "--..."); add('8', "---.."); add('9', "----.");
        add('.', ".-.-.-"); add(',', "--..--"); add('?', "..--.."); add('!', "-.-.--");
        add('\'', ".----."); add('"', ".-..-."); add(':', "---..."); add(';', "-.-.-.");
        add('=', "-...-"); add('+', ".-.-."); add('-', "-....-"); add('_', "..--.-");
        add('(', "-.--."); add(')', "-.--.-"); add('/', "-..-."); add('@', ".--.-.");
        add('&', ".-..."); add('$', "...-..-");
    }

    private MorseCodec() { }

    private static void add(char character, String code) {
        TO_MORSE.put(character, code);
        FROM_MORSE.put(code, character);
    }

    public static String encode(String text) {
        StringBuilder output = new StringBuilder();
        boolean pendingWordBreak = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (Character.isWhitespace(character)) {
                if (output.length() > 0) pendingWordBreak = true;
                continue;
            }

            char normalized = Character.toUpperCase(character);
            String code = TO_MORSE.get(normalized);
            if (code == null) {
                throw new IllegalArgumentException("Unsupported character '" + character
                        + "' at position " + (index + 1) + ". Use A–Z, 0–9, spaces, or common punctuation.");
            }
            if (output.length() > 0) output.append(pendingWordBreak ? " / " : " ");
            output.append(code);
            pendingWordBreak = false;
        }
        return output.toString();
    }

    public static String decode(String morse) {
        String trimmed = morse.trim();
        if (trimmed.isEmpty()) return "";
        String[] words = trimmed.replace('|', '/').split("\\s*/\\s*", -1);
        StringBuilder output = new StringBuilder();
        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String word = words[wordIndex].trim();
            if (word.isEmpty()) {
                throw new IllegalArgumentException("Each word separator (/) must have Morse code on both sides.");
            }
            if (wordIndex > 0) output.append(' ');
            for (String code : word.split("\\s+")) {
                Character character = FROM_MORSE.get(code);
                if (character == null) {
                    throw new IllegalArgumentException("Unknown Morse sequence '" + code + "'.");
                }
                output.append(character);
            }
        }
        return output.toString();
    }
}
