package com.java.crypto.Packet;

public final class Utils {


    public static String bytes2String( byte[] bytes )
    {
        String acc = "";

        // iterate through the whole array of bytes
        for ( byte byte_ : bytes )
        {
            // char is it self an integer ( 8 bit )
            acc += ( char ) byte_;
        }

        return acc;
    } 
}

