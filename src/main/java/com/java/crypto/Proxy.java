package com.java.crypto;

import java.util.ArrayList;

import com.java.crypto.ClientHandler;
import com.java.crypto.Entity       ;

public final class Proxy
{

	// add more flexibility using the interface
	private static ArrayList<Entity> clients = new ArrayList<>();
	private ClientHandler     chInst ;

	public Proxy() {}
	public Proxy( ClientHandler ch ) { this.chInst = ch; }

	public static ArrayList<Entity> getClients ( ) { return clients; }

	public void add ( Entity client ) {
		// sending the missing packets
		clients.add ( client ); 
		// chInst.sendMissingPackets();
	}

	public void remove ( Entity client ) { clients.remove ( client ); }
	public int  indexOf ( Entity client ) { return clients.indexOf ( client ); }
	public void remove ( int i )  { clients.remove( i ); }

}
