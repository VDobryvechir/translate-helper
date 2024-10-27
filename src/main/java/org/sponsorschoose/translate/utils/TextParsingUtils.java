package org.sponsorschoose.translate.utils;

import java.util.*;
import java.util.stream.Collectors;

import org.sponsorschoose.translate.model.TranslateEntry;
import org.sponsorschoose.translate.model.WordInfo;

public class TextParsingUtils {

    public static List<String> prepareLinesForTranslation(List<String> src) {
        List<String> res = src.stream().map((String line) -> {
            int n = line.length();
            int count = 0;
            for (int i = 0; i < n; i++) {
                char c = line.charAt(i);
                if (c == '(') {
                    count++;
                } else if (c == ')') {
                    count--;
                }
            }
            while (count > 0) {
                line += ")";
                count--;
            }
            while (count < 0) {
                line = "(" + line;
                count++;
            }
            return line.substring(0, 1).toUpperCase() + line.substring(1);
        }).collect(Collectors.toList());
        return res;
    }

    public static String getWordStatistics(Map<String, TranslateEntry> data, String dst) {
        int total = 0;
        int totalEntries = 0;
        int left = 0;
        int totalChars = 0;
        for (Map.Entry<String, TranslateEntry> entry : data.entrySet()) {
            TranslateEntry translate = entry.getValue();
            totalEntries++;
            if (translate.getTr() != null && translate.getOr() != null && translate.getOr().length() > 0) {
                Map<String, String> tr = translate.getTr();
                String word = tr.get(dst);
                total++;
                if (word == null || word.length() == 0) {
                    String add = translate.getOr();
                    left++;
                    totalChars += add.length() + 6;
                }
            }
        }
        int batches = totalChars / (5000 - 37);
        return dst + " words left: " + left + " / " + total + " / " + totalEntries + " Characters = " + totalChars
                + " Batches = " + batches;
    }

    public static ArrayList<String> extractArrayLine(String src, String dst, String kind, int totalLimit,
            int unitWeight,
            WordInfo wordInfo, Map<String, TranslateEntry> data) throws Exception {
        wordInfo.setStatistics(getWordStatistics(data, dst));
        ArrayList<String> words = new ArrayList<>(1024);
        int weight = 0;
        for (Map.Entry<String, TranslateEntry> entry : data.entrySet()) {
            TranslateEntry translate = entry.getValue();
            if (translate.getTr() != null && translate.getOr() != null && translate.getOr().length() > 0) {
                Map<String, String> tr = translate.getTr();
                String word = tr.get(dst);
                if (word == null || word.length() == 0) {
                    String add = translate.getOr();
                    weight += add.length() + unitWeight;
                    if (weight > totalLimit) {
                        break;
                    }
                    words.add(add);
                }
            }
        }
        return words;
    }

}
