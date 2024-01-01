package com.java.crypto.Packet;

public class Packet {
    private String msg;
    private PACKET_TYPE type;    

    public Packet (){}
    public Packet( String msg , PACKET_TYPE type )
    {
        this.msg = msg;
        this.type = type;
    }

    public Packet ( String msg ) { this( msg, null ); }
    public Packet ( PACKET_TYPE pt ) { this ( "", pt );}

    public Packet ( byte[] bytes )
    {
        // transform the byte array to readable ascii char
        String stringyfiedBytes =  bytes.toString();

        // the last letter is the PACKET_TYPE
        int length = stringyfiedBytes.length();
        int packeType = stringyfiedBytes.charAt(length - 1);

        switch ( packeType ) {

            case -1:
                type = PACKET_TYPE.DISCONNECT;
                break;

            case 1:
                type = PACKET_TYPE.CONNECT;
                break;

            case 2:
                type = PACKET_TYPE.SEND; 
                break;

            case 0:
                type = PACKET_TYPE.PRIVATE;
                break;
 
            default:
                break;
        }

        // the msg is just a slice the `stringyfiedBytes`
        msg = stringyfiedBytes.substring(0, length);
    }

    // output the final string ( to be sent to the server ), but in bytes
    public byte[] output( ) { return ( msg + type.getValue() ).getBytes(); }


    // GETTERS
    public String getMsg () { return msg;}
    public PACKET_TYPE getType() { return type;}

    // SETTERS
    public void setMsg ( String msg ) { this.msg = msg;}
    public void setType ( PACKET_TYPE pakcetType ) { type = pakcetType; }
}
