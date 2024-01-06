package com.java.crypto.Command.Commands;

import java.util.ArrayList;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

// how this works
// input : /dm <- ( to ignore ) -u ( --user ) <name> -m ( --message ) <msg>
// we need to correctly parse the following
// dm is used to privately message someone
public class DMUserOperation implements Action{

    private static final String USER_FLAG  = "-u";
    private static final String USER_FLAGG = "--user";
    private static final String MESSAGE_FLAG = "-m";
    private static final String MESSAGE_FLAGG = "--message";

    // we store the specified args here.
    private String targetUser = "";
    private String content    = "";
    private Sender sender;

    public DMUserOperation(){}
    public DMUserOperation( Sender sender, String input )
    { 
        this.sender = sender;
        this.parseInput(input);
    }

    // here we'll figure out what is the user
    // and what's the msg content;
    private void parseInput (String input )
    {
        String[] tokens =  input.split(" ");
        boolean  isUserFlag = false;
        boolean  isMsgFlag  = false;

        // first let's see if the client ( from ) specified the user ( to )
        for ( String token : tokens )
        {
            // means the next token shall be the name
            // of recipient ( or the target )
            if ( token.equals(USER_FLAG) | token.equals(USER_FLAGG) )
            { isUserFlag = true; }

            // means the next token shall be the content of
            // the msg
            else if ( token.equals(MESSAGE_FLAG) | token.equals(MESSAGE_FLAGG) )
            { isMsgFlag = true; }

            // means we encouter a argument, 
            else {
                if ( isMsgFlag ) {this.content = token.trim(); isMsgFlag = false;}
                else if ( isUserFlag ) {this.targetUser = token.trim(); isUserFlag = false;}
                else continue;
            }
        }
    }

    @Override
    public void execute() {

        // first check if the user and the msg is defined
        if ( targetUser.isEmpty() )
        {
            System.out.println("ERROR, need to specify the user target. Use /help.");
            return;
        }

        if ( this.content.isEmpty())
        {
            System.out.println("ERROR, you need to say something, we're waiting...");
            return;
        }

        String msg = this.targetUser + "," + this.content;
        PACKET_TYPE type = PACKET_TYPE.PRIVATE;
        Packet packet = new Packet(msg, type);

        this.sender.send(packet);
    }

}
