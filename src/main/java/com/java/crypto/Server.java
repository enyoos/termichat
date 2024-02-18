package com.java.crypto;

import java.io.IOException;
import java.lang.Thread;

import java.net.*;

import javax.crypto.spec.IvParameterSpec;

public class Server {

    private static final int BACK_LOG = 100;  // number of awaiting conn
    private static final String IP    = "192.168.859.672";
    private static final String DNS   = "envyoos.xyz"    ;

    private ServerSocket ss;
    private String name;

    public static void main( String... args )
    {

        String sName = "server1";
        int    PORT  = 5055;
        Server server = new Server( sName, PORT );
    }

    public Server( )
    { }

    public Server ( String name, int port )
    {

        try {

            InetAddress addr = InetAddress.getByName( DNS )      ;
            ss = new ServerSocket( port, 100, addr );
            this.name = name;

            String msg = String.format( "[%d, %s] listening for incoming connection", port, name);
            System.out.println( msg );

            this.listen();

        }catch( IOException e )
        {
            e.printStackTrace();
        }
        // catch ( UnknownHostException e )
        // {
        //     e.printStackTrace();
        // }
    }


    private void listen ( )
    {
        try {

            // we accept as many clients here.
            boolean running = true;
            while ( running )
            {
                // this is a blocking line
                Socket socket = ss.accept();
                System.out.println( String.format ("[%d] incoming connection !", socket.getPort()));
                
                // we handle each client in a seperate thread
                ClientHandler.setServerInstanceName(name);

                Thread uniqueClientThread = new Thread( new ClientHandler(socket) );
                uniqueClientThread.start();
            }
                
        }catch( IOException e )
        {
            e.printStackTrace();
        }
    }

}
