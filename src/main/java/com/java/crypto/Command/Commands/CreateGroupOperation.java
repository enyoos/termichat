package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Sender            ;
import com.java.crypto.Command.Action            ;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Commands.helpers.*;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet     ;

import static com.java.crypto.Encryption.Utils.* ;

public class CreateGroupOperation implements Action, Parseable
{

    private static final String FLAG_GC_NAME = "-n";

    private String gcName       ;
    private Sender sender       ;
    private Parser parser = null;

    public CreateGroupOperation () {}
    public CreateGroupOperation (Sender sender, String input ){
        this.sender = sender;
        this.parse ( input );
    }

    @Override
    public void parse(String input)
    {
        this.parser = new Parser ( new Lexer ( input ) );    
        parser.parse();
    }

    @Override
    public boolean eval(){
        ArrayList<String> name_s = parser.struct.get( FLAG_GC_NAME );

        boolean is_name = validate_name ( name_s );

        if ( is_name ) {

            String uncheck_name = name_s.get( 0 );

            // check if the name follows the nomenclature rules
            boolean is_correct = correctNameNomenclature( uncheck_name );
            if ( is_correct ) 
            {
                this.gcName = uncheck_name;
                return true;
            }

            System.out.println( "[ERROR] incorrect name. Your name must be in ascii letters only, " + 
            "without special characters and containing at least 4 characters." ); 
            return false;

        }

        System.out.println(
            "[ERROR] Name of the gc not specified or multiple names introduced... Type /help -c create"
        );
        return false;

    }

    private boolean validate_name ( ArrayList<String> name_s )
    { return name_s != null && name_s.size() == 1; }

    @Override
    public void execute ( )
    {
        boolean is_eval = this.eval(); 

        if ( is_eval )
        {
            String msg       = this.gcName             ;
            PACKET_TYPE type = PACKET_TYPE.CREATE      ;
            Packet      send = new Packet ( msg, type );
            this.sender.send ( send )                  ;

            return;
        }

        return;
        
    }

}