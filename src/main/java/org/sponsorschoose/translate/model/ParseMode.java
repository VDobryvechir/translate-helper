package org.sponsorschoose.translate.model;

public class ParseMode {
    private int limit;
    private String separator;
    private String mode;
    private String srcLang, dstLang;

    public ParseMode(int limit, String separator, String mode, String srcLang, String dstLang) {
        this.limit = limit;
        this.separator = separator;
        this.mode = mode;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getDstLang() {
        return dstLang;
    }

}
