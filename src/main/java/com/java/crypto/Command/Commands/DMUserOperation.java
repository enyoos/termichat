package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Commands.helpers.*;
import com.java.crypto.Command.Action            ;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Sender            ;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

// how this works
// input : /dm <- ( to ignore ) -u ( --user ) <name> -m ( --message ) <msg>
// we need to correctly parse the following
// dm is used to privately message someone
public class DMUserOperation implements Action, Parseable{

    // we store the specified args here.
    private String targetUser = ""  ;
    private String content    = ""  ;
    private Parser parser     = null;
    private Sender sender;

    public DMUserOperation(){}
    public DMUserOperation( Sender sender, String input )
    { 
        this.sender = sender;
        this.parse(input);
    }

    // here we'll figure out what is the user
    // and what's the msg content;
    private static final String FLAG_USER_SPECIFICATION    = "-u";
    private static final String FLAG_MESSAGE_SPECIFICATION = "-m";

    @Override
    public void parse (String input )
    {
        this.parser = new Parser ( new Lexer ( input ) );
        parser.parse();
    }

    @Override
    public boolean eval(){
        ArrayList<String> name_s = parser.struct.get ( FLAG_USER_SPECIFICATION )   ;
        ArrayList<String> msg_s  = parser.struct.get ( FLAG_MESSAGE_SPECIFICATION );

        boolean is_name = validate_name( name_s );
        boolean is_msg  = validate_msg( msg_s   );

        if (is_name)
        {
            this.targetUser = name_s.get ( 0 );
        }
        else
        {
            System.out.println(
                "[ERROR] The target user is not specified or supplied more than one username. Type /help -c dm, for more information" 
            );
            return false;
        }


        if (is_msg)
        {
            this.content = msg_s.get ( 0 );
        }
        else 
        {
            System.out.println ( 
                "[ERROR] The message to be sent is not specified. Type /help -c dm, for more information" 
            );
            return false;
        }

        return true;
    }
    
    private boolean validate_name ( ArrayList<String> name_s ) { return name_s != null && name_s.size() == 1;     }
    private boolean validate_msg  ( ArrayList<String> msg_s  ) { return msg_s  != null;/*&& msg_s.size()  == 1;*/ } 

    // for DEBUGGING PURPOSES ONLY
    public String getUserTarget()
    { return this.targetUser;  }
    public String getContent   ()
    { return this.content;     }
    public String output       ()
    { return this.targetUser + "," + this.content; }

    @Override
    public String toString()
    { return String.format ( "msg : " + this.content + " for : " + this.targetUser ); }

    @Override
    public void execute() {
        boolean is_eval = this.eval();

        if ( is_eval ){
            String msg = this.targetUser + "," + this.content;
            PACKET_TYPE type = PACKET_TYPE.PRIVATE;
            Packet packet = new Packet(msg, type);
            this.sender.send(packet);
        }
    }

}
