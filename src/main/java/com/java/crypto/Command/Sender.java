package com.java.crypto.Command;

import java.io.IOException;
import java.io.OutputStream;

import com.java.crypto.Packet.Packet;


// the Receive class ( performs the actual actions )
public class Sender {
    private OutputStream os; 
    public Sender(){}
    public Sender( OutputStream os ) { this.os = os;}

    public void send ( Packet packet )
    {
        try{
            os.write(packet.output());
            os.flush();
        }
        catch( IOException e ) { e.printStackTrace(); } // let's have an interface ( for passing down function in the arguments )
    }
}
