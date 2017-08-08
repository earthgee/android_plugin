package com.earthgee.plugin.provider;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class Article {

    private int id;
    private String title;
    private String abs;
    private String url;

    public Article(int id, String title, String abs, String url) {
        this.id = id;
        this.title = title;
        this.abs = abs;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
