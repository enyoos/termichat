package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;
import static com.java.crypto.Encryption.Utils.*;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ClientHandler implements Runnable{

    // the list of commands
    private static final String[] COMMANDS = { 
        "list", "server_info", "ping", "help"
    };
    
    // update it at each iter ?
    // at each msg ?
    private static IvParameterSpec iv = new IvParameterSpec ( new byte[] {
	    -30, 103, -50, -92, -70, 51, -94, 94, 90, 119, 116, -113, -116, 120, 23, -36
    });
    private static ArrayList<Entity> clients = new ArrayList<>();
    private static final int MAX_SIZE = 1024;
    private static String serverInstanceName;
    private int msgLength = MAX_SIZE;

    private Entity client;


    // receiving 
    private InputStream  is;
    // writing 
    private OutputStream os;

    public ClientHandler( Socket socket ) {

        client = new Entity(socket);
        clients.add(client);

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            
            // also at the begining of the connection the client will send us his keys
            System.out.println("receiving the key.");
            recvKey();

            // at the begining of the connection the client will send us his username
            // but b4 let's receive the msgLength ( i.e the length of the byte array )
            System.out.println("receiving the name ...");
            recvName();
            
        }catch( IOException e ){ System.out.println( "Couldn't handle the client request."); }
    }

    public static void setServerInstanceName ( String name ) { ClientHandler.serverInstanceName = name;}
    private void recvKey () { receivePacket(); }
    public void recvName() { receivePacket(); }

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
                    handlePrivateMessaging(packet);
                    break;

                case RESPONSE:
                    handleQueryClient( packet );
                    break;

                case KEY:
                    handleKeyExchange( packet );
                    break;

                default:
                    break;
            }
        }catch( IOException e ){
            // if we're here, meaning that the user disconnected
            handleDisconnectEvent(packet);
            // handleDisconnectEvent(packet);
        }
    }


    private void handleKeyExchange( Packet packet )
    { broadcast ( packet ); }
    
    // special request from the client
    // i.e some api fecth ( like )
    private void handleQueryClient( Packet packet )
    {
        // gettigns the Command
        String command = packet.getMsg();
        
        // handling the list command
        if( command.equals(COMMANDS[0]))
        { sendListOfUsersIncludingSelf( ); }

        // handlign the server_info command
        else if ( command.equals(COMMANDS[1]))
        { sendServerInstanceInfo();}

        // handling the ping command
        else if ( command.equals(COMMANDS[2]))
        { sendPongMessageToClient(); }

        // do nothing, return nothing.
        else { return; }

    }

    private void sendPongMessageToClient()
    {
        Packet packet = new Packet();

        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
        String resp = "Pong, server!";

        packet.setMsg(resp);
        packet.setType(type);
        
        try{
            os.write(packet.output());
            os.flush();
        }
        catch (IOException e ) { System.out.println("[ERROR] couldn't send the result of the command with value : " + COMMANDS[1]);}
    }

    private void sendServerInstanceInfo()
    {
        Packet packet = new Packet();
        
        // what to send and what not to send. ( as server info )
        String info = String.format("Server name : %s", ClientHandler.serverInstanceName);
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;

        packet.setMsg(info);
        packet.setType(type);

        try{
            os.write(packet.output());
            os.flush();
        }
        catch (IOException e ) { System.out.println("[ERROR] couldn't send the result of the command with value : " + COMMANDS[2]);}
    }


    private static final String DELIMITER = "------------------";
    private void sendListOfUsersIncludingSelf( )
    {
        Packet packet = new Packet();
        StringBuilder sb = new StringBuilder();

        sb.append("\n" + DELIMITER);
        for ( Entity client : clients )
        {
            if ( client == this.client )
            { sb.append(String.format ( "- %s ( you )\n", client.getName())); }
            else sb.append(String.format("- %s\n", client.getName()));
        }
        sb.append(DELIMITER);

        packet.setMsg(sb.toString());
        packet.setType(PACKET_TYPE.RESPONSE);

        try{
            this.os.write(packet.output());
            this.os.flush();
        }catch (IOException e ) {System.out.println( "[ERROR] couldn't send the result of the command with value : " + COMMANDS[0]);}
    }

    private void removeUserByName ( Entity client )
    {
        // getting the place of the name in the usernames array;
        int idx = clients.indexOf(client);

        if ( idx > 0 )
        {
            // removing in both array ( sockets, ...)
            clients.remove(idx);
            clients.remove(idx);
        }
        else { return ; }
    }


    private void handleDisconnectEvent( Packet packet )
    {
        // remove the user from the 
        String msg = "[BROADCAST] The user " + client.getName() + " has something better to do !";
        PACKET_TYPE type = PACKET_TYPE.DISCONNECT;

        packet.setType(type);
        packet.setMsg(msg);

        removeUserByName(client);
        broadcast(packet);
    }

    private void handlePrivateMessaging(Packet packet )
    {

        System.out.println("[LOGGING] handling a private msg.");
        String[] args     = splitAtFirstOccurenceOf(",", packet.getMsg());

        String targetUser = args[0];
        String content    = args[1];

        if ( ! isUserExist( targetUser) ) 
        {

            // send to the user the msg with the errorr
            String err = "[ERROR] client with name " + targetUser + " doesn't exist";
            PACKET_TYPE type = PACKET_TYPE.RESPONSE;

            packet.setMsg(err);
            packet.setType(type);

            try{
                os.write(packet.output());
                os.flush();
            }catch( IOException e ) { System.out.println("[ERROR] tunnel the error msg, on private messaging." );}

            return;
        }


        sendPrivateMsgTo(targetUser, content);
    }

    private void sendPrivateMsgTo( String name, String content )
    {
        String msg;
        PACKET_TYPE type;

        Packet packet;

        for ( Entity client : clients )
        {
            if ( client.getName().equals(name) )
            {
                msg = String.format( "%s -> You >%s", name, content);
                type= PACKET_TYPE.RESPONSE;

                packet = new Packet(msg, type);

                try{
                    client.getSocket().getOutputStream().write(packet.output());
                    client.getSocket().getOutputStream().flush();
                }
                catch( IOException e ){ System.out.println("ERROR, couldn't send the private message.");}

                break;
            }
        }
    }

    private boolean isUserExist ( String name )
    {
        // first check if the targetUser is present in the group chat
        for ( Entity client : clients )
        { if ( client.getName().equals(name) ) { return true; } }
        return false;
    }

    private void handleSendEvent( Packet packet )
    {
        // chatLog i.e [username]>...bla...bla...bla...
        String chatLog = String.format("%s >" + packet.getMsg(), client.getName());
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
        client.setName(packet.getMsg());
        
        System.out.println("[LOGGING] received ( name ) with ( value ) " + client.getName());
        String greetingAnnoucement = String.format ("%s joined the chat!", client.getName());

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
        OutputStream os ;

        // iterating through the sockets array
        for ( Entity client : clients )
        {
            if ( client != this.client )
            {
                try {
                    bytes = packet.output();
                    os = client.getSocket().getOutputStream();

                    // send the actual packet
                    os.write(bytes);
                    os.flush();
                }
                catch( IOException e ) {
                    System.out.println("[ERROR], couldn't broadcast the message.");
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
            receivePacket();
        }
    }
    
}
