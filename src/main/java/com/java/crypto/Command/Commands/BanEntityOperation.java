package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Sender;
import com.java.crypto.Command.Commands.helpers.*;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;


public class BanEntityOperation implements Action, Parseable
{
    // -n <name of the user> -t <duration of the ban, in minutes> -r <the reason of the ban>
    // all the flags are mandatory 

    // the user can specify multiple users
    private static final String FLAG_NAME   = "-n"; 

    // the user need to specify only one time and not more
    private static final String FLAG_TIME   = "-t";

    public BanEntityOperation( ){}
    public BanEntityOperation( Sender sender, String input ){
        this.sender = sender;
        this.parse ( input );
    }

    private static final String FLAG_REASON = "-r";

    private String targets    = null;
    private int time          = 0   ;
    private String reason     = null;
    private Parser parser     = null;

    private Sender sender;

    @Override
    public void parse (String input )
    {
        this.parser = new Parser ( new Lexer ( input ) );
        parser.parse();
    }

    // returns a false on err
    @Override
    public boolean eval ( )
    {
        // check if all the flags are specified
        ArrayList<String> names   = parser.struct.get ( FLAG_NAME )  ;
        ArrayList<String> times   = parser.struct.get ( FLAG_TIME )  ;
        // ArrayList<String> reasons = parser.struct.get ( FLAG_REASON );

        boolean is_names   = validate_name    ( names );
        boolean is_times   = validate_time    ( times );
        // boolean is_reasons = validate_reason( reasons );

        if ( is_names )  handle_names( names );
        else{
            System.out.println( "[ERROR] The name of the user is not specified." );
            return false;
        }

        if ( is_times )  handle_time( times.get( 0 ) );
        else             {
            System.out.println( "[ERROR] The duration of the ban is not specified." );
            return false;
        }

        // if ( is_reasons )handle_reason( reasons.get ( 0 ) );
        // else             {
        //     System.out.println( "[ERROR] The reason of the ban is not specified." );
        //     return false;
        // }

        return true;
    }

    private boolean validate_name   ( ArrayList<String> names  ) { return names != null && names.size() > 0 ; }
    private boolean validate_time   ( ArrayList<String> times  ) { return times != null && times.size() == 1; }

    private void handle_names (ArrayList<String> names)
    { 
        String repr  = names.toString();
        this.targets = repr.substring ( 1, repr.length()-1 );
    }

    private void handle_time  ( String time )
    {
        try 
        {
            // even tho converting to int might seem useless in this case
            // it is for checking if the integer would overflow
            int suspend_time = Integer.parseInt ( time );
            this.time        = suspend_time;
        }
        catch ( java.lang.ArithmeticException e ) {
            System.out.println( "[ERROR] Integer provided is invalid." );
        }
    }

    private void handle_reason( String reason) { this.reason = reason.toLowerCase(); } 

    @Override
    public void execute()
    {
        boolean evaluated = this.eval();


        if ( evaluated )
        {
            // <names...>|<time>|<reason>
            String msg       = String.format ( "%s|%d", this.targets, this.time ); 
            System.out.println( "exec : " + msg );
            PACKET_TYPE type = PACKET_TYPE.BAN;
            Packet packet    = new Packet ( msg, type );

            this.sender.send ( packet );
        }
    }



}