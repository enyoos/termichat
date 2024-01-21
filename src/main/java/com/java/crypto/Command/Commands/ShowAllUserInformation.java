package com.java.crypto.Command.Commands;

import java.util.ArrayList;
import java.math.BigInteger;

import javax.crypto.SecretKey;

import com.java.crypto.Client;
import com.java.crypto.Command.Action;
import com.java.crypto.Command.Sender;
import com.java.crypto.Packet.PACKET_TYPE;
import com.java.crypto.Packet.Packet;

public class ShowAllUserInformation implements Action
{
    private static final String DELIMITER = "------------------";
    private static final String nLine     = "\n";

    private Sender sender;
    private Client client;
    public ShowAllUserInformation() {}
    public ShowAllUserInformation( Sender sender ) { this ( sender, null ); }
    public ShowAllUserInformation( Sender sender, Client client ){
        this.sender = sender;
        this.client = client;
    }

    @Override
    public void execute() {

        StringBuilder sb = new StringBuilder  ();
        String name      = client.getName     ();
        BigInteger tempSk= client.getTempSk   ();
        SecretKey  sk    = client.getSk       ();
        BigInteger diffie= client.getDiffieKey();
        BigInteger mk    = client.getMK       ();

        sb.append ( DELIMITER );
        sb.append ( nLine );

        if ( name != null ) sb.append ( "name : " + name );

        sb.append ( nLine );

        if ( tempSk != null ) sb.append ( "temporary sk : " + tempSk );

        sb.append ( nLine );

        if ( sk != null ) sb.append ( "hash( sk ) : " + sk.hashCode() );

        sb.append ( nLine );

        if ( diffie != null ) sb.append ( "diffie : " + diffie );

        sb.append ( nLine );

        if ( mk != null ) sb.append ( "mixed key : " + mk );

        sb.append ( nLine );
        sb.append ( DELIMITER );

        String out = sb.toString();
        System.out.println( out );

    }

}