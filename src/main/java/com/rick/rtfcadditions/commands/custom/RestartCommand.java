package com.rick.rtfcadditions.commands.custom;

/**
 *
 * @author Rick
 */
public class RestartCommand extends ShutdownCommand
{
    @Override
    public boolean isRestart()
    {
        return true;
    }

}
