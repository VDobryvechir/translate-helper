package org.sponsorschoose.translate.utils;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;

public class ControlWord {

    private static Map<String, String> map0 = Stream.of(new String[][] {
            { "nb", "Blomster" },
            { "en", "Flowers" },
            { "nn", "Blomstrar" },
            { "uk", "Квіти" },
            { "de", "Blumen" },
            { "pl", "Kwiaty" },
            { "it", "Fiori" },
            { "es", "Flores" },
            { "da", "Blomster" },
            { "ru", "Цветы" },
            { "sv", "Blommor" },
            { "gr", "Λουλούδια" },
            { "pt", "Flores" },
            { "cz", "Květiny" },
            { "bg", "Цветя" },
            { "fr", "Fleurs" },

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static Map<String, String> map1 = Stream.of(new String[][] {
            { "bg", "Енциклопедия" },
            { "cz", "Encyklopedie" },
            { "da", "Encyklopædi" },
            { "de", "Enzyklopädie" },
            { "en", "Encyclopedia" },
            { "es", "Enciclopedia" },
            { "fr", "Encyclopédie" },
            { "gr", "Εγκυκλοπαιδεία" },
            { "it", "Enciclopedia" },
            { "nb", "Encyclopedia" },
            { "nn", "Encyclopedia" },
            { "pl", "Encyklopedia" },
            { "pt", "Enciclopédia" },
            { "ru", "Энциклопедия" },
            { "sv", "Encyklopedi" },
            { "uk", "Енциклопедія" },

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static Map<String, String> map2 = Stream.of(new String[][] {
            { "nb", "Kveld" },
            { "en", "Evening" },
            { "nn", "Kveld" },
            { "uk", "Вечір" },
            { "de", "Abend" },
            { "pl", "Wieczór" },
            { "it", "Sera" },
            { "es", "Tarde" },
            { "da", "Aften" },
            { "ru", "Вечер" },
            { "sv", "Afton" },
            { "gr", "Εσπερινός" },
            { "pt", "Noite" },
            { "cz", "Večer" },
            { "bg", "Вечер" },
            { "fr", "Soir" },

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static char[] firstLetter = new char[] { 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r',
            's', 't', 'p' };
    private static char[] firstCapital = new char[] { 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R',
            'S', 'T', 'P' };
    private static char[] secondLetter = new char[] { 'a', 'e', 'i', 'o', 'y', 'u', 'å', 'ø', 'æ', 'ö', 'ä', 'ü', 'w',
            'v', 'x', 'z' };
    private static String[] suffix = new String[] { "gata", "veien", "vegen", "vei" };

    private static Map<String, String>[] controlWords = new Map[] { map0, map1, map2 };

    public static String getControlWord(int order, String lang) {
        return controlWords[order % controlWords.length].get(lang);
    }

    public static int getMaxLength(String lang) {
        int n = controlWords.length;
        int maxLen = 1;
        for (int i = 0; i < n; i++) {
            String s = getControlWord(i, lang);
            int m = s.length();
            if (maxLen < m) {
                maxLen = m;
            }
        }
        return maxLen;
    }

    public static void addGeneratedWordByLetters(StringBuilder sb, String id, int n) {
        int suffixIndex = n & 3;
        n = (n >> 2) & 0xffff;
        sb.append(firstCapital[n & 0xf]);
        sb.append(secondLetter[id.charAt(2) & 0xf]);
        sb.append(firstLetter[(n >> 8) & 0xf]);
        sb.append(secondLetter[id.charAt(34) & 0xf]);
        sb.append(firstLetter[(n >> 16) & 0xf]);
        sb.append(secondLetter[(n >> 24) & 0xf]);
        sb.append(suffix[suffixIndex]);
    }

    public static String generateWordByLetters(String id, int n) {
        StringBuilder sb = new StringBuilder(10);
        addGeneratedWordByLetters(sb, id, n);
        return sb.toString();
    }
}
