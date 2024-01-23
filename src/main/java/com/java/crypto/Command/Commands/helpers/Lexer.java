package com.java.crypto.Command.Commands.helpers;

import java.util.Optional;

public final class Lexer
{

    String str;
    public Lexer ( String c ) { this.str = c ;}
    public Lexer ( )          { this.str = "";}

    // returns the next token
    public String next()
    {
        str = str.trim();

        if ( str.length() == 0 ) return null;
        
        String first = str.substring ( 0, 1 );

        for ( int i = 1; i < str.length(); i ++ )
        {
            char ch = str.charAt ( i );

            if ( ch == ' ' )
            {
                str = str.substring ( i );
                return first;
            }
            else {
                first += str.charAt ( i );
                continue;
            }

        }

        int tokenLength = first.length();
        this.str        = str.substring( tokenLength );

        return first;

    }
}