package com.java.crypto;

import java.util.ArrayList;
import com.java.crypto.Entity;


public class Group
{
    public ArrayList<Entity> clients;
    public String admin;
    public String name ;

    public Group() {}
    public Group(String admin, String name) { 
        this.admin = admin;
        this.name = name; 
        this.clients = new ArrayList<>(); 
    }

    @Override
    public String toString () { return "{admin : " + this.admin + ", name : " + this.name  + ", members : " + this.clients.toString(); }
}