package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ListAllClientsNamesOperation implements Action{
    
    private Sender sender;
    
    public ListAllClientsNamesOperation(){}
    public ListAllClientsNamesOperation( Sender sender ){ this.sender = sender;}

    @Override
    public void execute() {

	System.out.println( "[LOGGING] sending the command /list" );
        String msg = "list";
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
        Packet packet = new Packet(msg, type);

        this.sender.send(packet);
    }

}
