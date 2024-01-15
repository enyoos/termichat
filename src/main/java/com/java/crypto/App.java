package com.java.crypto;

import java.util.Scanner;
import static com.java.crypto.Encryption.Utils.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final Scanner scanner = new Scanner(System.in);

    public static void main( String[] args )
    {

        // instantiate the server
        // since we've the ability to execute the server alone
        // we don't need these lines of code
        // Thread serverThread = new Thread( new Runnable() {
        //     @Override
        //     public void run ()
        //     {
        //         int PORT = 5055;
        //         String serverName = "server 1";
        //         Server server = new Server(serverName, PORT);
        //     }  
        // });

        // serverThread.start();

        // now back to the main thread
        // we will run the client
        int PORT = 5055;
	

        Client client = new Client(promptUsername(), PORT);
    }

    public static String promptUsername ( )
    {

	boolean writing   = true;
	String clientName = "";
	String prompt     = "What's your username ? : ";

	while ( writing ){

		System.out.print( prompt );
		clientName = scanner.nextLine();

		if ( correctNameNomenclature ( clientName ) ) break;
		else { System.out.println( "[ERROR] incorrect name. Your name must be in ascii letters only, " + 
				"without special characters and containing atleast 4 characters." ); continue; }
	}

	return clientName;
    }
}
