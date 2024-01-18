package com.java.crypto;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List     ;
import java.util.Arrays   ;

import javax.crypto.spec.IvParameterSpec;

import static com.java.crypto.Encryption.Utils.*;
import com.java.crypto.Packet.PACKET_TYPE       ;
import com.java.crypto.Packet.Packet            ;

import java.net.Socket;

// remenber this is the server 
// ( it's going to apply for every client )
public class ClientHandler implements Runnable{

    // the list of commands
    private static final String[] COMMANDS = { 
        "list", "server_info", "ping", "help"
    };

    // specific to each client.
    // instead how about we store them on the client side ?
    // first check if there are some clients and then report ?

    // update it at each iter ?
    // at each msg ?
    private static IvParameterSpec iv = new IvParameterSpec ( new byte[] {
	    -30, 103, -50, -92, -70, 51, -94, 94, 90, 119, 116, -113, -116, 120, 23, -36
    });

    private static ArrayList<Entity> clients = new ArrayList<>();
    private static final int MAX_SIZE = 4096;
    private int msgLength             = MAX_SIZE;
    private static String serverInstanceName;

    private Entity client;


    // receiving 
    private InputStream  is;
    // writing 
    private OutputStream os;

    public ClientHandler( Socket socket ) {

        System.out.println( "[LOGGING] new clientHandler..." );
        client = new Entity(socket);

        // each time we add some clients to the client array
        // we send all the missed packet
        clients.add(client);

        // what's the plan ?
        // make the clients array an observable
        // such that on change we send a missing packet.


        try {

            is = socket.getInputStream();
            os = socket.getOutputStream();
           
        }catch( IOException e ){ System.out.println( "Couldn't handle the client request."); }
    }



    public static void setServerInstanceName ( String name ) { ClientHandler.serverInstanceName = name;}


    public void emitCacheSignal ( Packet packet )
    {
	    System.out.println( "[LOGGING] emitting cache signal with value : " + packet );
	    _emitCacheSignal ();
    }

    public void _emitCacheSignal ()
    {
	    // returns ( intact the packet sent ) back to the client;


            PACKET_TYPE type = PACKET_TYPE.CACHE; 
	    String      msg  = "CACHE SIGNAL";
	    Packet packet    = new Packet ( msg, type );

	    sendPacket ( packet );
    }

    public void receivePacket ()
    {

        Packet packet = new Packet();

        try {

            // we check and read the first byte ( which is the length of the byte )
            byte[] allocateBytesArray = new byte[this.msgLength];
            is.read(allocateBytesArray);

            packet = new Packet(allocateBytesArray);
	    System.out.println( "[LOGGING] INCOMING PACKET : " + packet );

	    PACKET_TYPE type = packet.getType ();

            // according to its type
            // we take specific actions
            switch (type) {

                case DISCONNECT:
                    handleDisconnectEvent(packet);
                    break;

                case CONNECT:
                    handleConnectEvent(packet); 
                    break;
            
                case BROADCAST:
                    handleBroadcastEvent(packet); 
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
		    handleUnknownPacket ( packet );
                    break;
            }
        }catch( IOException e ){
            // if we're here, meaning that the user disconnected
            handleDisconnectEvent(packet);
            // handleDisconnectEvent(packet);
        }
    }

    private void sendErrPacket2Client ( Packet packet )
    {
	    try{
		    os.write( packet.output () );
		    os.flush()                  ;
	    } catch ( IOException e ) { closeCurrentCommunicationWithClient( ); }
    }

	private void handleUnknownPacket ( Packet packet ) { System.out.println( "[LOGGING] packet unknown info : " + packet ); }
    private void handleKeyExchange( Packet packet ) { broadcast ( packet ); }
    
    // special request from the client
    // i.e some api fecth ( like )
    private void handleQueryClient( Packet packet )
    {

	System.out.println( "[LOGGING] handling query client" );
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
	System.out.println( "[LOGGING] executing the /list command" );
        Packet packet = new Packet();
        StringBuilder sb = new StringBuilder();

        sb.append("\n" + DELIMITER);
        for ( Entity client_ : clients )
        {
            if ( client_ == this.client )
            { sb.append(String.format ( "- %s ( you )\n", this.client.getName())); }
            else sb.append(String.format("- %s\n", client_.getName()));
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

        if ( idx >= 0 ) { clients.remove(idx); }
        else { return ; }
    }


    private void handleDisconnectEvent( Packet packet )
    {

        // remove the user from the 
        String msg = "[SERVER] The user " + client.getName() + " has something better to do !";
        PACKET_TYPE type = PACKET_TYPE.DISCONNECT;

        packet.setType(type);
        packet.setMsg(msg);

        removeUserByName(client);
        broadcast(packet);

	closeCurrentCommunicationWithClient ();
    }

    private void closeCurrentCommunicationWithClient ( )
    {
	    try{
		    System.out.println( "[LOGGING] aborting link with " + this.client.getName() );
		    os.close ();
		    is.close (); 

		    // send interrupt signal to the current thread ( this ) 
		    // we're in the runnable interface
		    // we must get the current thread and then interrupt it
		    Thread.currentThread ().interrupt ();

	    }catch ( IOException | SecurityException e ) { System.err.println ( e ); }
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
            }catch( IOException e ) { 
		    System.out.println("[ERROR] tunnel the error msg, on private messaging. Aborting client connection" );
	    }


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

    // need to refactor the isUserExist, so instead just created another function
    private boolean isUsernameCurrentlyUsed ( String name )
    {
        if ( clients.size() == 1 ) return false;
        else {
            for ( Entity client : clients )
            {
                if ( client != this.client && client.getName ( ).equals( name ) ) return true;   
                else continue;
            }
            return false;
        }

    }

    private void handleBroadcastEvent( Packet packet ) {
        boolean otherClients = this.clients.size() > 1;

        if ( otherClients ) { broadcast(packet); }
        else                _emitCacheSignal() ;
    }

    // to refactor ( like the whole file )
    private void sendPacket ( Packet packet ){
	    try {
		    os.write ( packet.output() );
		    os.flush()              ;
	    }
	    catch (IOException e ) { closeCurrentCommunicationWithClient (); }
    }

    // handling the connection.
    // i.e broadcasting to every client that the current user joined the gc
    private void handleConnectEvent ( Packet packet )
    {
        System.out.println( "[LOGGING] handling connection event" );
        // the connect packet will contain the name of curr client
        // check if that username is unique
        String name = packet.getMsg();


        if ( isUsernameCurrentlyUsed( name ) )
        {
            Packet err = new Packet ( "[ERROR] The name : (" + name + ") is already in use. Retry again.", PACKET_TYPE.REPEAT); 
            System.out.println( "[LOGGING] name already in use, sending ERROR packet" );
            sendPacket( err );
        }
        else{
            // TELLING THE CLIENT THAT THE USERNAME IS GOOD
            
            Packet ok = new Packet ( "OK!", PACKET_TYPE.OK );
            sendPacket ( ok );

            client.setName(name);
            String greetingAnnoucement = String.format ("%s joined the chat!", client.getName());
            packet.setMsg(greetingAnnoucement);

            broadcast(packet) ;
        }
    }

    // sending to all the clients the msg of the current client;
    public void broadcast( Packet packet )
    {
        byte[] bytes;
        OutputStream os ;

        // first check if there's another client ( i.e two or more )
        _broadcast( packet );
    }

    private void _broadcast ( Packet packet )
    {
        for ( Entity client : clients )
        {
            if ( client != this.client )
            {
                try {
                    client.getSocket().getOutputStream().write( packet.output() );
                    client.getSocket().getOutputStream().flush()                 ;
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
	    
	    if ( Thread.interrupted () ) break;
	    receivePacket();
	}
    }
    
}
