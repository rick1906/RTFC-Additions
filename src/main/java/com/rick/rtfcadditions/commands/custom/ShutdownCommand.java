package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.core.ServerHandler;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Rick
 */
public class ShutdownCommand extends AbstractCommand
{
    public boolean isRestart()
    {
        return false;
    }

    @Override
    public boolean run(ICommandSender sender)
    {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("cancel")) {
                ServerHandler.getInstance().cancelStopServer();
                return true;
            }
            try {
                int seconds = Integer.parseUnsignedInt(args[0]);
                ServerHandler.getInstance().stopServer(seconds, isRestart());
            } catch (NumberFormatException ex) {
                return false;
            }
        } else {
            ServerHandler.getInstance().stopServer(0, isRestart());
        }
        return true;
    }

}
