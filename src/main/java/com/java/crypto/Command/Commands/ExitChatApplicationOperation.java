package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ExitChatApplicationOperation implements Action{
    private Sender sender;    

    public ExitChatApplicationOperation() {}
    public ExitChatApplicationOperation( Sender sender ){ this.sender = sender; }

    @Override
    public void execute() {

        String msg = "exit";
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;

        Packet packet = new Packet(msg, type);
        sender.send( packet );
    }

}
