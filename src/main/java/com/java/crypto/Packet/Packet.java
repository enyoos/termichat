package com.java.crypto.Packet;

import java.util.Arrays;

import com.java.crypto.Encryption.Utils;

public class Packet {
    private byte[] msg;
    private PACKET_TYPE type;    

    public Packet (){}
    public Packet( byte[] msg , PACKET_TYPE type )
    {
        this.msg = msg;
        this.type = type;
    }


    public static void main ( String... args ) { }

    public Packet ( String msg, PACKET_TYPE type )
    {
        this.msg = msg.getBytes();
        this.type = type;
    }

    public Packet ( String msg ) { this( msg.getBytes(), null); }
    public Packet ( PACKET_TYPE pt ) { this ( new byte[]{}, pt );}

    // taking the output of hte packet class
    public Packet ( byte[] bytes )
    {
        // cleaning the bytes array
        bytes = Utils.cleanByteArray(bytes);

        // transform the byte array to readable ascii char
        // String stringyfiedBytes = Utils.bytes2String(bytes);
        byte[] msg = Utils.unPaddByOneArr(bytes);
        this.msg = msg;

        // the last letter is the PACKET_TYPE
        // sometimes the byte array is prefixed with 0000
	
	int packetType = bytes[bytes.length-1];

        switch ( packetType ) {

            case -1:
                type = PACKET_TYPE.DISCONNECT;
                break;

            case 1:
                type = PACKET_TYPE.CONNECT;
                break;

            case -2:
                type = PACKET_TYPE.BROADCAST; 
                break;

            case -4:
                type = PACKET_TYPE.PRIVATE;
                break;

            case 3:
                type = PACKET_TYPE.RESPONSE;
                break;

            case 4:
                type = PACKET_TYPE.KEY;
                break;
		
	    case 5:
		type = PACKET_TYPE.REPEAT;
	        break;

	    case 6:
		type = PACKET_TYPE.OK;
		break;

	    case 7:
		type = PACKET_TYPE.CACHE;
		break;
            default:
                break;
        }
    }

    // output the final string ( to be sent to the server ), but in bytes
    public byte[] output( ) { 
        byte[] ret = Utils.paddByOneArr(this.msg);
        ret[ret.length-1] = this.type.getValue();
        return ret;
    }


    // GETTERS
    public byte[] getMsg_(){ return this.msg;}
    public String getMsg() { return Utils.bytes2Str(msg);}
    public PACKET_TYPE getType() { return type;}

    // SETTERS
    public void setMsg ( String msg ) { this.msg = msg.getBytes();}
    public void setMsg_ ( byte[] arr ) { this.msg = arr;}
    public void setType ( PACKET_TYPE packetType ) { type = packetType; }

    // OVERRRIDES
    @Override
    public String toString( ) {
	    return "{length : " + this.msg.length + ", type: " + type.getValue() + "}";
    }
}
