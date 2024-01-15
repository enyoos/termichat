package com.java.crypto.Command.Commands;

import com.java.crypto.Client;
import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Client;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;
import static com.java.crypto.Encryption.Utils.*;


public class ShowAllCommandsOperation implements Action{

    private static final String DELIMITER                = "------------------";
    private static final String COMMAND_PARTICULAR_FLAG  = "-c";
    private static final String COMMAND_PARTICULAR_FLAGG = "--command";
    private String commandInParticular                   = "";
    private static final String[] COMMANDS_DESCRIPTION   = {
        "ping            pings the server and returns pong!",
        "sever_info      returns the name of the server    ",
        "list            returns list of all the users in the group chat.",
        "exit            exits the group chat instance",
        "dm              sends a private msg to some user                           (-m, -u)",
        "help            shows all the commands, or details a particular command    (-c)"
    };

    private Sender sender;
    public ShowAllCommandsOperation(){}
    public ShowAllCommandsOperation( Sender sender, String input )
    {
        this.sender = sender;
        this.parseInput(input);
    }

    public void parseInput(String input)
    {
        String[] tokens               = input.split(" ");
        boolean isCommandInParticular = false;

        for ( String token : tokens )
        {
            if ( token.equals(COMMAND_PARTICULAR_FLAG) )
            { isCommandInParticular = true; }
            else
            {
                if ( isCommandInParticular ) { this.commandInParticular = token.trim(); 
                    isCommandInParticular = false; }
                else continue;
            }
        }
    }

    @Override
    public void execute() {

        StringBuilder sb = new StringBuilder();
        sb.append(DELIMITER);
        sb.append("<command_name>            <usage>(...<flags>)\n");

        System.out.println("the command : " + this.commandInParticular);

        if ( this.commandInParticular.isEmpty() )
        {
            // always add 12 space
            sb.append(COMMANDS_DESCRIPTION[0]);
            sb.append("\n" + COMMANDS_DESCRIPTION[1]);
            sb.append("\n" + COMMANDS_DESCRIPTION[2]);
            sb.append("\n" + COMMANDS_DESCRIPTION[3]);
            sb.append("\n" + COMMANDS_DESCRIPTION[4]);
            sb.append("\n" + COMMANDS_DESCRIPTION[5]);
        }
        else 
        {
            // check if that command in particular exist
            if (isCommandExist(this.commandInParticular) )
            {
                for ( int i = 0; i < Client.COMMANDS.length; i ++ )
                {
                    if ( this.commandInParticular.equals(Client.COMMANDS[i]) )
                    { sb.append( COMMANDS_DESCRIPTION[i] ); }
                }
            }
            else { 
                System.out.println("The command specified as an argument doesn't exist. Maybe you meant : " + lev ( this.commandInParticular, Client.COMMANDS ) );

		return; // exit the function ...
            }
       }


        sb.append(DELIMITER);

        System.out.println(sb);

        // you don't even need to send ( it is client side ) ...
        // PACKET_TYPE type = PACKET_TYPE.PRIVATE;
        // Packet packet = new Packet(sb.toString(), type);
        
        // this.sender.send(packet);
    }

    private boolean isCommandExist( String cmd )
    {
        for ( String cmd_ : Client.COMMANDS )
        {
            if ( cmd.equals(cmd_) ) { return true; }
        }
        
        return false;
    }
    
}
