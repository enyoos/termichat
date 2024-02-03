package com.java.crypto.CustomStruct;

import java.lang.*        ;
import java.util.ArrayList;
import java.util.HashMap  ;

public class Pool implements Runnable
{

    private static final long ELAPSED = 60_000; // in ms, makes 60 sec -> 1 min

    public static void main ( String... args )
    {
        Pool time = new Pool();
        Thread t1 = new Thread ( time );

        t1.start();

        // 1 minutes
        ArrayList<String> banned = new ArrayList<>();
        Integer duration         = new Integer (1);
        String  client1          = "A really inappropriate name";
        
        banned.add( client1 ); 

        time.lookup.put ( duration, banned );
    }

    // private current_time = 0;
    // private time_out     = 0;
    // entry < time : [client1, client2, ...] >
    public HashMap<Integer, ArrayList<String>> lookup;
    public int timer_count = 0;

    public Pool() { this.lookup = new HashMap<>(); }

    // shall we do ip ban ?
    // to prevent ban evasion ?
    // perhaps some hardware limitation ( MAC ADDR )
    public void add ( String ip, Integer time )
    {
        ArrayList<String> result = this.lookup.get ( time );

        if ( result != null ) result.add ( ip ); // cool ?
        else             
        { 
            Integer key             = offset_time( time );
            ArrayList<String> value = new ArrayList<>();

            value.add ( ip );
            this.lookup.put ( key, value );
        }
    }

    private int offset_time ( int time ) { return time + this.timer_count; }

    @Override
    public void run ()
    {
        boolean running = true;

        while ( running )
        {
            if ( lookup.size() != 0 )
            {

                try{
                    Thread.sleep ( ELAPSED );
                } catch ( InterruptedException e ) { System.out.println("[ERROR] Sleep failed"); }

                // this could overflow ( there's no permaban, so it is less likely to happen)
                timer_count += 1; 
                boolean isEvaded = evade_banned( timer_count );
           }
            else timer_count = 0;
        }
    }

    @Override
    public String toString() { return this.lookup.toString(); }


    private boolean evade_banned ( int time_code ) 
    {
        Integer key = new Integer ( time_code );
        ArrayList<String> nullable = this.lookup.get ( key );

        if ( nullable != null ) { this.lookup.remove ( key ); return true ; } 
        else                                                  return false;
    }

}