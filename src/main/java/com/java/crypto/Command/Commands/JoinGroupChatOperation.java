package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Client;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class JoinGroupChatOperation implements Action, Parseable
{
    private static final String FLAG = "-n";

    private Sender sender;
    private String gcName;

    public JoinGroupChatOperation(){}
    public JoinGroupChatOperation(Sender sender, String input)
    {
        this.sender = sender;
        this.parse ( input );
    }

    @Override
    public void parse( String input )
    {
        String[] tokens= input.split ( " " );
        String arg     = "";
        boolean isFlag = false;

        for ( String token : tokens )
        {
            if ( token.contains ( FLAG ) ) isFlag = true; 
            else if ( isFlag )             arg    = token;
            else continue;
        }

        this.gcName = arg;
    }

    @Override
    public void execute()
    {
        if ( gcName != null )
        {
            // TODO, same methodolgy for the private messaging. 
            String msg       = this.gcName;
            PACKET_TYPE type = PACKET_TYPE.JOIN;
            Packet packet    = new Packet ( msg, type ); 
            sender.send ( packet );
        }
        else { System.out.println( "[ERROR] you need to specify the group chat name you intend to join. Use ``/help -c join``" ); }
    }

}