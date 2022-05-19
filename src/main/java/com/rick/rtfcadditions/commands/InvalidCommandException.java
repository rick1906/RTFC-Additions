package com.rick.rtfcadditions.commands;

/**
 *
 * @author Rick
 */
public class InvalidCommandException extends IllegalArgumentException
{
    public InvalidCommandException(String s)
    {
        super(s);
    }
}
