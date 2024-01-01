package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ClientHandler implements Runnable{

    private static ArrayList<Socket> sockets = new ArrayList<>();
    private Socket socket;

    // the username provided by the client
    private String name  ;

    // receiving 
    private InputStream  is;

    // writing 
    private OutputStream os;

    public ClientHandler( Socket socket ) {

        System.out.println( "instantiating the client handler" );

        this.socket=socket; 
        sockets.add(socket); 

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            
            // at the begining of the connection the client will send us his username
            receivePacket();

        }catch( IOException e ){ e.printStackTrace(); }
    }

    public void receivePacket ()
    {
        try {

            // firstly we send the length of the byte array representation TODO !

            // "packet reconstruciton"
            Packet packet = new Packet(bytesNative);
            
            // according to its type
            // we take specific actions
            switch (packet.getType()) {

                case DISCONNECT:
                    handleDisconnectEvent(packet);
                    break;

                case CONNECT:
                    handleConnectEvent(packet); 
                    break;
            
                case SEND:
                    handleSendEvent(packet); 
                    break;

                case PRIVATE:
                    // if you go and send what the PRIVATE enumeration means
                    // this function is another way to say handleDM.   
                    handlePrivateEvent(packet);
                    break;
 
                default:
                    break;
            }
        }catch( IOException e ){ e.printStackTrace(); }
    }
    
    private void handleDisconnectEvent( Packet packet )
    {
        System.out.println("TODO, NOT IMPLEMENTED");
    }

    private void handlePrivateEvent(Packet packet )
    {
        System.out.println("TODO, NOT IMPLEMENTED");
    }

    private void handleSendEvent( Packet packet )
    {
        // chatLog i.e [username]>...bla...bla...bla...
        String chatLog = String.format("%s >" + packet.getMsg());
        PACKET_TYPE packetType = PACKET_TYPE.RESPONSE; 

        packet.setMsg(chatLog);
        packet.setType(packetType);

        broadcast(packet);
    }

    // handling the connection.
    // i.e broadcasting to every client that the current user joined the gc
    private void handleConnectEvent ( Packet packet )
    {
        // the connect packet will contain the name of curr client
        name = packet.getMsg();
        String greetingAnnoucement = String.format ("[%s] just joined the chat !", name);
        PACKET_TYPE packetType     = PACKET_TYPE.RESPONSE;

        packet.setMsg(greetingAnnoucement);
        packet.setType(packetType);

        broadcast(packet);
    }

    // sending to all the clients the msg of the current client;
    private void broadcast( Packet packet )
    {
        // iterating through the sockets array
        for ( Socket socket : sockets )
        {
            if ( socket != this.socket )
            {
                try {
                    socket.getOutputStream().write(packet.output());
                }
                catch( IOException e ) {e.printStackTrace();}
            }
        }
    }
    
    // like the client the client handler also has the mainLoop ( which is the run loop )
    // likewise think of it like the main game loop.
    @Override
    public void run() {
        System.out.println( "here!!!" );
        boolean listening = true;
        while ( listening ) { 
            receivePacket();
        }
    }
    
}
