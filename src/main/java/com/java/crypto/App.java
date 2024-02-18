package com.java.crypto;

import java.util.Scanner;
import static com.java.crypto.Draw.Draw.*;
import java.util.NoSuchElementException;

import com.happycli.java.*;

import static com.java.crypto.Encryption.Utils.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final Scanner scanner = new Scanner(System.in);
    public static final int PORT = 5055;

    public static void main( String[] args )
    {
        // display intro ( name of the app, like in hacker movies )
        String intro = intro();
        System.out.println( intro );
        // 

        Client client = new Client(promptUsername(), PORT);
    }

    public static String promptUsername ( )
    {

        boolean writing   = true;
        String clientName = "";
        String prompt     = "What's your username ? : ";

        while ( writing ){

            System.out.print( prompt );

            try{
                clientName = scanner.nextLine();
            }
            catch ( NoSuchElementException e ){
                System.out.println( "\nExiting main context. Thanks for using TERMICHAT" );
                System.exit ( 0 );
            }

            if ( correctNameNomenclature ( clientName ) ) break;
            else {
                System.out.println( 
                        new TextureBuilder().content( 
                            "[ERROR] incorrect name. Your name must be in ascii letters only, " + 
                            "without special characters and containing atleast 4 characters. [FOSSIUM is reserved]" 
                        ).underline ( true ).foreground ( PaintOptions.RED ).build()
                );

                continue; 
            }
        }

        return clientName;
    }
}
