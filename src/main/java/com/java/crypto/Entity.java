package com.java.crypto;

import java.net.Socket;

public final class Entity {

    private Socket socket; 
    private String name;

    public Entity( Socket socket, String name ) { this.socket = socket; this.name = name;}
    public Entity(){}
    public Entity( Socket socket ) { this ( socket, null );}
    public Entity( String name ) { this ( null, name );}

    public void setName ( String name ) { this.name = name;}
    public void setSocket( Socket socket ) { this.socket = socket; }
    public Socket getSocket ( ) { return this.socket; }
    public String getName ( ) { return this.name ;}
}
