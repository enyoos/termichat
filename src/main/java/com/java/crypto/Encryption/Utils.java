package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.nio.charset.Charset;
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

	public static boolean compare( byte[] bytes1, byte[] bytes2 )
	{
		int length1 = bytes.length;
		int length2 = bytes.length;

		if ( length1 != length2 ) return false;
		
		for ( byte b : bytes1 )
		{
			for ( byte b_ : bytes2 )
			{
				if ( b == b_ ) continue    ;
				else           return false;
			}
		}
		
		return true;

	}

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

    public static boolean hasSpecialChar_ ( String content )
    {

	    for ( char ch : content.toCharArray() )
	    {

		    if ( (ch >= 32 && ch <= 47) || ( ch >= 58 && ch <= 64 ) || ( ch >= 91 && ch <= 96 )
				    || ( ch >= 123 && ch <= 127 ) ) { return true; }
		    else continue;
	    }

	    return false;
    }
 
 private static final int MIN_SIZE = 4;

 public static boolean correctNameNomenclature ( String name )
    {
	    boolean hasSpecialChar = hasSpecialChar_ ( name );
	    boolean hasNonAsciiChar= !Charset.forName("US-ASCII").newEncoder().canEncode( name ); 
	    boolean hasCorrectSize = name.length () >= MIN_SIZE;

	    return ( ! hasSpecialChar && ! hasNonAsciiChar && hasCorrectSize ) ;
    }


    public static void main(String[] args) throws Exception {

	// 10 rounds for 128-bit keys.
	// 12 rounds for 192-bit keys.
	// 14 rounds for 256-bit keys.
		byte[] arr  = {55,55,55,65};
		byte[] arr2  = {55,55,55,65};

		System.out.println( compare( arr, arr2 ) );

	    // BigInteger n = new BigInteger ( "27425479482750425052411308473799907881733956615995983604272760532752549886132188364177979682577637933682739157874614381139479324114205608822767804668030183777117212364720120437596396536521575673359676545490396298516702713135935003205818850321505845292881232654195070265215164909210247813280087445648457823509850033545776068495763859886627235335758321902561009282329416584312780131074195360947977983110793747142184327085584127714893377326384542209243309186960601920489936193440448428179859642038022647156899498217504893326117005948316948819913925241178630711303972544238391627138895162174451434461344485209534941428281" );

	    // BigInteger n2= new BigInteger ( bytes );
	    // System.out.println( "result > " + n2 );	    


		// byte[] arr2 = trimArrayByOne( arr );

		// System.out.println( Arrays.toString (arr2) );
    }


// we shall padd the iv with each each msg.
	public static byte[] paddWithIv ( IvParameterSpec iv, String msg )
	{
		byte[] bytesMsg = msg.getBytes();
		byte[] ivBytes  = iv.getIV()    ;

		// check for the first 
		byte[] result = concatArray ( ivBytes, bytesMsg );

		return result;
	}

	// returns the iv from some arbitrary array
	// we know that the iv is 16 bytes long.
	// return : [b1 ( iv dict ), b2 ( the unpadded array )]
	// so read the first 16 byte

	public static byte[] unpaddIv ( byte[] bytes )
	{

		byte[] ret = new byte[16];
		for ( int i = 0; i < 16; i ++ ) { ret[i] = bytes[i]; }
		return ret;
	}

	public static byte[] unpaddIvAndGetMsg ( byte[] bytes )
	{

		int msgArrayLength = bytes.length - 16;
		byte[] enc = new byte[msgArrayLength];

		for ( int i = 16, j = 0; i < bytes.length ; i ++ )
		{
			enc[j] = bytes[i];
			j++;
		}
		
		return enc;
	}


	public static byte[] concatArray ( byte[] b1, byte[] b2 ) 
	{
		int length = b1.length + b2.length;
		byte[] ret = new byte[length];

		for ( int i = 0, j = 0; i < length ; i ++ )
		{
			if ( i < b1.length )
			{ ret[i] = b1[i]; }

			else 
			{ ret[i] = b2[j]; j++; }
		}


		return ret;
	}
	

public static BigInteger gKey ( int bit ) { return BigInteger.probablePrime ( bit, new Random() ); }

	public static byte[] fromInt ( int value )
	{ return ByteBuffer.allocate( 4 ).putInt ( value ).array(); }

	public static String decrypt( String cipherText, SecretKey key,
	    IvParameterSpec iv) {
	    
		try{
		    Cipher cipher = Cipher.getInstance(ALGO2);
		    cipher.init(Cipher.DECRYPT_MODE, key, iv);
		    byte[] plainText = cipher.doFinal(Base64.getDecoder()
			.decode(cipherText));
		    return new String ( plainText );
		}catch ( NoSuchPaddingException | NoSuchAlgorithmException |
	    InvalidAlgorithmParameterException | InvalidKeyException |
	    BadPaddingException | IllegalBlockSizeException  e ) { System.out.println( "[ERROR] couldn't decrypt the msg" ); }

		return null;
	}

public static byte[] cropBigIntBy ( int bit, BigInteger N )
{
	// expect for this use case ( the value 16 )
	byte[] ret = new byte[bit]  ;
	int bitLen = N.bitLength()  ;
	byte[] arrN= N.toByteArray();

	if ( bitLen == bit ) return arrN;
	else { for ( int i = 0 ; i < bit; i ++ ) { ret[i] = arrN[i]; } }

	return ret;
}

    private static final int    KEY_SIZE = 256;
    private static final String ALGO     = "AES";
    private static final String ALGO2    = "AES/CBC/PKCS5Padding";

    public static String encrypt( String input, SecretKey key,
	    IvParameterSpec iv)
        {

	   try{
		    Cipher cipher = Cipher.getInstance(ALGO2);
		    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		    byte[] cipherText = cipher.doFinal(input.getBytes());
		    return Base64.getEncoder()
			.encodeToString(cipherText);
	   }
	   catch ( NoSuchPaddingException | NoSuchAlgorithmException |
	    InvalidAlgorithmParameterException | InvalidKeyException |
	    BadPaddingException | IllegalBlockSizeException e ) { System.err.println("[ERROR] couldn't encrypt the msg" ); }

	   return null;
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

	public static byte[] trimArrayByOne ( byte[] bytes )
	{

		byte[] ret = new byte[bytes.length-1];
		for ( int i = 1, j = 0 ; i < bytes.length ; i ++)
		{
			ret[j] = bytes[i];
			j ++;
		}

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


    // client joins the room
    // send his mixed key to the server
    // the server broadcast the keys to the occupants of the chat
    // the other clients generate their keys with the given key ( the broadcasted one )
    // and in return they send to the server their mixed keys
    //
    // ATTENTION TO THE INTEGER OVERFLOW ( COULD BE A PROBLEM ).

}
