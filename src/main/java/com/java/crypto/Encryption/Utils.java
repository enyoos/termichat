package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.util.Random;


public final class Utils {

    private static final int BIT_SIZE = 1024; // for more secure connection use the bit_size 2048
    private static final Random random = new Random();

    // we're going to use the bigInteger class
    // in order to get prime numbers
    // we shall choose a small bitSize so that futur operation don't overflow.
    public static BigInteger generateBigPrime ( int bitSize ) { return BigInteger.probablePrime(bitSize, random); }

    public static BigInteger combineKeyWithPAndG( BigInteger key, BigInteger P, BigInteger G )
    {
        BigInteger xp = G.pow(P.intValue());
        BigInteger mod = xp.mod(P);
        return mod;
    }


    public static BigInteger getSecretKey( BigInteger key1, BigInteger key2, BigInteger P)
    {
        BigInteger xp = key2.pow(key1.intValue());
        BigInteger mod = xp.mod(P);
        return mod;
    }

    // simulates the diffie hellman exchange.
    // read : https://sites.ualberta.ca/~jhoover/ConcreteComputing/section/diffie_hellman.htm
    public static void diffie( int bitSize )
    {

        BigInteger alicePK = generateBigPrime(bitSize);
        BigInteger bobPK   = generateBigPrime(bitSize);

        // generated server-side
        BigInteger P = generateBigPrime(bitSize) /* new BigInteger("23") */;
        BigInteger G = generateBigPrime(bitSize) /* new BigInteger("5")*/;

        BigInteger mixAlicePK = G.modPow(alicePK, P);
        BigInteger mixBobPK   = G.modPow(bobPK, P);

        BigInteger aliceSK    = mixBobPK.modPow(alicePK, P);
        BigInteger bobSK      = mixAlicePK.modPow(bobPK, P);

        System.out.println("sk alice : " + aliceSK);;
        System.out.println("sk bob: " +    bobSK);;
    }

    public static void diffie ( )
    {
        diffie(BIT_SIZE);
    }

    public static void main(String[] args) {
        diffie();
    }

}
