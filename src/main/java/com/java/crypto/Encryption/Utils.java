package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

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

   public static KeyPair   gKeyPair ( ) throws Exception{ 
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair pair = generator.generateKeyPair();
		return pair;
}

public static BigInteger gSK ( BigInteger pk, BigInteger mK, BigInteger P ){ return mK.modPow ( pk, P ); }

    public static void main(String[] args) throws Exception {

       // String msg = "some msg";
       // msg        = "some other very long msg, hello world, dont let me down ! Are you serious right now ! hello world, dont lecture me with that 30 dollar haircut., fklsjfdklsjfsdkljfdskjfds, asjfklsajfdksljfds, adsfjklsdklfjdklsjfdsklf, sdfdklfd=can you reapeat ?, sjkljfskljfdklsjfsdkljfdklsjwu847384973djsfjsdfkjsdfklsdjfklsdjklj";
       // 
       // KeyPair pair = gKeyPair();
       // PrivateKey sk = pair.getPrivate();
       // PublicKey  pk = pair.getPublic() ;
       // System.out.println( "the key mat : " + Arrays.toString ( sk.getEncoded() ) );
       // 
       // Cipher encryptCipher = Cipher.getInstance("RSA");
       // encryptCipher.init(Cipher.ENCRYPT_MODE, pk);


	    BigInteger b = new BigInteger ( "4389472" );
	    System.out.println( b.toString() );

       // // transforming our msg to bytes
       // byte[] secretMessageBytes = msg.getBytes(StandardCharsets.UTF_8);

       // byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

       // // for decryption
       // Cipher decryptCipher = Cipher.getInstance("RSA");
       // decryptCipher.init(Cipher.DECRYPT_MODE, sk);
       // byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
       // String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

       // System.out.println(decryptedMessage);

    }


    public static String bytes2Str ( byte[] bytes )
    {
        String ret = "";
        for ( byte b : bytes ) { ret += ( char ) b; }
        return ret;
    }

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
    public static String encryptMessageWithKey ( String msg, SecretKey key, IvParameterSpec spec ) throws Exception
    {
        String input = msg;
        // a secret key is just a long number
        return encrypt(DEFAULT_ALGO, input, key, spec);

    }

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

    public static PublicKey gPK () throws NoSuchAlgorithmException  
    {
        System.out.println("generating 2048 bit public key ...");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(BIT_SIZE);
        KeyPair pair = generator.generateKeyPair();
        PublicKey pk = pair.getPublic();
        return pk;
    }

    public static String decryptMessageWithKey ( String input, SecretKey key , IvParameterSpec spec) throws Exception
    {
        return decrypt(DEFAULT_ALGO, input, ( SecretKey ) key , spec);
    }

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
