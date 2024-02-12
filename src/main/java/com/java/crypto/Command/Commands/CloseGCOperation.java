package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Client                    ;
import com.java.crypto.Command.Action            ;
import com.java.crypto.Command.Sender            ;
import com.java.crypto.Client                    ;
import com.java.crypto.Packet.PACKET_TYPE        ;
import com.java.crypto.Packet.Packet             ;
import com.java.crypto.Command.Commands.helpers.*;
import com.java.crypto.Command.Commands.Parseable;

import static com.java.crypto.Encryption.Utils.*;

public class CloseGCOperation implements Action, Parseable{

    private static final String FLAG_COMMAND_PARTICULAR_ = "-n";

    private Sender sender          ;
    private ArrayList<String> gcs  ;
    private Parser parser          ;

    public CloseGCOperation(){}
    public CloseGCOperation( Sender sender, String input )
    {
        this.sender = sender;
        this.parse(input);
    }

    @Override
    public void parse(String input)
    {
        this.parser = new Parser ( new Lexer ( input ) );
        this.parser.parse();
    }

    @Override
    public boolean eval()
    {
        ArrayList<String> gc_s = this.parser.struct.get ( FLAG_COMMAND_PARTICULAR_ ); 
        boolean is_gc_s        = validate_gc_s( gc_s );

        if ( is_gc_s )
        { 
            this.gcs = gc_s; 
            return true;
        }
        else
        {
            this.gcs = new ArrayList<>();
            return false;
        }
    }

    private void ignore(){}

    private boolean validate_gc_s ( ArrayList<String> gc_s )
    { return gc_s != null && gc_s.size() > 0; }

    @Override
    public void execute() {

        boolean is_eval = this.eval();

        if ( is_eval )
        {
            PACKET_TYPE type = PACKET_TYPE.CLOSE   ;
            String str_gc_s  = this.gcs.toString();

            str_gc_s.substring( 1, str_gc_s.length() - 1 ).replaceAll( " ", "" ); 

            String      msg  = str_gc_s;

            sender.send( new Packet ( str_gc_s, type ) );
        } 

        else System.out.println( "No chat room specified. Hint : Use /help -c close" );
    }
    
}