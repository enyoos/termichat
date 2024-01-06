package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Random;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public final class Utils {

    private static final String DEFAULT_ALGO = "RSA";
    private static final int BIT_SIZE         = 2048;
    private static final Random random        = new Random();

    public static BigInteger generateBigPrime( )
    { return BigInteger.probablePrime ( BIT_SIZE, random ); }

    public static int getCorrectType ( byte[] array )
    {
        if ( array[array.length-1] != 0 ) return  ( int ) array[array.length-1];
        else return getCorrectType(unPaddByOneArr(array));
    }

    // removes all teh leading 0 of a string.
    // think of it like a trim operation    .
    public static byte[] cleanByteArray ( byte[] array )
    {
        if ( array[array.length-1] != 0 ) return array;
        else return cleanByteArray(unPaddByOneArr(array));
    }

   public static KeyPair   gKeyPair ( ) throws Exception{ 
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair pair = generator.generateKeyPair();
		return pair;
}

public static BigInteger gSK ( BigInteger pk, BigInteger mK, BigInteger P ){ return mK.modPow ( pk, P ); }

    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("null");
        System.out.println(sb.toString());

       String msg = "some msg";
       
       KeyPair pair = gKeyPair();
       PrivateKey sk = pair.getPrivate();
       PublicKey  pk = pair.getPublic() ;
       
       Cipher encryptCipher = Cipher.getInstance("RSA");
       encryptCipher.init(Cipher.ENCRYPT_MODE, pk);

       // testing if hte cleanbyte func is workign
       byte[] arr = {25,33,5,6,0,0,0,0};
       byte[] out = cleanByteArray(arr);

       System.out.println(Arrays.toString(out));

       // // transforming our msg to bytes
       byte[] secretMessageBytes = msg.getBytes(StandardCharsets.UTF_8);

       byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
       
       // // for decryption
       Cipher decryptCipher = Cipher.getInstance("RSA");
       decryptCipher.init(Cipher.DECRYPT_MODE, sk);
       byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
       String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

       System.out.println(decryptedMessage);

    }

    // public static PublicKey fromBigInteger2PK ( BigInteger n )
    // {

    // }


    // public String encrypt( String msg, BigInteger pk)
    // {
    //     Cipher encryptCipher = Cipher.getInstance("RSA"); 
    //     encryptCipher.init(Cipher.ENCRYPT_MODE, pk);
    // }

    // return the most similar string
    public static String lev( String cmd , String[] cmds )
    {
        int[] distances = new int[cmds.length];
        for ( int i = 0 ; i < cmds.length; i ++ ) { distances[i] = (_lev ( cmd, cmds[i]) ); }
        
    }

    // watch the coding with john stuff
    public static void quicksort( int[] array )
    {

    }

    private static int _lev( String cmd, String cmd2 )
    {
        if ( cmd.isEmpty() ) return cmd2.length();
        else if ( cmd2.isEmpty() ) return cmd.length();
        else if ( cmd.charAt(0) == cmd2.charAt(0) )
        { return _lev ( cmd.substring(1), cmd2.substring(1) ); }
        else 
        {
            // i feel it testing all the possible outcomes 
            // and choosing the least big
            return 1 + min (
                _lev ( cmd.substring(1), cmd2),
                _lev ( cmd, cmd2.substring(1)),
                _lev ( cmd.substring(1), cmd2.substring(1)),
            );
        }
    }

    private static int min ( int... values )
    {
        int min = Integer.MAX_VALUE;
        for ( int value : values ) {
            if ( value < min ) { min = value; }
        }

        return min;
    }

    public static String bytes2Str ( byte[] bytes ) { return new String ( bytes, StandardCharsets.UTF_8 ); }
    public static byte[] paddByOneArr ( byte[] bytes )
    {
        int l = bytes.length + 1;
        byte[] ret = new byte[l];
        for ( int i = 0 ; i < bytes.length ; i++ ) { ret[i] = bytes[i]; }
        return ret;
    }

    // like a substring func
    public static byte[] unPaddByOneArr( byte[] bytes )
    {
        int l = bytes.length - 1;
        if ( l < 0 ) return new byte[]{};
        byte[] ret = new byte[l];
        for ( int i = 0; i < l; i ++ ) { ret[i] = bytes[i]; }
        return ret;
    }

	public static BigInteger mixKey ( BigInteger key, BigInteger G, BigInteger P ) { return key.modPow ( G, P ); }

    // with the pk
    // public static String encryptMessageWithKey ( String msg, SecretKey key, IvParameterSpec spec ) throws Exception
    // {
    //     String input = msg;
    //     // a secret key is just a long number
    //     return encrypt(DEFAULT_ALGO, input, key, spec);

    // }

    public static PrivateKey fromByteSK ( byte[] bytes ) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        PrivateKey sk = 
            (PrivateKey) KeyFactory.getInstance(DEFAULT_ALGO).generatePublic(new X509EncodedKeySpec(bytes));

        return sk;
    }
    
    public static PublicKey fromBytePK ( byte[] bytes ) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		return KeyFactory.getInstance ( DEFAULT_ALGO).generatePublic( new X509EncodedKeySpec(bytes));
	}
    
    // but how can we apply the diffie hellman protocol with a lot of ppl ?
    // idea : we can use the same key ( pk, and sk , for everyone ).
    // but this is very unefficient ?
    // let's say we have two client.



    // client              server              client
    
    // here we have diffie classic exchange
    // so let's say a new client joins the chat.



    // client             sever                client
    //           client
    

    // this new client generate a new key.

}
