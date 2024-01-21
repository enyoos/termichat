package com.java.crypto.Packet;

public enum PACKET_TYPE {

    DISCONNECT ( -1 ), // DISCONNECT EVENT
    CONNECT    (  1 ), // CONNECT EVENT (SOMEONE JOINS THE CHAT )
    PRIVATE    ( -4 ), // DM OPERATION
    RESPONSE   (  3 ), // COMMUNICATION FROM THE SERVER TO THE CLIENT
    KEY        (  4 ), // DIFFIE HELLMAN EXCHANGE SPECIFICATION PACKET
    BROADCAST  ( -2 ), // BROADCASTING A MESSAGE TO EVERYONE 
    REPEAT     (  5 ), // IF USERNAME IS USED, SEND REPEAT INTRUCTION
    CREATE     (  8 ), // tells the server to create a new subgroup
    JOIN       (  9 ), // JOIN NEW CHAT GROUP
    ERR        (  7 ), // when somehting is wrong, can contain some directive. ( name, ... ) -> to refactor
    OK          (  6 ); // OK STANDS FOR OLL KORRECT
			
    private final byte value;
    private PACKET_TYPE( int b ) { this.value = ( byte ) b; }
    public byte getValue(){ return value;}

}
