package com.barbera.barberaconsumerapp;

public class BookingModel {

    private String summary;
    private String amount;
    private String date;
    private String time;
    private String address;
    private String docId;

    public BookingModel(String summary, String amount, String date,String time ,String address,String docId) {
        this.summary = summary;
        this.amount = amount;
        this.date = date;
        this.time=time;
        this.address = address;
        this.docId=docId;
    }

    public String getDocId() {
        return docId;
    }

    public String getTime() {
        return time;
    }

    public String getSummary() {
        return summary;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getAddress() {
        return address;
    }
}
