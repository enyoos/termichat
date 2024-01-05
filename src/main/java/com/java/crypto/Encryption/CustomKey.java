package com.java.crypto.Encryption;

import java.math.BigInteger;
import java.security.Key;

public class CustomKey implements Key{
    public BigInteger n;

    @Override
    public String getAlgorithm() {
        return "RSA";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[]{};
    }
    
}
