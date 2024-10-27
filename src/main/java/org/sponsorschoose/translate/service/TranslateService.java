package org.sponsorschoose.translate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.sponsorschoose.translate.model.*;
import org.sponsorschoose.translate.utils.DecodeManager;
import org.sponsorschoose.translate.utils.TextParsingUtils;

import java.util.*;
import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Service
public class TranslateService {

    private static Logger logger = LoggerFactory.getLogger(TranslateService.class);

    @Autowired
    TranslationRepository translationRepository;

    @Value("${app.translate.path}")
    private String appTranslatePath;

    public WordInfo getNextPortion(String src, String dst, String kind, ParseMode parseMode) {
        try {
            String id = UUID.randomUUID().toString();
            WordInfo wordInfo = new WordInfo();
            ArrayList<String> words = getNextPortion(src, dst, kind, 4990, 1, wordInfo);
            if (words == null || words.size() == 0) {
                wordInfo.setWords("!!NOTHING LEFT!!");
                return wordInfo;
            }
            StringBuilder sb = new StringBuilder();
            List<String> wordCopy = TextParsingUtils.prepareLinesForTranslation(words);
            int n = DecodeManager.encodeWordBatch(id, wordCopy, sb, parseMode);
            String res = sb.toString();
            List<String> origWords = words;
            if (n != words.size()) {
                origWords = words.subList(0, n);
                wordCopy = wordCopy.subList(0, n);
            }
            ensureWordBatch(id, wordCopy, res, parseMode.getSeparator(), parseMode.getMode(), src);
            TranslationBlock tb = new TranslationBlock(id, origWords, res, parseMode.getSeparator(),
                    parseMode.getMode());
            translationRepository.save(tb);
            wordInfo.setWords(res);
            return wordInfo;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return reportError(ex.toString());
        }
    }

    private WordInfo reportError(String message) {
        WordInfo wordInfo = new WordInfo();
        wordInfo.setStatistics(message);
        wordInfo.setWords("!!error");
        return wordInfo;
    }

    public WordInfo saveNextPortion(String src, String dst, String kind, String buf, ParseMode parseMode) {
        buf = buf.trim();
        if (buf.length() < 38) {
            return reportError("Too small buffer " + buf);
        }
        try {
            String id = decodeIdFromBatch(buf);
            TranslationBlock tb = translationRepository.find(id);
            if (tb == null) {
                throw new RuntimeException(id + " not found");
            }
            List<String> words = DecodeManager.decodeWordBatch(buf, tb.getSeparator(), tb.getMode(), dst);
            List<String> origWords = tb.getWords();
            if (words.size() != origWords.size()) {
                LogService.logPortion("/tmp/" + id, words, origWords, buf, tb.getText());
                throw new RuntimeException(words.size() + " words received, but expected " + origWords.size());
            }
            Map<String, TranslateEntry> data = readTranslationFile(src, kind);
            insertTranslations(data, dst, origWords, words);
            saveTranslationFile(src, kind, data);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return reportError(ex.toString());
        }
        return getNextPortion(src, dst, kind, parseMode);
    }

    private void insertTranslations(Map<String, TranslateEntry> data, String dstLang, List<String> origWords,
            List<String> dstWords) {
        int n = origWords.size();
        for (int i = 0; i < n; i++) {
            String orig = origWords.get(i);
            String dst = dstWords.get(i);
            TranslateEntry entry = data.get(orig.toLowerCase());
            if (entry == null) {
                throw new RuntimeException("Word " + orig + " is not present in the database");
            }
            entry.getTr().put(dstLang, dst);
        }
    }

    public void cleanAll() {
        translationRepository.clean();
    }

    private String getTranslationFileName(String src, String kind) {
        return appTranslatePath.replace("[kind]", kind).replace("[src]", src);
    }

    private Map<String, TranslateEntry> readTranslationFile(String src, String kind) throws Exception {

        String fileName = getTranslationFileName(src, kind);
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<LinkedHashMap<String, TranslateEntry>> typeRef = new TypeReference<LinkedHashMap<String, TranslateEntry>>() {
        };
        Map<String, TranslateEntry> mapEntries = mapper.readValue(new File(fileName), typeRef);
        return mapEntries;
    }

    private void saveTranslationFile(String src, String kind, Map<String, TranslateEntry> data) throws Exception {

        String fileName = getTranslationFileName(src, kind);
        ObjectMapper mapper = new ObjectMapper();
        String jsonText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(jsonText);
        }
    }

    public String makeWordStatistics(String src, String dst, String kind) {
        try {
            Map<String, TranslateEntry> data = readTranslationFile(src, kind);
            String res = TextParsingUtils.getWordStatistics(data, dst);
            return "Src=" + src + " kind=" + kind + ": " + res;
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private ArrayList<String> getNextPortion(String src, String dst, String kind, int totalLimit, int unitWeight,
            WordInfo wordInfo) throws Exception {
        Map<String, TranslateEntry> data = readTranslationFile(src, kind);
        return TextParsingUtils.extractArrayLine(src, dst, kind, totalLimit, unitWeight, wordInfo, data);
    }

    private void ensureWordBatch(String id, List<String> words, String buf, String separator, String mode,
            String lang) {
        String newId = decodeIdFromBatch(buf);
        if (!newId.equals(id)) {
            throw new RuntimeException("Ids are different: " + id + " and " + newId);
        }
        if (buf.length() >= DecodeManager.criticalLimit) {
            throw new RuntimeException("Buffer exceeded critical limit " + buf.length() + " where limit is "
                    + DecodeManager.criticalLimit);
        }
        List<String> newWords = DecodeManager.decodeWordBatch(buf, separator, mode, lang);
        if (!newWords.equals(words)) {
            throw new RuntimeException("Words are different");
        }
    }

    private String decodeIdFromBatch(String buf) {
        String id = buf.trim().substring(0, 36);
        UUID.fromString(id);
        return id;
    }

}