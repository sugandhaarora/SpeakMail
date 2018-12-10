package com.speakmail.database.model;

public class Draft {
    public static final String TABLE_NAME = "drafts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FROM = "mail_from";
    public static final String COLUMN_TO = "mail_to";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_MESSAGE = "message";

    private int id;
    private String from;
    private String to;
    private String subject;
    private String message;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FROM + " TEXT, "
                    + COLUMN_TO + " TEXT, "
                    + COLUMN_SUBJECT + " TEXT, "
                    + COLUMN_MESSAGE + " TEXT"
                    + ")";

    public Draft() {
    }

    public Draft(int id, String from, String to, String subject, String message) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

