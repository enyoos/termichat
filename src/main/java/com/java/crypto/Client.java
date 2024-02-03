package com.java.crypto;

import java.io.IOException              ;
import java.io.InputStream              ;
import java.io.OutputStream             ;
import java.math.BigInteger             ;
import java.net.Socket                  ;
import java.util.Scanner                ;
import java.util.Arrays                 ;
import java.util.Base64                 ;
import java.util.ArrayList              ;
import java.util.Random                 ;
import java.util.NoSuchElementException ;

import com.happycli.java.*;

import static com.java.crypto.Encryption.Utils.*;
import static com.java.crypto.App.*             ;

import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet     ;
import com.java.crypto.Command.Action    ;
import com.java.crypto.Command.Sender    ;
import com.java.crypto.Command.Commands.*;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey           ;
import javax.crypto.spec.SecretKeySpec  ;

public class Client {

    private static final long TIME_OUT       = 500;
    // the info command is for debugging purposes
    public static String[] COMMANDS          = {
        "ping",
        "sinfo",
        "list",
        "exit",
        "dm",
        "help", 
        "info", 
        "create", 
        "listgc", 
        "join", 
        "ban", 
        "close",
    };

    private volatile boolean RECEIVEDPACKET  = false;
    private static char COMMAND_DELIMITER    = '/';
    private static long start                = 0;
    private static final String DEFAULT_HOST = "localhost";
    private static final String ALGORITHM    = "AES";
    private BigInteger MIXED_KEY                    ;
    private static final Scanner scanner     = new Scanner(System.in);
    private static final Random  random      = new Random ();
    private static final int MAX_BYTE_RECV   = 4096;
    private static final int SIZE_KEY_BIT    = 2048;

    // ------
    // for now let's make the P and G values client-side
    // convert them to the adequate class when doing the rsa e    ncryption
    // ------
    private static final BigInteger G        = new BigInteger ( "29477216248556571790188355105958421171017588675856077450982938565960829212013054309462070998391461094041831402145531512255212461711077147264591937559444025009276513139697905405362311715793483901079228943997043492071835753652280403690828672211623822204487686701542659932108817401902465502854674782655107704489715333636274537660725752397542128190204586783024276096660727817435884944669373187891992147367420696864345419030748867924233115063034424369413695996037194187915663302698301730217385865205733532876034279732307130146367186464815516016177746732081584886750880459484847733607088734405196106390759788696569480087431" );
    private static final BigInteger P        = new BigInteger ( "19474466331652733655832051458553385064043212458095124200912009294819345895972189505915769512313480865923793313340501916953015584166532313103740069387851835022945716114704862091376298763276441380923154722122437014796553689749812134516963749832403414532662243582453368471573597878812661606345274421114769635444409570652211078130819498231470427749591748807061847547957719205965753895170094138164528351514966602849951310091191492513611565485056820930462260513041657995223151734292549103405648018769834599247783995393243168507371182616792568116715228533111095312406381801789553091907467985780343386183520134766937438245637" );

    private SecretKey sk         ;
    private BigInteger tempSk    ;
    private BigInteger diffieKey ;
    private InputStream is       ; // channel where you can read from
    private OutputStream os      ; // chanel where you can write from
    private Sender sender        ; // this is essential to the command pattern
    private Socket socket        ;
    private String name          ;

    private String currentGrpName   = "default";
    private final TextureBuilder tb = new TextureBuilder();

    // all the getters
    public String getName          () { return this.name      ; }
    public BigInteger getMK        () { return this.MIXED_KEY ; }
    public BigInteger getDiffieKey () { return this.diffieKey ; }
    public BigInteger getTempSk    () { return this.tempSk    ; }
    public SecretKey getSk         () { return this.sk        ; }

    public Client(){}
    public Client ( String name, int port )
    {
        try {
            this.name=name;
            this.socket = new Socket(DEFAULT_HOST, port);

            is             = socket.getInputStream();
            os             = socket.getOutputStream();
            sender         = new Sender(os);

            this.genKey();
            mainLoop()   ;

        }catch( IOException err ) {
            exitAppOnServerShutDown();
        }
    }

    private void sendNamePacket ( )
    {

        // as you can see we're not encrypting the name sent.
        // in order to remove the slight overhead.
        System.out.println("[LOGGING] sending the name to the server and other parties.");
        Packet SecondDefaultPacket = new Packet(name, PACKET_TYPE.CONNECT);

        sendPacket(SecondDefaultPacket);
    
        // we should receive OK or ERR
        receivePacket();

    }

    private void sleepClient ( long st )
    {
        try{
            Thread.sleep ( st );
        }catch ( InterruptedException e ) { System.out.println( "[LOGGING, ERROR] unpatient" ); }
    }

    // this the main loop
    // think of it like the main Game Loop
    public void mainLoop()
    {
        // sedning also the key
        sendNamePacket ();

        sendKey();
        inputTask      ();

        while( this.socket.isConnected() ) { receivePacket(); }
    }

    public void interruptThreadWithName ( String name )
    {
	    int MAX_SIZE     = 5;
	    Thread[] threads = new Thread[MAX_SIZE];
	    Thread.enumerate ( threads );

	    for ( Thread t : threads ) { if ( t.getName().equals ( name ) ) { t.interrupt(); } }

    }

    private Packet encryptMsg ( String input )
    {

	    IvParameterSpec iv = generateIv ();
	    String msg         = String.format ( "%s > %s", name, input );
	    String digest      = encrypt  ( msg, sk, iv );
	    byte[] send        = paddWithIv ( iv, digest ); // [iv, msg] ( padd operation )
	    Packet packet      = new Packet(send, PACKET_TYPE.BROADCAST);

	    return packet;
    }

    // asks, while the program is running the input of the user
    public void inputTask ( )
    {

        String tname = "inputTask";
        Thread t     = new Thread( new Runnable() {
            @Override
            public void run()
            {

                String prompt   = String.format( "%s [%s] > ", name, currentGrpName );
                boolean running = true                         ;
                String command  = ""                           ;
                Action command_ = null                         ;
                String input    = ""                           ;
                Packet packet   = null                         ;

                while ( running )
                {
                    //if ( !RECEIVEDPACKET ){
                    try{
                        promptUserForMsg ( 
                            prompt,
                            command,
                            command_,
                            input,
                            packet 
                        );
                    }
                    catch ( NoSuchElementException ignore ) {
                        exit();
                    }
                //else continue;
                }
            }
        }, tname);

        t.start();
    }

    private void exit()
    {
        handleInfoResponse("Killing main process. Hasta La Vista Amigo!");
        System.exit( 0 );
    }

    private void promptUserForMsg ( String context, String command, Action command_, String input, Packet packet )
    {

        System.out.print(context); 
        input = scanner.nextLine();
        // on Input check if there's any command
        // a command starts with the COMMAND_DELIMITER

        if ( input.isEmpty() ) { handleErrResponse("[ERROR] you need to say something, everyone is waiting ..."); }
        else
        {
            // we can compare the hash ?
            if ( input.charAt(0) == COMMAND_DELIMITER )
            {
                command = input.substring(
                    1, 
                    input.indexOf(" ") == -1 ? input.length() : input.indexOf(" ")
                );

                if ( command.equals( COMMANDS[0]) )
                {
                    start = System.currentTimeMillis();
                    command_ = new PingServerOperation(sender);
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[1]) )
                {
                    command_ = new ShowServerInfoOperation( sender );
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[2]) )
                {
                    // split at the first occurence of " "
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new ListAllClientsNamesOperation( sender , input );
                    command_.execute();
                }
                else if ( command.equals( COMMANDS[3]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new ExitChatApplicationOperation( sender );
                    command_.execute();
                    
                    this.exit();
                }
                else if ( command.equals(COMMANDS[4]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new DMUserOperation(sender, input);
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[5]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];

                    System.out.println( "input : " + input );

                    command_ = new ShowAllCommandsOperation(sender, input);
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[6]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new ShowAllUserInformation( sender, this );
                    command_.execute();
                } 
                else if ( command.equals(COMMANDS[7]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new CreateGroupOperation( sender, input );
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[8]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new ShowAllGroupChatsOperation( sender, input );
                    command_.execute();
                }
                else if ( command.equals(COMMANDS[9]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];

                    // before joining, we reinit our data.
                    // we generate our key
                    // then send it ( after the execute statement ).
                    this.sk     = null;
                    this.tempSk = null;
                    this.genKey()     ;

                    command_ = new JoinGroupChatOperation( sender, input );
                    command_.execute();
                    sendKey();

                }
                else if ( command.equals(COMMANDS[10]) )
                {
                    input    = splitAtFirstOccurenceOf( " ", input )[1];
                    command_ = new BanEntityOperation( sender, input );
                    command_.execute();
                }
                else 
                {
                    String meantCmd = lev ( input, COMMANDS );
                    handleErrResponse( 
                            "[ERROR] this command doesn't exist. Did you mean " + meantCmd + " ?"
                    );
                }
            }
            else {

                // by default, we broadcast each msg
    // we generate the iv for each   msg.
    boolean hasSk = sk != null;

    if ( hasSk ){
        sendPacket ( encryptMsg ( input ) );
    }

    else { showNoSKWarning( ); }
            }
        }

    }

    private void showNoSKWarning( )
    { 
	    // pause the thread
	    handleInfoResponse( "[WARNING] You don't have a Secret key and this will result in unencrypted messages. You must wait for someone to join the room." );
    }

    // handling the events.
    private void handleDisconnectEvent ( Packet packet ) { 

        String msg = packet.getMsg();
        System.out.println(
            tb.content(msg).underline(true).bold(true).blink(true).build()
        );

    ;}
    private void handleInvalidUsernameEvent ( Packet packet ) {

	    // interrrupt the key receival
	    // and also interrupt the inputTask
	    
	    // interruptThreadWithName ( "sendNameAndKeyTask" );
	    // interruptThreadWithName ( "inputTask"          );
	    handleResponseEvent     ( packet );

	    this.name = promptUsername();

	    // how about we use the Thread.wait functionality ?
	    // sendNameAndKey     ();
	    // inputTask()          ;
    }
    private void handleBroadcastEvent  ( Packet packet ) {

	    byte[] bytes= packet.getMsg_()                             ;
	    byte[] iv_  = unpaddIv ( bytes )                           ; 
            byte[] msg  = unpaddIvAndGetMsg ( bytes )                  ;

	    IvParameterSpec iv = new IvParameterSpec ( iv_ ) ;
	    String dec         = decrypt ( bytes2Str ( msg ), this.sk, iv );  
	    System.out.println( dec );
    }
    private void handleDefaultUnknownPacketEvent ( Packet packet ) { System.out.println( "[LOGGING] received unknown packet : " + packet ); }

    private static final byte[] hasSK    = {1};
    private static final byte[] notHasSk = {0};

    private void sendKey ( )
    {
	    // you broadcast your key thanks
	    // bfore even sending your name
	    System.out.println("[LOGGING] sending the mixed key to the server.");
	    byte[] bytes   = this.MIXED_KEY.toByteArray() ;
	    Packet firstDefaultPacket = new Packet( bytes , PACKET_TYPE.KEY);

        sendPacket ( firstDefaultPacket );
    }


    public void setKey ( byte[] recKey )
    {

        BigInteger recKey_= new BigInteger ( recKey );
        System.out.println( "[LOGGING] received key : " + recKey_ );
        this.tempSk       = recKey_.modPow ( this.diffieKey, P );
        byte[] bytes      = cropBigIntBy ( 16, this.tempSk);
        this.sk           = new SecretKeySpec ( bytes , ALGORITHM );
        System.out.println( "[LOGGING] generating new secret key : " + this.tempSk );
	    
    }

    private void ignore(){};

    private void handleKeyEvent ( Packet packet ) {

        byte[] bytes   = packet.getMsg_();

        // we check if we have already a private key
        boolean hasSK = this.sk != null;
        if ( hasSK ) ignore();
        else         {
            setKey ( bytes);
            sendKey(      );
        }

    }

    // we shouldn't renew, rather update the keys ( the secret keys becomes the newly mixed key )
    private void updateKey ()
    {
        // our new diffieKey becomes the secret
        boolean hasSk = this.sk != null;
        if ( hasSk ) 
        {
            this.diffieKey  = this.tempSk;
        }

        genMKey();

        this.sk     = null;
        this.tempSk = null;
    }

    // generates the mixed Key from an initial key
    private void genMKey( ) { 
        this.MIXED_KEY = this.G.modPow ( this.diffieKey, P );
    }

    public void genKey ( )
    {

        this.diffieKey = gKey ( SIZE_KEY_BIT );
        genMKey();

    }

    private void handleConnectEvent( Packet packet ) 
    { 
        // we should ``update`` the key.
        updateKey();

	    String uncMsg = packet.getMsg();	    
        handleOKResponse( uncMsg );
    }

    // painted with yellow and underlined
    private void handleInfoResponse ( String info )
    {
        String out = tb.content( info ).underline( true ).foreground( PaintOptions.YELLOW ).build();
        System.out.println( out );

        tb.clear();
    }

    // the ok response shall be in GREEN and underlined
    private void handleOKResponse   ( String respContent ) { 
        String out = tb.content ( respContent ).underline( true ).foreground(PaintOptions.GREEN).build();
        System.out.println ( out );

        tb.clear();
    }

    // the err response is going to be red with the underlined effect
    private void handleErrResponse  ( String errMsg      ) {
        String out = tb.content ( errMsg ).underline(true).foreground(PaintOptions.RED).build();
        System.out.println ( out ); 

        tb.clear();
    }

    // the join response 
    private void handleJoinResponse ( String respContent ) {

        // change the gc name;
        // get the idx for ':' increment it and substring until the end.

        String[] tokens       = splitAtFirstOccurenceOf(",", respContent);
        this.currentGrpName   = tokens[0];
        String out            = tokens[1];

        int idx               = out.indexOf( ':' )      ;
        String gcName         = out.substring( idx + 1 );
        this.currentGrpName   = gcName                  ;

        handleOKResponse( out );
    }

    // the dm will be purple and bold
    private void handleDMResponse      ( String respContent ) { 
        String out = tb.content ( respContent ).bold ( true ).foreground ( PaintOptions.MAGENTA ).build();
        System.out.println ( out ); 

        tb.clear();
    }

    // blue bold, underline and blinking
    private void handleListResponse ( String list )
    {
        String out = tb.content ( list ).underline ( true ).foreground ( PaintOptions.BLUE ).bold( true ).blink( true ).build();
        System.out.println ( out );

        tb.clear();
    }

    private void handleUnknownResponse ( String respType    )                    
    { 
        // the stdin should be a log file 
        handleErrResponse  ( "[LOGGING] received an unknow response with value : " + respType );
    }

    private static final String[] RESPONSES = {
        "ok"    , // if name is correct
        "error" , // if there's an error
        "joined", // succesfully joined a group chat
        "list"  , // shows a list of all the users in some group chat
        "dm"    , // dm from someone
        "pong"  , // receiving the pong response
    };

    private void handleResponseEvent ( Packet packet ) {


        String[] tokens    = splitAtFirstOccurenceOf( ",", packet.getMsg() );
        String respType    = tokens[0];
        String respContent = tokens[1];

        // handling ok TypeScenario
        if      ( respType.equals ( RESPONSES[0]) )
        { handleOKResponse( respContent )  ; }
        
        else if ( respType.equals ( RESPONSES[1]) )
        { handleErrResponse( respContent ) ; }

        else if ( respType.equals ( RESPONSES[2]) )
        { handleJoinResponse( respContent ); }

        else if ( respType.equals ( RESPONSES[3]) )
        { handleListResponse( respContent ); }

        else if ( respType.equals ( RESPONSES[4]) )
        { handleDMResponse( respContent )  ; }

        else if ( respType.equals ( RESPONSES[5]) )
        { handlePongResponse( respContent )  ; }

        else { handleUnknownResponse(respType, respContent); }
    }

    private void handlePongResponse( String rc )
    { 
        long pong_time = System.currentTimeMillis() - start;
        rc            += " [" + " " + pong_time + " ms " + "]"; 
        String out     = tb.content ( rc ).underline( true ).bold(true).blink(true).foreground(PaintOptions.GREEN).build();

        System.out.println ( out );
        tb.clear();
    }

    private void handleUnknownResponse(String rt, String rc)
    {
        String msg = "[LOGGING] Unknown response type : " + rt + " with content : " + rc;
        String out = tb.content ( msg ).underline ( true ).bold( true ).blink( true ).build();
        System.out.println ( out );

        tb.clear();
    }

    private void handleOKEvent ( Packet packet ) { System.out.println( "[SERVER] 200 OK!" )   ;}

    private void receivePacket( )
    {
	
	this.RECEIVEDPACKET = false;
        Packet packet;

        try {

            byte[] allocateByteMsgArray = new byte[MAX_BYTE_RECV];
            is.read(allocateByteMsgArray);

            packet = new Packet( allocateByteMsgArray );

	    this.RECEIVEDPACKET = true;

            switch (packet.getType()) {

                // RESPONSE enum means the PRIVATE communication between the client and the server ( for instance )
                // if the user wish to dump data from the server ( with the use of commands ).
                // They're not encrypted
		
                case RESPONSE:
                    handleResponseEvent( packet );
                    break;

                case DISCONNECT:
                    handleDisconnectEvent ( packet );
                    break;

                case CONNECT:
                    // ON CONNECT ( meaning a new client joined )
                    //
                    handleConnectEvent    ( packet );
                    break;

                case REPEAT:
                    handleInvalidUsernameEvent ( packet ); 
                    break;

                // what is broadcasting
                // it is the communication between the users.
                // they shall be decrypted here ( on the client side )
                case BROADCAST:
                    handleBroadcastEvent( packet ); 
                    break;

                // we receive the key from the other user
                // which in theory should be the mixed key
                // we should take that mixed key and put modPow it.
                case KEY: 
                    handleKeyEvent( packet );
                    break;

                default:
                    handleDefaultUnknownPacketEvent ( packet );
                    break;
            }

        }catch ( IOException e ){ exitAppOnServerShutDown(); }
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
            os.flush();

        }
        catch ( IOException e )
        { 
            // if we're here, then the server must have shut down.
            // we show to the user that the server shut down.
            // exit the application with status 1 ( error )
            exitAppOnServerShutDown();
       }
    }

    // static, so that this function can be called inside static context
    private static void exitAppOnServerShutDown()
    {
        String msg = "The server couldn't take it anymore...";
        System.out.println(msg);
        System.exit( 1 );
    }
}
