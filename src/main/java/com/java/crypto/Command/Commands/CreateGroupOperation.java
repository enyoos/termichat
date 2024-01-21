package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

import static com.java.crypto.Encryption.Utils.*;

public class CreateGroupOperation implements Action, Parseable
{

    private static final String flag = "-n";
    private String gcName;
    private Sender sender;

    public CreateGroupOperation () {}
    public CreateGroupOperation (Sender sender, String input ){
        this.sender = sender;
        this.parse ( input );
    }

    @Override
    public void parse(String input)
    {
        boolean isFlag   = false;
        String arg       = ""   ;
        String[] tokens  = input.split ( " " );

        for ( String token : tokens )
        {
            if ( token.contains ( flag ) ) isFlag = true;
            else if ( isFlag )             arg    = token;
            else continue;
        }

        this.gcName = arg; 
    }

    @Override
    public void execute ( )
    {
        if ( gcName.isEmpty () ) {
            System.out.println( "[ERROR] What's the name of your group chat ? Use /help" );
            return;
        }
        else if ( !correctNameNomenclature( gcName ) )
        {
            System.out.println( "[ERROR] incorrect name. Your name must be in ascii letters only, " + 
                "without special characters and containing at least 4 characters." ); 

            return;
        }

        PACKET_TYPE type = PACKET_TYPE.CREATE;
        Packet      send = new Packet ( this.gcName, type );

        this.sender.send ( send );
    }

}