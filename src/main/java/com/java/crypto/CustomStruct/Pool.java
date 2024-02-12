package com.java.crypto.CustomStruct;

import java.lang.*;
import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Pool implements Runnable
{

    private static final long ELAPSED = 60_000; // in ms, makes 60 sec -> 1 min

    public static void main ( String... args )
    {
        // we don't want to do that.
        // how about we extends the Thread class of the Pool
        Pool time = new Pool();
        Thread t1 = new Thread ( time );

        t1.start();

        // 1 minutes
        ArrayList<String> banned = new ArrayList<>();
        Integer duration         = new Integer   (2);
        String  client1          = "A really inappropriate name";
        String  client2          = "some other correct name"    ;
        
        banned.add( client1 ); 
        time.lookup.put ( duration, banned );

        // try{
        //     System.out.println( "time up : " + time.timer_count.get() );
        //     Thread.sleep( ELAPSED );
        //     System.out.println( "time up : " + time.timer_count.get() );
        // }catch ( InterruptedException e ) {System.out.println( "interrupt failed");}

    }

    // private current_time = 0;
    // private time_out     = 0;
    // entry < time : [client1, client2, ...] >
    public volatile HashMap<Integer, ArrayList<String>> lookup;
    public volatile AtomicInteger timer_count = new AtomicInteger ( 0 );

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

    // return the time to wait until unbanned
    // TODO : this won't work ( use entry set )
    public int when ( String name )
    {
        Set<Map.Entry<Integer, ArrayList<String>>> entries = this.lookup.entrySet();

        for ( Map.Entry<Integer, ArrayList<String>> entry : entries )
        {
            Integer key = entry.getKey();
            ArrayList<String> value= entry.getValue();

            if ( value.contains( name ) ) { return key.intValue() - this.timer_count.get(); }
            else                            continue;
        }

        return 0;
    }

    public boolean isBanned ( String name )
    {
        // get all the keys value pairs.
        // and check if the client is in there.
        
        Collection<ArrayList<String>> values = this.lookup.values();

        for ( ArrayList<String> value : values )
        {
            if ( value.contains( name ) ) return true;
            else                          continue;
        }

        return false;
    }

    private int offset_time ( int time ) {
        int value = this.timer_count.intValue(); 
        int ret   = time + value;
        return ret;
    }

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
                    // this could overflow ( there's no permaban, so it is less likely to happen)
                    this.timer_count.incrementAndGet();
                    boolean isEvaded = evade_banned( this.timer_count.get() );

                } catch ( InterruptedException e ) { System.out.println("[ERROR] Sleep failed"); }

           }
            else this.timer_count.set( 0 );
        }
    }

    @Override
    public String toString() { return this.lookup.toString(); }


    private boolean evade_banned ( int time_code ) 
    {
        Integer key = new Integer ( time_code );
        ArrayList<String> nullable = this.lookup.get ( key );

        if ( nullable != null ) { 

            System.out.println( "[EVADED] removed : " + nullable );

            this.lookup.remove ( key ); 
            return true ; 
        } 
        else                                                  return false;
    }

}