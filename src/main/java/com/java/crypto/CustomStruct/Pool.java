package com.java.crypto.CustomStruct;

import java.lang.*        ;
import java.util.ArrayList;
import java.util.HashMap  ;

public class Pool implements Runnable
{

    private static final int ELAPSED = 60_000; // in ms, makes 60 sec -> 1 min

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

                timer_count += 1;
                boolean isEvaded = evade_banned( timer_count );

                if ( isEvaded ) System.out.println( "Someone just got out !" );
                else            System.out.println( "nothing happened" )      ;
            }
            else continue;
        }
    }

    private boolean evade_banned ( int time_code ) 
    {
        Integer key = new Integer ( time_code );
        ArrayList<String> nullable = this.lookup.get ( key );

        if ( nullable != null ) { this.lookup.remove ( key ); return true ; } 
        else                                                  return false;
    }

}