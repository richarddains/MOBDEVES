package com.example.jotty;

public class Note {
    private String id;
    private String title;
    private String subtitle;
    private String content;
    private String dateTime;
    private boolean isPinned;

    public Note() {}

    public Note(String title, String subtitle, String content, String dateTime) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.dateTime = dateTime;
        this.isPinned = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}
