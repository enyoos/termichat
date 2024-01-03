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
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public final class Utils {

    private static final String DEFAULT_ALGO = "RSA";
    private static final int BIT_SIZE         = 2048;
    private static final Random random        = new Random();

    // public static SecretKey combineKeyWithPAndG( BigInteger key, BigInteger P, BigInteger G )
    // {
    //     SecretKey xp = G.pow(P.intValue());
    //     SecretKey mod = xp.mod(P);
    //     return mod;
    // }

    // public static SecretKey getSecretKey( BigInteger key1, BigInteger key2, BigInteger P)
    // {
    //     SecretKey xp = key2.pow(key1.intValue());
    //     SecretKey mod = xp.mod(P);
    //     return mod;
    // }

    // simulates the diffie hellman exchange.
    // read : https://sites.ualberta.ca/~jhoover/ConcreteComputing/section/diffie_hellman.htm
    // public static void diffie( int bitSize )
    // {

    //     SecretKey alicePK = generateBigPrime(bitSize);
    //     SecretKey bobPK   = generateBigPrime(bitSize);

    //     // generated server-side
    //     SecretKey P = generateBigPrime(bitSize) /* new BigInteger("23") */;
    //     SecretKey G = generateBigPrime(bitSize) /* new BigInteger("5")*/;

    //     SecretKey mixAlicePK = G.modPow(alicePK, P);
    //     SecretKey mixBobPK   = G.modPow(bobPK, P);

    //     SecretKey aliceSK    = mixBobPK.modPow(alicePK, P);
    //     SecretKey bobSK      = mixAlicePK.modPow(bobPK, P);

    //     System.out.println("sk alice : " + aliceSK);;
    //     System.out.println("sk bob: " +    bobSK);;
    // }

    // public static void diffie ( )
    // {
    //     diffie(BIT_SIZE);
    // }

    private static BigInteger generatePrime (  int size )
    {
        return BigInteger.probablePrime(size, random);
    }
    
    // private static generateKey(int n) throws NoSuchAlgorithmException {
    //     KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    //     keyGenerator.init(n);
    //     SecretKey key = keyGenerator.generateKey();
    //     return key;
    // }

    // public static SecretKey gKey ( ) throws NoSuchAlgorithmException { return generateKey(AES_SIZE_KEY_BIT); }

    public static KeyPair   gKeyPair ( ) throws Exception{ 
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    public static void main(String[] args) throws Exception {

        // it's called assymitric
        // KeyPair kp = gKeyPair();
        // BigInteger sk = generatePrime(1024);
        // BigInteger pk = generatePrime(1024);
        String msg = "some msg";

       // PublicKey  pk = pair.getPublic();
        
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pk);

        // transforming our msg to bytes
        byte[] secretMessageBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

        // for decryption
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, sk);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

        System.out.println(decryptedMessage);

    }

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

    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
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
