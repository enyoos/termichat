package com.java.crypto.Packet;

// this class is designed to test the Packet class
public class Test {
    public static void main(String[] args) {
        String msg = "hello world";
        PACKET_TYPE pt = PACKET_TYPE.SEND;

        Packet packet = new Packet(msg, pt);
        System.out.println(packet);

        byte[] deconstructionPacket = packet.output();
        Packet brotherPacket = new Packet(deconstructionPacket);
        System.out.println(brotherPacket);
    } 
}
