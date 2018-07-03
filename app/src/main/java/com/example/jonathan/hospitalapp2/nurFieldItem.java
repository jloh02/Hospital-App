package com.example.jonathan.hospitalapp2;

import java.util.ArrayList;

public class nurFieldItem {
    String name;
    String contactNumber;
    String email;
    ArrayList<Integer> level;

    public nurFieldItem(String n, String cN, String e, ArrayList<Integer> lvl) {
        this.name = n;
        this.contactNumber = cN +"";
        this.email = e;
        this.level = lvl;
    }

    String getName() {
        return this.name;
    }
}