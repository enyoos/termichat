package com.java.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Command.Commands.ExitChatApplicationOperation;
import com.java.crypto.Command.Commands.ListAllClientsNamesOperation;
import com.java.crypto.Command.Commands.PingServerOperation;
import com.java.crypto.Command.Commands.ShowServerInfoOperation;
import com.java.crypto.Encryption.Utils;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class Client {

    private static String[] COMMANDS = {
        "ping", "server_info", "list", "exit"
    };

    // ------
    private PublicKey pk;
    private PrivateKey sk;
    // private SecretKey sk;
    // private SecretKey pk;
    // ------

    private static char COMMAND_DELIMITER = '/';
    private static String DEFAULT_HOST = "localhost";
    private static final Scanner scanner = new Scanner(System.in);
    private Socket socket;
    private String name;
    private int msgLength;

    // channel where you can read from
    private InputStream is;
    // chanel where you can write from
    private OutputStream os;

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
            this.pk = Utils.gPK();    

            mainLoop();
        }catch( IOException | NoSuchAlgorithmException err ) {
            exitAppOnServerShutDown();
        }
    }

    // this the main loop
    // think of it like the main Game Loop
    public void mainLoop()
    {

        // bfore even sending your name
        // you broadcast your key thanks


        // firstly, we notify the server of our name,
        // using the CONNECT packet
        Packet firstDefaultPacket = new Packet(name, PACKET_TYPE.CONNECT);
        sendPacketLength(firstDefaultPacket);
        sendPacket(firstDefaultPacket);

        inputTask();

        boolean running = true;
        while( running )
        {
            receivePacketLength();
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
                    if ( input.charAt(0) == COMMAND_DELIMITER )
                    {
                        command = input.substring(1);
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
                        }
                        else 
                        {
                            // TODO implement the lev algo
                            // String diff
                            System.out.println("this command doesn't work !");
                        }
                    }

                    if ( !input.isEmpty() )
                    {
                        // by default, we broadcast each msg
                        packet = new Packet(input, PACKET_TYPE.SEND);
                        sendPacketLength(packet);
                        sendPacket ( packet );
                    }
                }
            }
        }).start();
    }

    private void receivePacketLength ()
    {
        try {
            this.msgLength = is.read();
        }        
        catch ( IOException e ){ 
            System.err.println("Couldn't read the msg from the server");    
        }
    }

    // but is this a blocking line ?, well yeah...
    // we should maybe have a thread for writing and another thread for inputing
    private void receivePacket( )
    {
        Packet packet;

        try {
            byte[] allocateByteMsgArray = new byte[this.msgLength];
            is.read(allocateByteMsgArray);
            packet  = new Packet( allocateByteMsgArray );

            switch (packet.getType()) {
                case RESPONSE:
                    // RESPONSE enum means it's a broacast
                    System.out.println( packet.getMsg() );
                    break;
                
                case DISCONNECT:
                    System.out.println( packet.getMsg() );
                    break;

                default:
                    break;
            }

        }catch ( IOException e ){ 
            System.err.println("Couldn't read the msg from the server");    
        }
    }

    // b4 sending any packet, we send it's length through the socket
    private void sendPacketLength ( Packet packet ){
        try{

            // get the length of the byte array
            int lengthByteMsgToSend = packet.output().length;

            // the msgLength shall not exceed 255 ( for now let's not check for it )
            os.write(lengthByteMsgToSend);
            os.flush();
        }catch ( IOException e ) { 
            exitAppOnServerShutDown();
        } 
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
