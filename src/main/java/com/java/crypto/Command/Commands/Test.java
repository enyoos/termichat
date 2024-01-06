package com.java.crypto.Command.Commands;

import java.util.Arrays;

import com.java.crypto.Command.Sender;
import com.java.crypto.Encryption.Utils;

public class Test {
    public static void main(String[] args) {

        Sender sender = new Sender();

        String userT  = "some normal name";
        String msg    = "hello world and my name is ilyas";
        String _dm     = String.format ( "/dm -u %s -m %s", userT, msg );

        DMUserOperation dm = new DMUserOperation(sender, _dm);
        System.out.println(dm);

        System.out.println(userT.length());
        System.out.println(dm.getUserTarget().length());

        System.out.println("---");

        System.out.println(msg.length());
        System.out.println(dm.getContent().length());

        System.out.println(dm.output());        
        System.out.println(Arrays.toString ( Utils.splitAtFirstOccurenceOf(",", dm.output() ) ) );
 
    }
}
