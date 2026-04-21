package com.example.bezierqrcode;

public class Participant {
    private String uid;
    private String name;

    public Participant() {}

    public Participant(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
