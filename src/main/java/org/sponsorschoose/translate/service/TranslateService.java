package org.sponsorschoose.translate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sponsorschoose.translate.model.*;
import java.util.*;
import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class TranslateService {

        @Autowired
        TranslationRepository translationRepository;
        
        @Value("${app.translate.path}")
        private String appTranslatePath;
	
        public WordInfo getNextPortion(String src, String dst, String kind) {
           try {
             String id = UUID.randomUUID().toString();
             WordInfo wordInfo = new WordInfo();
             ArrayList<String> words = getNextPortion(src, dst, kind, 4990, 1, wordInfo);
             if (words ==null || words.size()==0) {
                  wordInfo.setWords("!!NOTHING LEFT!!");
                  return wordInfo;
             }
             StringBuilder sb = new StringBuilder();
             int n=encodeWordBatch(id, words, sb, 4990);
             String res = sb.toString();
             List<String> origWords = words;
             if (n!=words.size()) {
                origWords = words.subList(0,n);
             } 
             ensureWordBatch(id, origWords, res, 5000); 
             TranslationBlock tb = new TranslationBlock(id, origWords, res);
             translationRepository.save(tb);  
             wordInfo.setWords(res);
             return wordInfo;
           } catch(Exception ex) {
               return reportError(ex.toString());
           }
        }

        private WordInfo reportError(String message) {
             WordInfo wordInfo = new WordInfo();
             wordInfo.setStatistics(message);
             wordInfo.setWords("!!error");
             return wordInfo;
        }

        public WordInfo saveNextPortion(String src, String dst, String kind, String buf) {
            buf = buf.trim();
            if (buf.length()<38) {
                return reportError("Too small buffer "+buf);
            }
            try {
               String id = decodeIdFromBatch(buf);
               List<String> words = decodeWordBatch(buf);
               TranslationBlock tb = translationRepository.find(id);
               if (tb==null) {
                   throw new RuntimeException(id + " not found");
               } 
               List<String> origWords = tb.getWords();
               if (words.size()!=origWords.size()) {
                  throw new RuntimeException(words.size() + " words received, but expected " + origWords.size());
               } 
               Map<String, TranslateEntry> data = readTranslationFile(src, kind);
               insertTranslations(data, dst, origWords, words);
               saveTranslationFile(src, kind, data);
            } catch(Exception ex) {
                return reportError(ex.toString());
            }
            return getNextPortion(src, dst, kind);
        }

        private void insertTranslations(Map<String, TranslateEntry> data, String dstLang, List<String> origWords, List<String> dstWords) {
            int n = origWords.size();
            for(int i=0;i<n;i++) {
                String orig = origWords.get(i);
                String dst = dstWords.get(i);
                TranslateEntry entry = data.get(orig.toLowerCase());
                if (entry==null) {
                    throw new RuntimeException("Word "+orig + " is not present in the database"); 
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
             TypeReference<LinkedHashMap<String, TranslateEntry>> typeRef = new TypeReference<LinkedHashMap<String, TranslateEntry>>() {};
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
                String res = getWordStatistics(data, dst);
                return "Src=" + src + " kind="+kind + ": " + res; 
            } catch(Exception ex) {
                return ex.toString();
            }
        }

        private String getWordStatistics(Map<String, TranslateEntry> data, String dst) {
             int total = 0;
             int totalEntries = 0;
             int left = 0;
             int totalChars = 0;
             for(Map.Entry<String, TranslateEntry> entry : data.entrySet()) {
                 TranslateEntry translate = entry.getValue();
                 totalEntries++;
                 if (translate.getTr()!=null && translate.getOr()!=null && translate.getOr().length()>0) {
                     Map<String,String> tr = translate.getTr();
		     String word = tr.get(dst);
                     total++;
                     if (word==null || word.length()==0) {
                         String add = translate.getOr();
                         left++;
                         totalChars += add.length() + 6;
                     }	
                 } 
             }
             int batches = totalChars / (5000 - 37);
             return dst +" words left: "+ left + " / " + total + " / " + totalEntries+ " Characters = "+totalChars+" Batches = "+batches; 
        }

        ArrayList<String> getNextPortion(String src, String dst, String kind,int totalLimit, int unitWeight, WordInfo wordInfo) throws Exception {
             Map<String, TranslateEntry> data = readTranslationFile(src, kind);
             wordInfo.setStatistics(getWordStatistics(data, dst));
             ArrayList<String> words = new ArrayList();
             int weight=0;
             for(Map.Entry<String, TranslateEntry> entry : data.entrySet()) {
                 TranslateEntry translate = entry.getValue();
                 if (translate.getTr()!=null && translate.getOr()!=null && translate.getOr().length()>0) {
                     Map<String,String> tr = translate.getTr();
		     String word = tr.get(dst);
                     if (word==null || word.length()==0) {
                         String add = translate.getOr();
                         weight += add.length() + unitWeight;
                         if (weight>totalLimit) {
                            break;
                         }
                         words.add(add);      
                     }	
                 } 
             }
             return words;   
        }

       private int encodeWordBatch(String id, List<String> words, StringBuilder buf, int limit) {
            buf.append(id);
            buf.append(' ');
            int n = words.size();
            for(int i=0;i<n;i++) {
                  String word = words.get(i);
                  int nextLimit = word.length() + 6 + buf.length();
                  if (nextLimit>=limit) {
                     return i;
                  } 
                  if (i!=0) {
                      buf.append(". y");
                      buf.append((char)(n % 10 + 48));
                      buf.append(". ");
                  } 
                  buf.append(word);  
            }
            return n;
       }

       private void ensureWordBatch(String id, List<String> words, String buf, int limit) {
           String newId = decodeIdFromBatch(buf);
           if (!newId.equals(id)) {
               throw new RuntimeException("Ids are different: "+id+" and "+ newId);
           } 
           if (buf.length()>=limit) {
               throw new RuntimeException("Buffer exceeded limit "+buf.length()+" where limit is "+limit);
           }
           List<String> newWords = decodeWordBatch(buf);
           if (!newWords.equals(words)) {
               throw new RuntimeException("Words are different");  
           }   
       }

       private String decodeIdFromBatch(String buf) {
           String id = buf.trim().substring(0, 36);
           UUID.fromString(id); 
           return id;
       }

       private List<String> decodeWordBatch(String buf) {
           buf = buf.trim().substring(37);
           char[] search = new char[6];
           search[0]='.';
           search[1]=' ';
           search[2]='y';
           search[3]='0';
           search[4]='.';
           search[5]=' ';
           List<String> res = new ArrayList<String>(1024);
           int pos = 0;
           int n = buf.length();
           int count = 1;
           while(pos<n) {
               while(pos<n && buf.charAt(pos)<=32) pos++;
               int startPos = pos;
               search[3] = (char) (count % 10 + 48);
               pos = buf.indexOf(new String(search), pos);
               if (pos<0) {
                   pos = n;
               }
               count++;
               String word = buf.substring(startPos, pos).trim();
               res.add(word);   
           } 
           return res; 
       } 

}                                                                                                             