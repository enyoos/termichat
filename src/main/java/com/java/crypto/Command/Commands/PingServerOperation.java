package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class PingServerOperation implements Action{
    private Sender sender;

    public PingServerOperation( ) {}
    public PingServerOperation( Sender sender ) { this.sender = sender; }
    
    @Override
    public void execute() {
        String msg = "PING";
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
        Packet packet = new Packet(msg, type);

        this.sender.send ( packet );
    }
        
}