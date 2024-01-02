package com.java.crypto.Packet;

public enum PACKET_TYPE {
    // DISCONNECTING, CONNECTING ( WILL NOT BE USED, SINCE THE CONNECTION IS ALREADY ESTABLISHED )
    // SEND ( I.E BROADCAST ), PRIVATE ( I.E DM ), RESPONSE ( server -> client ) in response to the client.
    DISCONNECT ( -1 ), CONNECT ( 1 ) , SEND ( 2 ) , PRIVATE ( 0 ), RESPONSE(3);

    private final byte value;
    private PACKET_TYPE( int b ) { this.value = ( byte ) b; }
    public byte getValue(){ return value;}
}