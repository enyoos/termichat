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

public class ShowAllCommandsOperation implements Action, Parseable{

    private static final String DELIMITER                = "------------------";
    private static final String FLAG_COMMAND_PARTICULAR_ = "-c";

    private static final String[] COMMANDS_DESCRIPTIONS  = {
        "ping            pings the server and returns pong!",
        "sinfo           returns the name of the server    ",
        "list            returns list of all the users in the group chat.                  (-l/limit of the users to show                                                    )",
        "exit            exits the group chat instance",
        "dm              sends a private msg to some user                                  (-m/specify message, -u/specify user                                              )",
        "help            shows all the commands, or details a particular command           (-c/name of unique cmd.s                                                            )",
        "info            shows all the information concerning the client ( you )",
        "create          creates special group                                             (-n/name of the group chat                                                        )",
        "listgc          shows all the groups currently active                             (-l/limit of the gc to show                                                       )",
        "join            joins the specified group chat ( gc )                             (-n/name of the group chat                                                        )",
        "ban             ban someone or a specific gc                                      (-n/name of the user, -t/duration (min) of the ban, -r/explicit reason of the ban )",
        "close           close a group chat                                                (-n/name of the group chat                                                        )",
        "!!              executes the last command                                                                                                                           " ,
        "notif          toggle notification sound                                         (-s/off or on                                                                      )",
    };

    private Sender sender          ;
    private ArrayList<String> cmd_s;
    private Parser parser          ;

    public ShowAllCommandsOperation(){}
    public ShowAllCommandsOperation( Sender sender, String input )
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
        ArrayList<String> command_s = this.parser.struct.get ( FLAG_COMMAND_PARTICULAR_ ); 
        boolean is_commands         = validate_commands( command_s );

        if ( is_commands )
        { 
            this.cmd_s = command_s; 
            return true;
        }
        else
        {
            this.cmd_s = new ArrayList<>();
            return false;
        }
    }

    private void ignore(){}

    // returns -1 if the element is not present
    // returns <value> the position of the element
    private int retrieve_idx_command ( String some_cmd )
    {
        int length = Client.COMMANDS.length;
        String cmd = "";

        for ( int i = 0; i < length; i ++ )
        {
            cmd = Client.COMMANDS[i];
            if ( cmd.equals ( some_cmd ) ) return i;
            else continue;
        }

        return -1;
    }

    private boolean validate_commands ( ArrayList<String> command_s )
    { return command_s != null && command_s.size() > 0; }

    @Override
    public void execute() {

        boolean is_eval = this.eval();

        StringBuilder sb = new StringBuilder();
        sb.append(DELIMITER);
        sb.append("<command_name>            <usage>(...<flags>)\n");

        if ( is_eval )
        {
            // we check if all the commands are existing
            for ( String cmd : this.cmd_s )
            {
                int res = retrieve_idx_command( cmd );
                if ( res != -1 ) continue;
                else
                {
                    System.out.println( 
                        "[ERROR] The command : " + cmd + " doesn't exist. Maybe you meant : " + lev ( cmd, Client.COMMANDS) 
                    );

                    return;
                }
            }
            
            for ( String cmd : this.cmd_s )
            {
                int res = retrieve_idx_command( cmd );
                sb.append ( "\n" + COMMANDS_DESCRIPTIONS[res] );
            }

        }
        else 
        {
            sb.append(""   + COMMANDS_DESCRIPTIONS[0]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[1]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[2]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[3]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[4]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[5]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[6]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[7]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[8]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[9]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[10]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[11]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[12]);
            sb.append("\n" + COMMANDS_DESCRIPTIONS[13]);
        }


        sb.append( "\n" )   ;
        sb.append(DELIMITER);

        System.out.println(sb);

    }
    
}
