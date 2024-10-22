package org.sponsorschoose.translate.model;

import java.util.*;

public class TranslateEntry {
     private String or;
     private Map<String, String> tr;
     
     public String getOr() { 
          return or;
     }

     public void setOr(String or) {
           this.or = or;
     }

     public Map<String, String> getTr() {
          return tr;
     }
     
     public void setTr(Map<String, String> tr) {
          this.tr = tr;
     }

}