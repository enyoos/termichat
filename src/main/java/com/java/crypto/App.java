package com.java.crypto;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Scanner scanner = new Scanner(System.in);

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
        String prompt = "What's your username ? : ";
        System.out.print( prompt );
        String clientName = scanner.nextLine();
        Client client = new Client(clientName, PORT);
    }
}
