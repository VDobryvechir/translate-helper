package org.sponsorschoose.translate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sponsorschoose.translate.model.TranslateEntry;
import org.sponsorschoose.translate.utils.TextParsingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class LanguageFixService {

    private static Logger logger = LoggerFactory.getLogger(LanguageFixService.class);

    @Autowired
    private TranslateService translateService;

    public String fixCommonDictionary(String src, String dst, String kind) {
        StringBuilder res = new StringBuilder();
        try {
            Map<String, TranslateEntry> data = translateService.readTranslationFile(src, kind);
            int count = cleanTranslation(src, dst, data);
            res.append(count);
            res.append(" cleaned ");
            translateService.saveTranslationFile(src, kind, data);
            String stat = TextParsingUtils.getWordStatistics(data, dst);
            res.append(stat);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            res.append(ex.getMessage());
        }
        return res.toString();
    }

    private int cleanTranslation(String src, String dst, Map<String, TranslateEntry> data) {
        int count = 0;
        for (Map.Entry<String, TranslateEntry> entry : data.entrySet()) {
            TranslateEntry translate = entry.getValue();
            String orig = translate.getOr();
            Map<String, String> all = translate.getTr();
            if (orig != null && all != null) {
                String dest = all.get(dst);
                if (dest != null && dest.length() > 0) {
                    all.remove(dst);
                    count++;
                }
            }
        }
        return count;
    }
}
