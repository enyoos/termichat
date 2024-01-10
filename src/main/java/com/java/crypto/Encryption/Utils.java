package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.KeyGenerator;
import java.nio.ByteBuffer;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom; 
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public final class Utils {

    private static final String DEFAULT_ALGO = "RSA";
    private static final Random random        = new Random();

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

public static String[] splitAtFirstOccurenceOf ( String t, String content )
{
    // check the first occurence of the comma
    int firstOccurenceOfComma = content.indexOf(t);

    String leftPart = content.substring(0, firstOccurenceOfComma);
    String rightPart= content.substring(firstOccurenceOfComma + 1);

    String[] ret = { leftPart, rightPart };

    return ret;
}

// shoudl output 4x4 matrix
// go and check that ; https://www.comparitech.com/blog/information-security/what-is-aes-encryption/
public static char[][] toCharMatrix( String input )
{
    char[][] charMatrix = new char[4][4];
    char[]   vector     = input.toCharArray();
    int  lvector        = vector.length;
    char[] temp         = new char[4];

    // [ ... ] -> [[.],[.],[.]]
    int j = 0;
    for ( int i = 0; i < lvector; i ++ )
    {
        temp[i / 2] = vector[i];

        if ( (i + 1)  % 4 == 0 ) {
            charMatrix[j] = temp;
            j ++;
            temp = new char[4];
        }
    }    


    return charMatrix;
}


	public static BigInteger gSK ( BigInteger pk, BigInteger mK, BigInteger P ){ return mK.modPow ( pk, P ); }

public static int modPow ( int value, int g, int p ) { return ( (int)  Math.pow (g, value) ) % p; }

    public static void main(String[] args) throws Exception {

	// 10 rounds for 128-bit keys.
	// 12 rounds for 192-bit keys.
	// 14 rounds for 256-bit keys.
    }

public static BigInteger gKey ( int bit ) { return BigInteger.probablePrime ( 16, new Random() ); }

	public static byte[] fromInt ( int value )
	{ return ByteBuffer.allocate( 4 ).putInt ( value ).array(); }

	public static String decrypt(String algorithm, String cipherText, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.DECRYPT_MODE, key, iv);
	    byte[] plainText = cipher.doFinal(Base64.getDecoder()
		.decode(cipherText));
	    return new String(plainText);
	}

    private static final int    KEY_SIZE = 256;
    private static final String ALGO = "AES";
    private static final String ALGO2 = "AES/CBC/PKCS5Padding";

    public static String encrypt(String algorithm, String input, SecretKey key,
	    IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
	    InvalidAlgorithmParameterException, InvalidKeyException,
	    BadPaddingException, IllegalBlockSizeException {
	    
	    Cipher cipher = Cipher.getInstance(algorithm);
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	    byte[] cipherText = cipher.doFinal(input.getBytes());
	    return Base64.getEncoder()
		.encodeToString(cipherText);
	}

    public static IvParameterSpec generateIv() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return new IvParameterSpec(iv);
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
	    Map<Integer, String> map = new HashMap<>();
        int[] distances = new int[cmds.length];
        for ( int i = 0 ; i < cmds.length; i ++ ) { 
		int value = (_lev ( cmd, cmds[i]) );
		distances[i] = value;
		map.put ( value, cmds[i] );
	}

	quicksort ( distances );
	int target = distances[0];

	return map.get ( target );
    }

    // TODO : IMPLEMENT THE QUICKSORT ALGORITHM
    public static void quicksort( int[] array )
    {
	quickSort ( array, 0, array.length-1 );
    }

	public static void quickSort(int arr[], int begin, int end) {
	    if (begin < end) {
		int partitionIndex = partition(arr, begin, end);

		quickSort(arr, begin, partitionIndex-1);
		quickSort(arr, partitionIndex+1, end);
	    }
	}

	private static int partition(int arr[], int begin, int end) {
	    int pivot = arr[end];
	    int i = (begin-1);

	    for (int j = begin; j < end; j++) {
		if (arr[j] <= pivot) {
		    i++;

		    int swapTemp = arr[i];
		    arr[i] = arr[j];
		    arr[j] = swapTemp;
		}
	    }

	    int swapTemp = arr[i+1];
	    arr[i+1] = arr[end];
	    arr[end] = swapTemp;

	    return i+1;
	}

	private static int size = 1024;
	private static long[] cache = new long[size];
	static {
		cache[1] = 1;
		cache[2] = 1;
	}

// since for each fib we need to compute the other fib
	private static long nthFib ( int nth )
	{
		if ( cache[nth] != 0 ) return cache[nth];

		if ( nth <= 0 ) return 0;

		long value = nthFib ( nth - 1 );
		long othervalue = nthFib ( nth - 2 );

		cache[nth] = value + othervalue;

		return value + othervalue;
	}

    private static int _lev( String cmd, String cmd2 )
    {
        if ( cmd.isEmpty() ) return cmd2.length();
        else if ( cmd2.isEmpty() ) return cmd.length();
        else if ( cmd.charAt(0) == cmd2.charAt(0) )
		// we're computing that 2 times ?
        { return _lev ( cmd.substring(1), cmd2.substring(1) ); }
        else 
        {
            // i feel it testing all the possible outcomes 
            // and choosing the least big
            return 1 + min (
                _lev ( cmd.substring(1), cmd2),
                _lev ( cmd, cmd2.substring(1)),
                _lev ( cmd.substring(1), cmd2.substring(1))
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
