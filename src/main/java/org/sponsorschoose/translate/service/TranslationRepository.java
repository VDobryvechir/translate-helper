package org.sponsorschoose.translate.service;

import org.springframework.stereotype.Service;
import org.sponsorschoose.translate.model.*;
import java.util.*;

@Service
public class TranslationRepository {

      private Map<String, TranslationBlock> data = new HashMap<>();

      public void save(TranslationBlock tb) {
          data.put(tb.getId(), tb);
      }

      public void clean() {
         data.clear();  
      } 

      public TranslationBlock find(String id) {
          return data.get(id); 
      }
}   