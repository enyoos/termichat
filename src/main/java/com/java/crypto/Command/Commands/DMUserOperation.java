package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Commands.Parseable;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

// how this works
// input : /dm <- ( to ignore ) -u ( --user ) <name> -m ( --message ) <msg>
// we need to correctly parse the following
// dm is used to privately message someone
public class DMUserOperation implements Action, Parseable{

    // we store the specified args here.
    private String targetUser = "";
    private String content    = "";
    private Sender sender;

    public DMUserOperation(){}
    public DMUserOperation( Sender sender, String input )
    { 
        this.sender = sender;
        this.parse(input);
    }

    // here we'll figure out what is the user
    // and what's the msg content;
    private static final char COMMAND_PREFIX = '-';
    private static final char USER_SPECIFICATION = 'u';
    private static final char MESSAGE_SPECIFICATION = 'm';

    @Override
    public void parse (String input )
    {

        String username = "";
        String msg      = "";

        boolean isUser= false;
        boolean isMsg = false;
        boolean isCmd = false;

        // let's read each char
        for ( char ch : input.toCharArray())
        {
            // we stumble upon the '-'
            if ( ch == COMMAND_PREFIX )
            { isCmd = true; }
            else 
            {
                if ( isCmd )
                {
                    if ( ch == USER_SPECIFICATION )
                    {
                        isUser = true ;
                        isMsg  = false;
                        isCmd  = false;
                    }
                    else if ( ch == MESSAGE_SPECIFICATION )
                    {
                        isMsg  = true ;
                        isUser = false;
                        isCmd  = false;
                    }
                    else { continue; }
                }
                else
                {
                    if ( isUser )     { username += ch; }
                    else if ( isMsg ) { msg      += ch; } 
                    else              { continue;  }
                }
            }
        }

        this.targetUser = username.trim();
        this.content    = msg.trim()    ;

    }

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

        // first check if the user and the msg is defined
        if ( targetUser.isEmpty() )
        {
            System.out.println("ERROR, you need to specify the user target. Use /help.");
            return;
        }

        if ( this.content.isEmpty())
        {
            System.out.println("ERROR, you need to specify the content you want to send. Use /help.");
            return;
        }


        String msg = this.targetUser + "," + this.content;
        PACKET_TYPE type = PACKET_TYPE.PRIVATE;
        Packet packet = new Packet(msg, type);

        this.sender.send(packet);
    }

}
