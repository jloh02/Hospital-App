package com.example.jonathan.hospitalapp2;

import java.util.ArrayList;

public class docFieldItem {
    String name;
    String contactNumber;
    String email;
    ArrayList<String> specialisation;

    public docFieldItem(String n, String cN, String e, ArrayList<String> spec) {
        this.name = n;
        this.contactNumber = cN +"";
        this.email = e;
        this.specialisation = spec;
    }

    String getName() {
        return this.name;
    }
}