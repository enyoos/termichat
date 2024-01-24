package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Action;
import com.java.crypto.Client;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

import com.java.crypto.Command.Commands.helpers.*;

public class JoinGroupChatOperation implements Action, Parseable
{
    private static final String FLAG_NAME_SPEC_GC = "-n";

    private Sender sender;
    private Parser parser;
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
        this.parser = new Parser ( new Lexer ( input ) );
        this.parser.parse();
    }

    @Override
    public boolean eval(){

        ArrayList<String> name_s = this.parser.struct.get ( FLAG_NAME_SPEC_GC );
        boolean is_name          = validate_name( name_s );

        if (is_name)
        {
            this.gcName = name_s.get ( 0 ); 
            return true;
        }

        System.out.println( "[ERROR] Name of the group chat is not specified or supplied multiple names" );
        return false;
    }

    private boolean validate_name ( ArrayList<String> name_s )
    { return name_s != null && name_s.size() == 1; }

    @Override
    public void execute()
    {
        boolean is_eval = this.eval();
        if ( is_eval )
        {
            String msg       = this.gcName;
            PACKET_TYPE type = PACKET_TYPE.JOIN;
            Packet packet    = new Packet ( msg, type ); 
            sender.send ( packet );
        }
        
    }

}