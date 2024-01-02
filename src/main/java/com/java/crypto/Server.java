package com.java.crypto;

import java.io.IOException;
import java.net.ServerSocket;
import java.lang.Thread;
import java.net.Socket;

public class Server {

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
            ss = new ServerSocket( port );
            this.name = name;

            String msg = String.format( "[%d, %s] listening for incoming connection", port, name);
            System.out.println( msg );
            this.listen();

        }catch( IOException e )
        {
            e.printStackTrace();
        }
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
                Thread uniqueClientThread = new Thread( new ClientHandler(socket) );
                uniqueClientThread.start();
            }
                
        }catch( IOException e )
        {
            e.printStackTrace();
        }
    }

}
