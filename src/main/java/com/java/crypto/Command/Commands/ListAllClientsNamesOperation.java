package com.java.crypto.Command.Commands;

import com.java.crypto.Client;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ListAllClientsNamesOperation implements Action, Parseable{

    private static final String FLAG_SPEC_LIMIT = "-l";

    private Sender sender;
    private Integer limit;
    private Parser parser;

    public ListAllClientsNamesOperation(){}
    public ListAllClientsNamesOperation( Sender sender, String input ){ this.sender = sender; this.parse ( input );}

    @Override
    public void parse ( String input ) 
    {
        this.parser = new Parser ( new Lexer ( input ) );
        this.parser.parse();
    }

    @Override
    public boolean eval()
    {
        ArrayList<String> limit_s = this.parser.struct.get ( FLAG_SPEC_LIMIT );
        boolean is_limit          = 
    }

    private boolean validate_limit ( ArrayList<String> limit_s )
    {
        boolean isnt_null = limit_s != null    ;
        boolean has_one   = limit_s.size() == 1;

        if ( isnt_null && has_one ) {
            try{
                Integer.parse ( limit_s.get ( 0 ) );
            }
        }
    }

    @Override
    public void execute() {

        String msg;
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;

        if ( this.limit != null ) 
        {
            int limitValue = this.limit.intValue();
            msg            = limitValue + "," + Client.COMMANDS[2];
        }
        else {
            msg    = Client.COMMANDS[2];
        }

        Packet packet = new Packet(msg, type);
        this.sender.send(packet);
    }

}
