package com.java.crypto;

import java.util.ArrayList;

import com.java.crypto.ClientHandler;
import com.java.crypto.Entity       ;

public final class Proxy
{

	// add more flexibility using the interface
	private ArrayList<Entity> clients = new ArrayList<>();
	private ClientHandler     chInst ;

	public Proxy() {}
	public Proxy( ClientHandler ch ) { this.chInst = ch; }

	public ArrayList<Entity> getClients ( ) { return this.clients; }

	public void add ( Entity client ) {
		// sending the missing packets
		// chInst.sendMissingPackets();
		this.clients.add ( client ); 
	}

	public void remove ( Entity client ) { this.clients.remove ( client ); }
	public int  indexOf ( Entity client ) { return this.clients.indexOf ( client ); }
	public void remove ( int i )  { this.clients.remove( i ); }

}
