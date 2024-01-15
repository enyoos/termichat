package com.java.crypto;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket     ;
import java.util.Scanner   ;
import java.util.Arrays    ;
import java.util.Base64    ;
import java.util.ArrayList ;
import java.util.Random    ;

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

    private static final long TIME_OUT       = 1000;
    public static String[] COMMANDS          = {
        "ping", "server_info", "list", "exit","dm","help"
    };

    private volatile boolean RECEIVEDPACKET  = false;
    private static char COMMAND_DELIMITER    = '/';
    private static final String DEFAULT_HOST = "localhost";
    private static final String ALGORITHM    = "AES";
    private BigInteger MIXED_KEY                    ;
    private static final Scanner scanner     = new Scanner(System.in);
    private static final Random  random      = new Random ();
    private static final int MAX_BYTE_RECV   = 1024;
    private static final BigInteger G        = new BigInteger ( "29477216248556571790188355105958421171017588675856077450982938565960829212013054309462070998391461094041831402145531512255212461711077147264591937559444025009276513139697905405362311715793483901079228943997043492071835753652280403690828672211623822204487686701542659932108817401902465502854674782655107704489715333636274537660725752397542128190204586783024276096660727817435884944669373187891992147367420696864345419030748867924233115063034424369413695996037194187915663302698301730217385865205733532876034279732307130146367186464815516016177746732081584886750880459484847733607088734405196106390759788696569480087431" );
    private static final BigInteger P        = new BigInteger ( "19474466331652733655832051458553385064043212458095124200912009294819345895972189505915769512313480865923793313340501916953015584166532313103740069387851835022945716114704862091376298763276441380923154722122437014796553689749812134516963749832403414532662243582453368471573597878812661606345274421114769635444409570652211078130819498231470427749591748807061847547957719205965753895170094138164528351514966602849951310091191492513611565485056820930462260513041657995223151734292549103405648018769834599247783995393243168507371182616792568116715228533111095312406381801789553091907467985780343386183520134766937438245637" );



    // ------
    // for now let's make the P and G values client-side
    // convert them to the adequate class when doing the rsa e    ncryption
    // ------
   
    private SecretKey sk                       ;
    private BigInteger diffieKey               ;
    private InputStream is                     ; // channel where you can read from
    private OutputStream os                    ; // chanel where you can write from
    private Sender sender                      ; // this is essential to the command pattern
    private Socket socket                      ;
    private String name                        ;


    public Client(){}
    public Client ( String name, int port )
    {
        try {
            this.name=name;
            this.socket = new Socket(DEFAULT_HOST, port);
            this.socket.setTcpNoDelay(true);

            is     = socket.getInputStream();
            os     = socket.getOutputStream();
            sender = new Sender(os);
        
            // generate the pk only !
            // the sk will be computed thanks to the diffie hellman exchange.
	    this.genKey();

            mainLoop();
        }catch( IOException err ) {
            exitAppOnServerShutDown();
        }
    }

    private void sendKey ( )
    {
	    // you broadcast your key thanks
	    // bfore even sending your name
	    System.out.println("[CLIENT] sending the mixed key to the server.");
	    byte[] bytes   = this.MIXED_KEY.toByteArray() ;
	    Packet firstDefaultPacket = new Packet( bytes , PACKET_TYPE.KEY);

	    // sending the content of the packet 
	    sendPacket ( firstDefaultPacket );
    }

    private void sendNamePacket ( )
    {

    // as you can see we're not encrypting the name sent.
    // in order to remove the slight overhead.
        System.out.println("[CLIENT] sending the name to the server and other parties.");
        Packet SecondDefaultPacket = new Packet(name, PACKET_TYPE.CONNECT);
        sendPacket(SecondDefaultPacket);

	// we should listen for the output.
	receivePacket ();
    }


    private void sleepClient ( long st )
    {
	try{
		Thread.sleep ( TIME_OUT );
	}catch ( InterruptedException e ) { System.out.println( "unpatient" ); }
    }


    private void sendNameAndKey ( )
    {

	// then when the key exchange is finished, send the username.
        sendKey();

	// for some reason the server can't read the key
	// only if I sleep here.
	// for like 2 s before sending the name.

	sleepClient ( 2000 );

	// firstly, we notify the server of our name,
        // using the CONNECT packet
        sendNamePacket();

    }

    // this the main loop
    // think of it like the main Game Loop
    public void mainLoop()
    {
	
	sendNameAndKey ();
        inputTask      ();

        while( this.socket.isConnected() ) { receivePacket(); }
    }

    // asks, while the program is running the input of the user
    public void inputTask ( )
    {
        new Thread( new Runnable() {
            @Override
            public void run()
            {

		String prompt   = name + " >";
		boolean running = true       ;
		String command  = ""         ;
		Action command_ = null       ;
		String input    = ""         ;
		Packet packet   = null       ;

                while ( running )
                {
			if ( !RECEIVEDPACKET ){
				promptUserForMsg ( prompt,
						command,
						command_,
						input,
						packet );
			}
			else continue;
                }
            }
        }).start();
    }

    private void promptUserForMsg ( String context, String command, Action command_, String input, Packet packet )
    {
			System.out.print(context); 
                    input = scanner.nextLine();

                    // on Input check if there's any command
                    // a command starts with the COMMAND_DELIMITER

                    if ( input.isEmpty() ) { System.out.println("[ERROR] you need to say something, everyone is waiting ..."); }
                    else
                    {
                        if ( input.charAt(0) == COMMAND_DELIMITER )
                        {
                            command = input.substring(
                                1, 
                                input.indexOf(" ") == -1 ? input.length() : input.indexOf(" ")
                            );

                            if ( command.equals( COMMANDS[0]) )
                            {
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
                                command_ = new ListAllClientsNamesOperation( sender );
                                command_.execute();
                            }
                            else if ( command.equals( COMMANDS[3]) )
                            {
                                command_ = new ExitChatApplicationOperation( sender) ;
                                command_.execute();
                                
                                System.out.println("[CLIENT] Killing main process. Hasta La Vista Amigo!");
                                System.exit( 0 );
                            }
                            else if ( command.equals(COMMANDS[4]) )
                            {
                                command_ = new DMUserOperation(sender, input);
                                command_.execute();
                            }
                            else if ( command.equals(COMMANDS[5]) )
                            {
                                command_ = new ShowAllCommandsOperation(sender, input);
                                command_.execute();
                            }
                            else 
                            {
				    String meantCmd = lev ( input, COMMANDS );
				    System.out.println( 
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

    private Packet encryptMsg ( String input )
    {

	    IvParameterSpec iv = generateIv ();
	    String msg         = String.format ( "%s > %s", name, input );
	    String digest      = encrypt  ( msg, sk, iv );
	    byte[] send        = paddWithIv ( iv, digest ); // [iv, msg] ( padd operation )
	    Packet packet      = new Packet(send, PACKET_TYPE.BROADCAST);

	    return packet;
    }

    private void showNoSKWarning( )
    { System.out.println( "[WARNING] You don't have a Secret key and this will result in unencrypted messages. You must wait for someone to join the room." ); }

    // handling the events.
    private void handleResponseEvent ( Packet packet ) { System.out.println( packet.getMsg() )                  ;}
    private void handleInvalidUsernameEvent ( Packet packet ) {

	    handleResponseEvent( packet );
	    this.name = promptUsername() ;
	    sendNamePacket ()            ;

    }
    private void handleDisconnectEvent ( Packet packet ) { System.out.println( packet.getMsg() )                ;}
    private void handleBroadcastEvent  ( Packet packet ) {

	    byte[] bytes= packet.getMsg_()                             ;
	    byte[] iv_  = unpaddIv ( bytes )                           ; 
            byte[] msg  = unpaddIvAndGetMsg ( bytes )                  ;

	    IvParameterSpec iv = new IvParameterSpec ( iv_ ) ;
	    String dec         = decrypt ( bytes2Str ( msg ), this.sk, iv );  
	    System.out.println( dec );
    }

    private void handleDefaultUnknownPacketEvent ( Packet packet )
    { System.out.println( "[LOGGING] received unknown packet : " + packet ); }

    private void handleKeyEvent ( Packet packet ) {
	    System.out.println( "[LOGGING] handling the KEY event" );
	    setKey ( packet );
    }

    private void handleConnectEvent( Packet packet ) { 

	    String uncMsg = packet.getMsg();	    
	    System.out.println( uncMsg );

    }
    private void handleOKEvent ( Packet packet ) { System.out.println( "[SERVER] 200 OK!" ); }

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

		case OK:
		    handleOKEvent( packet );
		    break;
                
                case DISCONNECT:
		    handleDisconnectEvent ( packet );
                    break;

		case CONNECT:
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


    private BigInteger lastSeenKey = new BigInteger ( "0" );

    public void setKey ( Packet packet )
    {

	    BigInteger recKey = new BigInteger ( packet.getMsg_() ); // OTHER'S KEY
	    BigInteger ourKey = this.diffieKey ; // OUR KEY

	    byte[] bytes           = cropBigIntBy ( 16, recKey.modPow ( ourKey, P ));

	    this.sk = new SecretKeySpec ( bytes , ALGORITHM );
	    System.out.println( "[LOGGING] generating new secret key : " + this.sk );

	    // then send our key to the server
	    if ( ! lastSeenKey.equals ( recKey ) ) { sendKey (); } 
	    else { System.out.println( "[LOGGING] this key is already in my book" ); }

	    
	    lastSeenKey = recKey;
    }

    public void genKey ( )
    {
	int lengthKey  = 2048;
	this.diffieKey = gKey ( lengthKey );
        this.MIXED_KEY = this.G.modPow ( this.diffieKey, P );
	int length     = this.MIXED_KEY.bitLength();

	System.out.println( "[LOGGING] generated diffie key with value : " + this.diffieKey + " and length " + lengthKey );
	System.out.println( "[LOGGING] generated the mixed key with value : " + this.MIXED_KEY + " and length " + length);
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
