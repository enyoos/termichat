package com.java.crypto.Command.Commands.helpers;

import java.util.*;

public class Parser
{

    private static final String OPS = "-";

    // flag : args
    HashMap<String, ArrayList<String>> struct;
    Lexer lexer;
    

    public Parser ( Lexer l ) { this.lexer = l; this.struct = new HashMap<>();}
    public Parser (  )        { this ( null ); }
    
    public static void main ( String... args )
    {
        System.out.println( "Hello, World" );
        Lexer l      = new Lexer ( input );
        Parser p     = new Parser ( l );

        p.parse();
        System.out.println( p.struct );
    }

    private void __parse ( String op )
    {
        String next = this.lexer.next();
        if ( next == null ) return;
        
        if ( next.contains ( OPS ) ) {

            struct.put ( next, new ArrayList<String>() );
            __parse ( next );

        }
        else {

            ArrayList<String> temp = struct.get ( op );
            temp.add ( next );
            struct.put ( op, temp );

            __parse ( op );
        }
    }

    public void parse() { __parse ( "" ); }

}