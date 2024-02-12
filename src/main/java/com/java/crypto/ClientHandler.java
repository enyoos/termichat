package com.java.crypto;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List     ;
import java.util.Iterator ;
import java.util.Optional ;
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
        "list", "server_info", "ping", "listgc", "exit"
    };

    // specific to each client.
    // instead how about we store them on the client side ?
    // first check if there are some DEFAULT_GROUP.clients and then report ?

    // update it at each iter ?
    // at each msg ?
    private static IvParameterSpec iv = new IvParameterSpec ( new byte[] {
	    -30, 103, -50, -92, -70, 51, -94, 94, 90, 119, 116, -113, -116, 120, 23, -36
    });

    // we got the DEFAULT_GROUP
    // there's also different groups 
    private static final Group DEFAULT_GROUP     = new Group ("fossium", "default_global"); // fossium is me !
    private static final ArrayList<Group> GROUPS = new ArrayList<>();

    static {
        GROUPS.add ( DEFAULT_GROUP ); 
    }

    // private static ArrayList<Entity> DEFAULT_GROUP.clients = new ArrayList<>();
    private static final int MAX_SIZE = 4096;
    private int msgLength             = MAX_SIZE;
    private static String serverInstanceName;

    private Entity client                   ;
    private Group currentGrp = DEFAULT_GROUP;  // keep track of the current group
    


    // receiving 
    private InputStream  is;
    // writing 
    private OutputStream os;

    public ClientHandler( Socket socket ) {

        System.out.println( "[LOGGING] new clientHandler..." );
        client = new Entity(socket);

        // each time we add some DEFAULT_GROUP.clients to the client array
        // we send all the missed packet
        DEFAULT_GROUP.clients.add(client);

        // what's the plan ?
        // make the DEFAULT_GROUP.clients array an observable
        // such that on change we send a missing packet.


        try {

            is = socket.getInputStream();
            os = socket.getOutputStream();
           
        }catch( IOException e ){ System.out.println( "Couldn't handle the client request."); }
    }

    public static void setServerInstanceName ( String name ) { ClientHandler.serverInstanceName = name;}

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
                    boolean exit = false;
                    handleDisconnectEvent(packet, exit);
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

                case CREATE:
                    handleGroupCreation( packet );
                    break;

                case JOIN:
                    handleJoinEvent( packet );
                    break;
                
                case BAN:
                    handleBanEvent ( packet );
                    break;

                case CLOSE:
                    handleCloseEvent( packet );
                    break;

                default:
                    handleUnknownPacket ( packet );
                    break;
            }
        }catch( IOException e ){
            // if we're here, meaning that the user disconnected
            boolean exit = true;
            handleDisconnectEvent(packet, exit);
        }
    }

    private void handleCloseEvent( Packet packet )
    {
        String[] gcs = packet.getMsg().split( "," );
        for ( String gc : gcs )
        {
            int ret = close ( gc );
            if ( ret == 1 ) return  ;
            else            continue;
        }
    }

    private int close ( String grpName )
    {
        // check if the grpName exist
        // and if authAdmin()
        if ( gcNameUsed ( grpName ) && authAdmin( getGrp ( grpName ) ) )
        {
            GROUPS.remove ( getGrp ( grpName ) ); 
            return 0;
        }

        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
        String      msg  = "error, [ERROR] Group chat name : " + grpName + " doesn't exist or you're not its admin";

        sendPacket( new Packet ( msg, type ) );
        return 1;
    }

    private Group getGrp ( String grpName )
    {

        for ( Group grp : GROUPS )
        {
            if ( grpName.equals ( grp.name ) ) return grp;
            else                               continue  ;
        }

        return null;
    }

    private void sendPacketTo ( Entity client, Packet packet )
    {
        try{
            OutputStream temp = client.getSocket().getOutputStream();

            temp.write ( packet.output() );
            temp.flush (        );
        }
        catch ( IOException e )
        {
            System.out.println( "something went wrong ... " );
        }
    }

    private boolean authAdmin () { return this.currentGrp.admin.equals ( this.client.getName() ); }
    private boolean authAdmin ( Group grp ) { return grp.admin.equals( this.client.getName() ); }

    private void ban ( String name, int duration )
    {

        // kick the user from the currentGrp chat 
        // but the user can always re-enter
        // we must keep tracks of the banned ppl;
        // bannedPool for instance that evacuates itself by the time
        // like a timed-out cache ( to be implemented )
        // like an arrayList that manages it self
        
        Iterator<Entity> it = this.currentGrp.clients.iterator();

        while ( it.hasNext() )
        {
            Entity cl = it.next();
            if ( cl.getName().equals ( name ) )
            {
                this.currentGrp.bannedPool.add ( name, duration );
                sendPacketTo( cl, new Packet ( PACKET_TYPE.BAN ) );
                it.remove ( ); 

                // then add the client to the default grp
                this.DEFAULT_GROUP.clients.add ( cl );
            }
            else                                    continue;
        }

    }

    private void handleBanEvent ( Packet packet )
    {
        String pattern = "\\|";
        String[] msg   = packet.getMsg ( ).split(pattern);
        System.out.println ( Arrays.toString( msg ) );

        pattern        = ",";
        String[] names = msg[0].contains(pattern) ? msg[0].split(",") : new String[]{msg[0]};
        String   time  = msg[1];

        for ( String name : names )
        {
            // check if the user exist in the current group
            if ( isUserExist( name ) && authAdmin() ) {
                ban ( name, Integer.parseInt ( time ) );
                return;
            }
            else                       continue;
        } 

        sendPacket( 
            new Packet ( 
                "error,The user you're trying to ban doesn't exist or you don't have the admin status.",
                PACKET_TYPE.RESPONSE 
            ) 
        );
    }

    private Optional<Group> gcPresent( String name )
    {
        for ( Group grp : GROUPS ) { if ( grp.name.equals ( name ) ) return Optional.of(grp); }
        return Optional.empty();
    }

    private void handleJoinEvent( Packet packet )
    {
        String gcName       = packet.getMsg();
        Optional<Group> opt = gcPresent ( gcName );

        if ( opt.isPresent() ) 
        {

            // add the user to the group
            // and remove him from the precedent group
            // and broadcast disconnect packet to all the users 
            Group joined = opt.get();

            if ( !joined.bannedPool.isBanned( this.client.getName() ) )
            {
                joined.clients.add( this.client );

                this.currentGrp.clients.remove( this.client );
                Packet mockPacket = new Packet( );

                boolean exit = false;
                handleDisconnectEvent(mockPacket, false);

                this.currentGrp   = joined;

                // let the user know that he joined a new group
                // and broadcast his new arrival
                Packet letDUserKnow = new Packet ( "joined,You joined the gc : " + gcName, PACKET_TYPE.RESPONSE );
                sendPacket ( letDUserKnow );

                String msg = String.format ( "%s joined the group chat", this.client.getName() );
                Packet letDOtherUsersKnow = new Packet ( msg, PACKET_TYPE.CONNECT );

                broadcast( letDOtherUsersKnow );
            }
            else 
            {
                int when       = this.currentGrp.bannedPool.when ( this.client.getName() );
                String msg     = String.format ( "error,[ERROR] You're banned from this group. Rejoin in : " + when, gcName );
                Packet packet_ = new Packet ( msg, PACKET_TYPE.RESPONSE );
                sendPacket ( packet_ );
            }
        }

        else
        {
            String msg = String.format ( "error,[ERROR] the group chat with name : %s doesn't exist", gcName );
            Packet packet_ = new Packet ( msg, PACKET_TYPE.RESPONSE );
            
            sendPacket ( packet_ );
        }
    }

    private boolean gcNameUsed ( String name )
    {
        for ( Group group : GROUPS )
        {
            if ( group.name.equals ( name ) ) return true;
            else continue;
        }
        
        return false;
    }

    private void handleGroupCreation( Packet packet )
    {
        String gcName    = packet.getMsg() ;
        String adminName = client.getName();

        if ( !gcNameUsed ( gcName ) )
        {   

            Group grp        = new Group (adminName, gcName);
            PACKET_TYPE type = PACKET_TYPE.RESPONSE         ;
            String msg       = "ok,Created GroupChat"       ;
            Packet send      = new Packet ( msg, type )     ;

            GROUPS.add ( grp );
            sendPacket ( send );
        }
        else {
            sendPacket (
                new Packet ( 
                    "error,[ERROR] group chat name : " + gcName + " is already in use.",
                    PACKET_TYPE.RESPONSE 
                )
            );
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
    
    private void handleQueryClient( Packet packet )
    {

        String command       = packet.getMsg();
        String optionalValue = "";
        int limit_client     = currentGrp.clients.size();
        int limit_gcs        = GROUPS.size();           
        
        if ( command.contains ( "," ) )
        {
            String[] values = splitAtFirstOccurenceOf( ",", command );
            optionalValue   = values[0];
            command         = values[1]; 
        }
        
        // handling the list command
        if( command.equals(COMMANDS[0]))
        { 
            if ( !optionalValue.isEmpty() ){
                int temp          = Integer.parseInt ( optionalValue );
                // by default we check if it is greater than the number
                // of users in the DEFAULT GROUP
                boolean outBounds = temp > GROUPS.get(0).clients.size();
                limit_client      = outBounds ? GROUPS.get(0).clients.size() : temp;
            }

            sendListOfUsersIncludingSelf( limit_client ); 
        }

        // handlign the server_info command
        else if ( command.equals(COMMANDS[1]))
        { sendServerInstanceInfo();}

        // handling the ping command
        else if ( command.equals(COMMANDS[2]))
        { sendPongMessageToClient(); }
        
        // handling the listgc commands
        else if ( command.equals(COMMANDS[3]))
        { 
            if ( !optionalValue.isEmpty() ){ 

                int temp          = Integer.parseInt(optionalValue);
                boolean outBounds = temp > GROUPS.size();
                limit_gcs         = outBounds ? GROUPS.size() : temp;
            }

            sendListOfGroupChats( limit_gcs ); 
        }
        
        else if ( command.equals(COMMANDS[4]))
        {
            boolean exit = true;
            handleDisconnectEvent( packet, true );
        }

        // do nothing, return nothing.
        else { return; }

    }

    // private static final Group DEFAULT_GROUP     = new Group ("default", "fossium"); // fossium is me !
    // private static final ArrayList<Group> GROUPS = new ArrayList<>()               ;


    private void sendListOfGroupChats(int limit)
    {
        Packet packet    = new Packet();
        StringBuilder sb = new StringBuilder();
        Group temp       = null;


        sb.append(DELIMITER);
        
        for ( int i = 0; i < limit ; i ++ ) {

            temp = GROUPS.get ( i );
            sb.append ( "\n" + temp.toString() );

            if ( temp == this.currentGrp ) sb.append (" (current)"); 
            else continue;
        }

        sb.append("\n");
        sb.append( DELIMITER );

        String msg = "list," + sb.toString();

        packet.setMsg (msg);
        packet.setType( PACKET_TYPE.RESPONSE );

        sendPacket ( packet );
    }

    private void sendPongMessageToClient()
    {
        Packet packet = new Packet();

        PACKET_TYPE type = PACKET_TYPE.RESPONSE;
        String resp = "pong,Pong, server!";

        packet.setMsg(resp);
        packet.setType(type);
        
        sendPacket ( packet );
    }

    private void sendServerInstanceInfo()
    {
        Packet packet = new Packet();
        
        // what to send and what not to send. ( as server info )
        String info = String.format("ok,Server name : %s", ClientHandler.serverInstanceName);
        PACKET_TYPE type = PACKET_TYPE.RESPONSE;

        packet.setMsg(info);
        packet.setType(type);

        sendPacket ( packet );

    }

    private static final String DELIMITER = "------------------";

    private void sendListOfUsersIncludingSelf( int limit )
    {
        Packet packet = new Packet();
        StringBuilder sb = new StringBuilder();

        sb.append("\n" + DELIMITER);

        // DEFAULT_GROUP.clients

        for ( int i = 0; i < limit ; i ++ )
        {
            Entity client_ = this.currentGrp.clients.get ( i );
            if ( client_ == this.client )
            { 
                sb.append(
                    String.format ( "- %s ( you )\n", this.client.getName())
                );
            }
            else sb.append(String.format("- %s\n", client_.getName()));
        }

        sb.append(DELIMITER);

        String msg = "list," + sb.toString();

        packet.setMsg(msg);
        packet.setType(PACKET_TYPE.RESPONSE);

        try{

            this.os.write(packet.output());
            this.os.flush();
        }catch (IOException e ) {System.out.println( "[ERROR] couldn't send the result of the command with value : " + COMMANDS[0]);}
    }

    private void removeUserByName ( Entity client )
    {
        // getting the place of the name in the usernames array;
        int idx = DEFAULT_GROUP.clients.indexOf(client);

        if ( idx >= 0 ) { DEFAULT_GROUP.clients.remove(idx); }
        else { return ; }
    }


    private void handleDisconnectEvent( Packet packet, boolean exit )
    {

        // remove the user from the 
        String msg = "[SERVER] The user " + client.getName() + " has something better to do !";
        PACKET_TYPE type = PACKET_TYPE.DISCONNECT;

        packet.setType(type);
        packet.setMsg(msg);

        removeUserByName(client);
        broadcast(packet);

        if ( exit ) closeCurrentCommunicationWithClient ();
    }

    private void genocideOf ( String clientName )
    {
        // get the iter out of the array List
        // since arrayList doesn't allow for concurrent modif 
        Iterator<Group> grpIter = GROUPS.iterator();
        Group           temp    = null;

        while ( grpIter.hasNext() )
        {
            temp = grpIter.next();
            if ( temp.admin.equals(clientName) ) grpIter.remove();
            else continue;
        }

        // but also remove him from the clients arrayList;
        Iterator<Entity> entityIter = DEFAULT_GROUP.clients.iterator();
        Entity           temp_      = null;

        while ( entityIter.hasNext() )
        {
            temp_ = entityIter.next();
            if ( temp_.getName().equals( clientName ) ) entityIter.remove();
            else continue;
        }

    }

    private void closeCurrentCommunicationWithClient ( )
    {
	    try{

            // remove every chat group that belong to the client
            genocideOf ( this.client.getName() );
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

        if ( ! isUserExist( targetUser ) ) 
        {

            // send to the user the msg with the errorr
            String err = "error,[ERROR] client with name " + targetUser + " doesn't exist";
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

        for ( Entity client : currentGrp.clients )
        {
            if ( client.getName().equals(name) )
            {
                msg = String.format( "dm,%s > %s", name, content);
                type= PACKET_TYPE.RESPONSE;

                packet = new Packet(msg, type);

                try{
                    client.getSocket().getOutputStream().write(packet.output());
                    client.getSocket().getOutputStream().flush();
                }
                catch( IOException e ){ System.out.println("ERROR, couldn't send the private message.");}
            }
        }

    }

    private boolean isUserExist ( String name )
    {
        // first check if the targetUser is present in the group chat
        for ( Entity client : currentGrp.clients )
        { if ( client.getName().equals(name) ) { return true; } }

        return false;
    }

    // need to refactor the isUserExist, so instead just created another function
    private boolean isUsernameCurrentlyUsed ( String name )
    {
        if ( DEFAULT_GROUP.clients.size() == 1 && GROUPS.size() == 1 ) return false; 
        else
        {
            for ( Group g : GROUPS )
            {
                for ( Entity client : g.clients )
                {
                    if ( client != this.client && client.getName ( ).equals( name ) ) return true;   
                    else continue;
                }
            }

            return false;
        }

    }

    private void handleBroadcastEvent( Packet packet ) { broadcast(packet); }

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
            Packet err = new Packet ( "error,[ERROR] The name : (" + name + ") is already in use. Retry again.", PACKET_TYPE.REPEAT); 
            System.out.println( "[LOGGING] name already in use, sending ERROR packet" );
            sendPacket( err );
        }
        else{
            // TELLING THE CLIENT THAT THE USERNAME IS GOOD
            
            Packet ok = new Packet ( "ok,OK!", PACKET_TYPE.RESPONSE );
            sendPacket ( ok );

            client.setName(name);
            String greetingAnnoucement = String.format ("%s joined the chat!", client.getName());
            packet.setMsg(greetingAnnoucement);

            broadcast(packet) ;
        }
    }

    // sending to all the DEFAULT_GROUP.clients the msg of the current client;
    public void broadcast( Packet packet )
    {
        byte[] bytes;
        OutputStream os ;

        // first check if there's another client ( i.e two or more )
        _broadcast( packet );
    }

    private void _broadcast ( Packet packet )
    {
        ArrayList<Entity> to = this.currentGrp.clients;
        OutputStream os      = null;

        for ( int i = 0 ; i < to.size(); i ++ )
        {
            Entity client_ = to.get ( i );
            if ( client_ != this.client ){
                try{
                    os = client_.getSocket().getOutputStream();
                    os.write( packet.output() );
                    os.flush();
                }
                catch ( IOException e )
                {
                    System.out.println( "[ERROR] Something went terribly wrong..." );
                }
            }
            else continue;
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
