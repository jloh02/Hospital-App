package com.example.jonathan.hospitalapp2;

public class patFieldItem {
    String name;
    String contactNumber;
    String email;
    String address;

    public patFieldItem(String n, String cN, String e, String a) {
        this.name = n;
        this.contactNumber = cN +"";
        this.email = e;
        this.address = a;
    }

    String getName() {
        return this.name;
    }
}