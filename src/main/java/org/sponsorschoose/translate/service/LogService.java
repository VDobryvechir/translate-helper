package org.sponsorschoose.translate.service;

import java.util.*;
import java.io.*;

public class LogService {

    public static void logListComparison(String path, List<String> b1, List<String> b2) {
        StringBuffer sb = new StringBuffer();
        sb.append("Size 1: ");
        int n1 = b1.size();
        sb.append(n1);
        sb.append(" Size 2:");
        int n2 = b2.size();
        sb.append(n2);
        sb.append("\n");
        int n = n1 < n2 ? n2 : n1;
        for (int i = 0; i < n; i++) {
            String s1 = i < n1 ? b1.get(i) : "### 1 ###";
            String s2 = i < n2 ? b2.get(i) : "### 2 ###";
            sb.append("** ");
            sb.append(s1);
            sb.append(" **  ** ");
            sb.append(s2);
            sb.append(" **\n");
        }
        logSave(path, sb.toString());
    }

    public static void logBuffer(String path, String data) {
        StringBuffer sb = new StringBuffer();
        sb.append(data);
        logSave(path, sb.toString());
    }

    public static void logSave(String path, String data) {
        try {
            PrintWriter p = new PrintWriter(path);
            p.println(data);
            p.close();
        } catch (Exception ex) {
            System.out.println("Error saving " + path + ": " + ex.toString());
        }
    }

    public static void logPortion(String path, List<String> words, List<String> origWords, String buf, String bufOrig) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        logListComparison(path + "/lists.txt", words, origWords);
        logBuffer(path + "/translation.txt", buf);
        logBuffer(path + "/origin.txt", bufOrig);
    }
}
