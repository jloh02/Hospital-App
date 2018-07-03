package com.example.jonathan.hospitalapp2;

public class careFieldItem {
    String name;
    String contactNumber;
    String email;
    String address;
    String patientEmail;

    public careFieldItem(String n, String cN, String e, String a, String pE) {
        this.name = n;
        this.contactNumber = cN +"";
        this.email = e;
        this.address = a;
        this.patientEmail = pE;
    }

    String getName() {
        return this.name;
    }
}