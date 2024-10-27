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
            { "nb", "Fugl" },
            { "en", "Bird" },
            { "nn", "Fugl" },
            { "uk", "Птах" },
            { "de", "Vogel" },
            { "pl", "Ptak" },
            { "it", "Uccello" },
            { "es", "Pájaro" },
            { "da", "Fugl" },
            { "ru", "Птица" },
            { "sv", "Fågel" },
            { "gr", "Πουλί" },
            { "pt", "Pássaro" },
            { "cz", "Pták" },
            { "bg", "Птица" },
            { "fr", "Oiseau" },

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
}
