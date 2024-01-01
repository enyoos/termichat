package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class Client {
    
    private static String DEFAULT_HOST = "localhost";
    private static final Scanner scanner = new Scanner(System.in);
    private Socket socket;
    private static String name;

    // channel where you can read from
    private InputStream is;
    // chanel where you can write from
    private OutputStream os;


    public Client(){}
    public Client ( String name, int port )
    {
        try {
            this.name=name;
            this.socket = new Socket(DEFAULT_HOST, port);
            this.socket.setTcpNoDelay(true);

            is = socket.getInputStream();
            os = socket.getOutputStream();

            mainLoop();
        }catch( IOException e ) { e.printStackTrace(); }
    }


    // this the main loop
    // think of it like the main Game Loop
    public void mainLoop()
    {

        // firstly, we notify the server of our name,
        // using the CONNECT packet
        Packet firstDefaultPacket = new Packet(name, PACKET_TYPE.CONNECT);
        sendPacket(firstDefaultPacket);

        inputTask();

        boolean running = true;
        while( running )
        {
            receivePacket();
        }
    }

    // asks, while the program is running the input of the user
    public void inputTask ( )
    {
        new Thread( new Runnable() {
            private String name = Client.name ;

            @Override

            public void run()
            {
                boolean running = true;
                String msg      = this.name + " >";
                String input    = "";
                Packet packet   ;

                while ( running )
                {
                    System.out.print(msg); 
                    input = scanner.nextLine();

                    // by default, we broadcast each msg
                    packet = new Packet(input, PACKET_TYPE.SEND);
                    sendPacket ( packet );
                }
            }
        }).run();
    }

    // but is this a blocking line ?, well yeah...
    // we should maybe have a thread for writing and another thread for inputing
    private void receivePacket( )
    {
        Packet packet;
        byte[] byteMsg;

        try {
            byteMsg = is.readAllBytes();
            packet  = new Packet( byteMsg );
            System.out.println( packet.getMsg() );
        }catch ( IOException e ){ e.printStackTrace(); }
    }

    // the client can : SEND_PACKETS ( MSG to the server );
    private void sendPacket ( Packet packet )
    {
        try {
            // the flush occurs when a new line operator is entered
            // we simply call the flush method;
            // wiich propably less efficient
            // TODO : change.
            os.write(packet.output());
            // os.flush();
            System.out.println( "[LOGGING] sent the packet with msg : " + packet.getMsg());
        }
        catch ( IOException e )
        { e.printStackTrace(); }
    }
}
