package org.sponsorschoose.translate.utils;

import java.util.List;
import java.util.ArrayList;
import org.sponsorschoose.translate.model.ParseMode;

public class DecodeManager {

        final public static int criticalLimit = 5000;
        final public static int limitReserve = 10;
        final public static String defaultMode = "ma";

        final public static String defaultSerarator = "(0).";
        final public static String PreDecodeWord = ". ";
        final public static String PostDecodeWord = " ";

        final public static int UUIDAndSpaceLength = 36 + 1;

        public static List<String> decodeWordBatch(String buf, String separator, String mode, String dstLang) {
                buf = buf.trim();
                String id = buf.substring(0, 36);
                buf = buf.substring(UUIDAndSpaceLength);
                String decodeWord = separator + PostDecodeWord;
                int digitPosInDecodeWord = TextParsingUtils.findFirstDigit(decodeWord);
                int method = TextParsingUtils.detectDigitAtPos(decodeWord, digitPosInDecodeWord);
                if (digitPosInDecodeWord < 0) {
                        digitPosInDecodeWord = 0;
                }
                String partFirst = decodeWord.substring(0, digitPosInDecodeWord);
                String partLast = decodeWord.substring(digitPosInDecodeWord + 1);
                List<String> res = new ArrayList<String>(1024);
                int pos = 0;
                int n = buf.length();
                int count = 1;
                while (pos < n) {
                        while (pos < n && buf.charAt(pos) <= 32)
                                pos++;
                        int startPos = pos;
                        String t = partFirst + (method == 1 ? ControlWord.getControlWord(count, dstLang)
                                        : (method == 2 ? ControlWord.generateWordByLetters(id, count)
                                                        : (method == 0 ? Integer.toString(count) : "")))
                                        + partLast;
                        pos = buf.indexOf(t, pos);
                        if (pos < 0) {
                                pos = n;
                        }
                        count++;
                        int shift = 0;
                        if (pos == startPos) {
                                throw new RuntimeException("Omitted translation at " + pos);
                        }
                        if (pos > 0) {
                                while (pos - shift - 1 >= 0 && buf.charAt(pos - shift - 1) <= 32)
                                        shift++;
                                if (pos - shift - 1 >= 0 && buf.charAt(pos - shift - 1) == '.')
                                        shift++;
                        }
                        String word = buf.substring(startPos, pos - shift).trim();
                        res.add(word);
                        pos += t.length();
                }
                return res;
        }

        public static void validateParseMode(ParseMode parseMode) {
                if (parseMode.getLimit() <= 0 || parseMode.getLimit() >= criticalLimit) {
                        parseMode.setLimit(criticalLimit - limitReserve);
                }
                if (parseMode.getMode() == null || parseMode.getMode().length() != 2) {
                        parseMode.setMode(defaultMode);
                }
                if (parseMode.getSeparator() == null || parseMode.getSeparator().length() == 0) {
                        parseMode.setSeparator(defaultSerarator);
                } else {
                        parseMode.setSeparator(parseMode.getSeparator().trim());
                }
        }

        public static int encodeWordBatch(String id, List<String> words, StringBuilder buf, ParseMode parseMode) {
                validateParseMode(parseMode);
                int limit = parseMode.getLimit();
                String decodeWord = parseMode.getSeparator();
                int digitPosInDecodeWord = TextParsingUtils.findFirstDigit(decodeWord);
                int method = TextParsingUtils.detectDigitAtPos(decodeWord, digitPosInDecodeWord);
                if (digitPosInDecodeWord < 0) {
                        digitPosInDecodeWord = 0;
                }
                String wholePreDecode = PreDecodeWord + parseMode.getSeparator().substring(0, digitPosInDecodeWord);
                String wholePostDecode = parseMode.getSeparator().substring(digitPosInDecodeWord + 1) + PostDecodeWord;
                String lang = parseMode.getSrcLang();
                int extraSize = wholePreDecode.length() + wholePostDecode.length();
                switch (method) {
                        case 0:
                                extraSize += 4;
                                break;
                        case 1:
                                extraSize += ControlWord.getMaxLength(lang);
                                break;
                        case 2:
                                extraSize += 10;
                }
                buf.append(id);
                buf.append(' ');
                int n = words.size();
                for (int i = 0; i < n; i++) {
                        String word = words.get(i);
                        if (i != 0) {
                                int nextLimit = word.length() + extraSize + buf.length();
                                if (nextLimit >= limit) {
                                        return i;
                                }
                                buf.append(wholePreDecode);
                                switch (method) {
                                        case 0:
                                                buf.append(i);
                                                break;
                                        case 1:
                                                buf.append(ControlWord.getControlWord(i, lang));
                                                break;
                                        case 2:
                                                ControlWord.addGeneratedWordByLetters(buf, id, i);
                                                break;
                                }
                                buf.append(wholePostDecode);
                        }
                        buf.append(word);
                }
                return n;
        }

}
