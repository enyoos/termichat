package com.java.crypto.Command.Commands;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Commands.helpers.*;

import com.java.crypto.Client;

import static com.java.crypto.Encryption.Utils.* ;
import java.util.ArrayList;

public class ToggleNotifyOperation implements Action, Parseable{

    private static final String FLAG= "-s" ;
    private static final String OFF = "off";
    private static final String ON  = "on" ;

    private String status;
    private Parser parser;
    private Sender sender;
    private Client client;

    public ToggleNotifyOperation( ) {}
    public ToggleNotifyOperation( Sender sender, String input, Client client ) {
        this.client = client;
        this.sender = sender;
        this.parse( input ) ;
    }
    
    @Override
    public void parse( String input )
    {
        this.parser = new Parser ( new Lexer ( input ) );
        this.parser.parse();
    }

    @Override
    public boolean eval()
    {
        ArrayList<String> status_ = this.parser.struct.get ( FLAG ); 
        boolean is_status_        = validate_status_( status_ );

        if ( is_status_ ) { this.status = status_.get( 0 ); return true; } 
        else              { return false; }
    }

    public boolean validate_status_ ( ArrayList<String> s_ )
    { return s_ != null && s_.size() == 1; }

    @Override
    public void execute() {

        boolean is_eval = this.eval();

        if ( is_eval )
        {
            if ( this.status.equals( OFF ) ){
                this.client.notifOn = false;
            }
            else if ( this.status.equals( ON ) ) {
                this.client.notifOn = true;
            }
            else {
                System.out.println( "You didn't supply the correct args. Help : /help -c notify" );
            }

            return;
        }

        System.out.println( "You didn't supply the appropriate flag or you supplied more than one arguments. Help : /help -c notify" );

    }
        
}