package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ClientHandler implements Runnable{

    // the list of commands
    private static final String[] COMMANDS = { 
        "LIST", "SHOW", "PING"
    };

    // THE USER ( SERVER-SIDE ) ENTITY
    private static ArrayList<Socket> sockets = new ArrayList<>();
    private static ArrayList<String> usernames = new ArrayList<>();

    private int msgLength;
    private Socket socket;

    // the username provided by the client
    private String name  ;

    // receiving 
    private InputStream  is;

    // writing 
    private OutputStream os;

    public ClientHandler( Socket socket ) {

        this.socket=socket; 
        sockets.add(socket); 

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            
            // at the begining of the connection the client will send us his username
            // but b4 let's receive the msgLength ( i.e the length of the byte array )
            receiveLengthMsg();
            receivePacket();
            
        }catch( IOException e ){ e.printStackTrace(); }
    }

    private void receiveLengthMsg ( )
    {
        try{
            this.msgLength = is.read();
        }
        catch( IOException e ) {
            // also here, this means the user disconnected
            Packet packet = new Packet();
            handleDisconnectEvent(packet);
        }
    }

    public void receivePacket ()
    {
        Packet packet = new Packet();
        try {

            // we check and read the first byte ( which is the length of the byte )
            byte[] allocateBytesArray = new byte[this.msgLength];
            is.read(allocateBytesArray);

            packet = new Packet(allocateBytesArray);
            
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

                case RESPONSE:
                    handleQueryClient( packet );
                    break;
                default:
                    break;
            }
        }catch( IOException e ){
            // if we're here, meaning that the user disconnected
            handleDisconnectEvent(packet);
        }
    }
    
    // special request from the client
    // i.e some api fecth ( like )
    private void handleQueryClient( Packet packet )
    {
        System.out.println("TODO, NOT IMPLEMENTED");
    }

    private void removeUserByName ( String name )
    {
        // getting the place of the name in the usernames array;
        int idx = usernames.indexOf(name);

        // removing in both array ( sockets, ...)
        usernames.remove(idx);
        sockets.remove(idx);
    }


    private void handleDisconnectEvent( Packet packet )
    {
        // remove the user from the 
        String msg = "The user " + this.name + " has something better to do !";
        PACKET_TYPE type = PACKET_TYPE.DISCONNECT;

        packet.setType(type);
        packet.setMsg(msg);

        removeUserByName(name);
        broadcast(packet);
    }

    private void handlePrivateEvent(Packet packet )
    {
        System.out.println("TODO, NOT IMPLEMENTED");
    }

    private void handleSendEvent( Packet packet )
    {
        // chatLog i.e [username]>...bla...bla...bla...
        String chatLog = String.format("%s >" + packet.getMsg(), name);
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
        usernames.add(name);

        String msg = String.format ("[%s] just joined the chat !", name);
        String greetingAnnoucement = msg;

        PACKET_TYPE packetType     = PACKET_TYPE.RESPONSE;

        packet.setMsg(greetingAnnoucement);
        packet.setType(packetType);

        // in reality you should only send the packet info ( i.e the msg )
        broadcast(packet);
    }

    // sending to all the clients the msg of the current client;
    private void broadcast( Packet packet )
    {
        byte[] bytes;
        int  length;
        OutputStream os ;

        // iterating through the sockets array
        for ( Socket socket : sockets )
        {
            if ( socket != this.socket )
            {
                try {
                    bytes = packet.output();
                    length = bytes.length;

                    os = socket.getOutputStream();

                    // send the lenght of the packet first
                    os.write( length );
                    os.flush();

                    // send the actual packet
                    os.write(bytes);
                    os.flush();

                }
                catch( IOException e ) {
                    // this means that the user content msg couldn't ( for some reason )
                    // to the other clients
                    try {
                        String msg = "[SERVER] couldn't send your message to the other users";
                        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
                        Packet _packet    = new Packet(msg, type);
                        this.os.write( _packet.output() );
                    }
                    catch ( IOException _e )
                    {
                        // this means that the server lost the connection with the user
                        // we'll interpret it like a disconnection
                       handleDisconnectEvent(packet);
                    }
                }
            }
        }
    }
    
    // like the client the client handler also has the mainLoop ( which is the run loop )
    // likewise think of it like the main game loop.
    @Override
    public void run() {
        boolean listening = true;
        while ( listening ) { 
            // if those functions are failing
            // it will cause an infinite loop of errors
            // however, the server will still be running
            // but, it's better to actually handle those
            // as if the user disconnected

            receiveLengthMsg();
            receivePacket();
        }
    }
    
}
