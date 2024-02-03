package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Action;
import com.java.crypto.Client;
import com.java.crypto.Command.Sender;
import com.java.crypto.Command.Commands.helpers.*;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ShowAllGroupChatsOperation implements Action, Parseable
{
    private static final String FLAG_SPEC_LIMIT = "-l";

    private Sender sender;
    private Integer limit;
    private Parser parser;

    public ShowAllGroupChatsOperation (){}
    public ShowAllGroupChatsOperation (Sender sender, String input) {
        this.sender = sender;
        parse ( input );
    }

    @Override
    public void parse( String input )
    {
        this.parser = new Parser ( new Lexer ( input ) );
        this.parser.parse();
    }

    @Override
    public boolean eval(){
        ArrayList<String> limit_s = this.parser.struct.get ( FLAG_SPEC_LIMIT );
        boolean is_limit          = validate_limit( limit_s );

        if ( is_limit )
        {
            try{
                this.limit = Integer.parseInt ( limit_s.get ( 0 ) );
            }
            catch ( NumberFormatException e )
            {
                System.out.println( "[ERROR] The Integer supplied is invalid" );
                return false;
            }
            return true;
        }
        
        return false;   
    }

    private boolean validate_limit ( ArrayList<String> limit_s )
    {
        boolean isnt_null = limit_s != null    ;
        if ( isnt_null ){ return limit_s.size() == 1; }
        return false;
    }


    @Override
    public void execute()
    {
        boolean is_eval = this.eval();

        if ( is_eval )
        {
            String msg       = this.limit + "," + Client.COMMANDS[8];
            PACKET_TYPE type = PACKET_TYPE.RESPONSE;
            Packet packet    = new Packet(msg, type);

            this.sender.send(packet);
        }
        else
        {
            String msg       = Client.COMMANDS[8];
            PACKET_TYPE type = PACKET_TYPE.RESPONSE;
            Packet packet    = new Packet(msg, type);

            this.sender.send(packet);
        }

    }
}