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

        // final public static int DecodeWordLength = DecodeWord.length();
        // final public static int DigitPosInDecodeWord = DecodeWord.indexOf("0");
        // final public static String WholePreDecode = PreDecodeWord +
        // DecodeWord.substring(0, DigitPosInDecodeWord);
        //
        final public static int UUIDAndSpaceLength = 36 + 1;

        public static List<String> decodeWordBatch(String buf, String separator, String mode, String dstLang) {
                buf = buf.trim().substring(UUIDAndSpaceLength);
                String decodeWord = separator + PostDecodeWord;
                char[] search = decodeWord.toCharArray();
                int digitPosInDecodeWord = decodeWord.indexOf('0');
                boolean isControlWord = digitPosInDecodeWord < 0;
                if (isControlWord) {
                        digitPosInDecodeWord = decodeWord.indexOf('1');
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
                        search[digitPosInDecodeWord] = (char) (count % 10 + 48);
                        String t = isControlWord ? partFirst + ControlWord.getControlWord(count, dstLang) + partLast
                                        : new String(search);
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
                if (parseMode.getSeparator() == null || parseMode.getSeparator().length() == 0 ||
                                (parseMode.getSeparator().indexOf('0') < 0
                                                && parseMode.getSeparator().indexOf('1') < 0)) {
                        parseMode.setSeparator(defaultSerarator);
                } else {
                        parseMode.setSeparator(parseMode.getSeparator().trim());
                }
        }

        public static int encodeWordBatch(String id, List<String> words, StringBuilder buf, ParseMode parseMode) {
                validateParseMode(parseMode);
                int limit = parseMode.getLimit();
                int digitPosInDecodeWord = parseMode.getSeparator().indexOf('0');
                boolean isControlWord = digitPosInDecodeWord < 0;
                if (isControlWord) {
                        digitPosInDecodeWord = parseMode.getSeparator().indexOf('1');
                }
                String wholePreDecode = PreDecodeWord + parseMode.getSeparator().substring(0, digitPosInDecodeWord);
                String wholePostDecode = parseMode.getSeparator().substring(digitPosInDecodeWord + 1) + PostDecodeWord;
                String lang = parseMode.getSrcLang();
                int extraSize = wholePreDecode.length() + wholePostDecode.length()
                                + (isControlWord ? ControlWord.getMaxLength(lang) : 1);
                buf.append(id);
                buf.append(' ');
                int n = words.size();
                for (int i = 0; i < n; i++) {
                        String word = words.get(i);
                        int nextLimit = word.length() + extraSize + buf.length();
                        if (nextLimit >= limit) {
                                return i;
                        }
                        if (i != 0) {
                                buf.append(wholePreDecode);
                                if (isControlWord) {
                                        buf.append(ControlWord.getControlWord(i, lang));
                                } else {
                                        buf.append((char) ((i % 10) + 48));
                                }
                                buf.append(wholePostDecode);
                        }
                        buf.append(word);
                }
                return n;
        }

}
