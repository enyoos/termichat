package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Command.Commands.*;
import static com.java.crypto.Encryption.Utils.*;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

    public static String[] COMMANDS = {
        "ping", "server_info", "list", "exit","dm","help"
    };

    // ------
    // for now let's make the P and G values client-side
    // convert them to the adequate class when doing the rsa encryption
    // ------

    private SecretKey sk                       ;
    private BigInteger diffieKey               ;

    private static char COMMAND_DELIMITER = '/';
    private static final String DEFAULT_HOST = "localhost";
    private static final String ALGORITHM = "AES";
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random  random  = new Random ();
    private static final int MAX_BYTE_RECV = 1024;

    private Socket socket;
    private String name;

    // channel where you can read from
    private InputStream is;
    // chanel where you can write from
    private OutputStream os;

    private static final BigInteger G = new BigInteger ( "482374" );
    private static final BigInteger P = new BigInteger ( "423897" );

    // this is essential to the command pattern
    private Sender sender;

    public Client(){}
    public Client ( String name, int port )
    {
        try {
            this.name=name;
            this.socket = new Socket(DEFAULT_HOST, port);
            this.socket.setTcpNoDelay(true);

            is = socket.getInputStream();
            os = socket.getOutputStream();

            sender = new Sender(os);
        
            // generate the pk only !
            // the sk will be computed thanks to the diffie hellman exchange.
	    this.genKey();

            mainLoop();
        }catch( IOException err ) {
            exitAppOnServerShutDown();
        }
    }

    private void sendKeyPKMixed ( )
    {
	    // you broadcast your key thanks
	    // bfore even sending your name
	    System.out.println("[CLIENT] sending the mixed key to the server.");
	    // BigInteger mixKey_   = mixKey(this.pk, G, P);
	    // byte[] bytes        = mixKey_.toByteArray();
	    
	    byte[] bytes = this.G.modPow ( this.diffieKey, P ).toByteArray() ;

	    Packet firstDefaultPacket = new Packet( bytes , PACKET_TYPE.KEY);

	    // sending the content of the packet 
	    sendPacket ( firstDefaultPacket );
    }

    private void sendNamePacket ( )
    {

        System.out.println("[CLIENT] sending the name to the server and other parties.");
        Packet SecondDefaultPacket = new Packet(name, PACKET_TYPE.CONNECT);
        sendPacket(SecondDefaultPacket);
    }


    private void sleepClient ( long st )
    {
	try{
		long sleepTime = 200;
		Thread.sleep ( sleepTime );
	}catch ( InterruptedException e ) { System.out.println( "unpatient" ); }
    }

    // this the main loop
    // think of it like the main Game Loop
    public void mainLoop()
    {

        // firstly, we notify the server of our name,
        // using the CONNECT packet
        sendKeyPKMixed();


	// how about we wait a bit ? 
	
	// for some reason the server can't read the key
	// only if I sleep here.
	// for like 2 s before sending the name.
	sleepClient ( 2000 );

	// then when the key exchange is finished, send the username.
        sendNamePacket();

        inputTask();

        while( this.socket.isConnected() )
        {
            receivePacket();
        }
    }

    // asks, while the program is running the input of the user
    public void inputTask ( )
    {
        new Thread( new Runnable() {
            @Override
            public void run()
            {
                String command = "";
                Action command_ ;
                boolean running = true;
                String msg      = name + " >";
                String input    = "";
                Packet packet   ;

                while ( running )
                {
                    System.out.print(msg); 
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
                                
                                System.out.println("[CLIENT] Existing the application ...");
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
                            packet = new Packet(input, PACKET_TYPE.SEND);
                            sendPacket ( packet );
                        }
                    }
                }
            }
        }).start();
    }

    // but is this a blocking line ?, well yeah...
    // we should maybe have a thread for writing and another thread for inputing
    private void receivePacket( )
    {
        Packet packet;

        try {

            byte[] allocateByteMsgArray = new byte[MAX_BYTE_RECV];
            is.read(allocateByteMsgArray);

            // do nothing if the byte read is zero
            packet = new Packet( allocateByteMsgArray );

            switch (packet.getType()) {
                case RESPONSE:
                    // RESPONSE enum means it's a broacast
                    System.out.println( packet.getMsg() );
                    break;
                
                case DISCONNECT:
                    System.out.println( packet.getMsg() );
                    break;

                case KEY: 
		    // we receive the key from the other user
                    // which in theory should be the mixed key
                    // we should take that mixed key and put modPow it.
                    setKey( packet );

		    // for those who just joined the chat ( here, we assume
		    // there's only two clients.
		    // sendKeyPKMixed ();

                    break;

                default:
                    break;
            }

        }catch ( IOException e ){ 
            System.err.println("[CLIENT] tunnel in the content from the server");    
        }
    }

    public void setKey ( Packet packet )
    {

	    BigInteger recKey = new BigInteger ( packet.getMsg_() ); // OTHER'S KEY
	    BigInteger ourKey = diffieKey ; // OUR KEY

	    byte[] bytes           = recKey.modPow ( ourKey, P ).toByteArray();

	    this.sk = new SecretKeySpec ( bytes , ALGORITHM );


	    BigInteger skValueTemp = new BigInteger ( bytes );
	    System.out.println( "[LOGGING] generating new secret key : " + skValueTemp );

    }

    public void genKey ( )
    {
	this.diffieKey = gKey ( 16 );
	System.out.println( "[LOGGING] generated diffie key with value : " + this.diffieKey );
    }


    // the client can : SEND_PACKETS ( MSG to the server );
    private void sendPacket ( Packet packet )
    {
        try {
            // the flush occurs when a new line operator is entered
            // we simply call the flush method;
            // wiich propably less efficient
            // TODO : change.
	    System.out.println( "the array to send : " + Arrays.toString ( packet.output()));

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
