package com.java.crypto;

import java.util.ArrayList;

import com.java.crypto.Entity           ;
import com.java.crypto.CustomStruct.Pool;

public class Group
{
    public ArrayList<Entity> clients;
    public Pool           bannedPool;
    public String admin;
    public String name ;

    public Group() {}
    public Group(String admin, String name) { 

        this.admin      = admin;
        this.name       = name; 
        this.clients    = new ArrayList<>(); 
        this.bannedPool = new Pool();

    }

    @Override
    public String toString () 
    { 
        return "{admin : " + this.admin + ", name : " + this.name  + ", members : " + this.clients.toString() + ", wall of shame : " + this.bannedPool;
    }
}