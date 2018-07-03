package com.example.jonathan.hospitalapp2;

public class DBAfieldItem {
    String name;
    String contactNumber;
    String email;

    public DBAfieldItem(String n, String cN, String e) {
        this.name = n;
        this.contactNumber = cN +"";
        this.email = e;
    }

    String getName() {
        return this.name;
    }
}