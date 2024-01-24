package com.java.crypto.Command.Commands;

// interface requiring you to parse the input ( as command with additional flags )
public interface Parseable
{
    public void parse(String input); // construct the "AST";
    public boolean eval ()            ; // evaluates the "AST";
}