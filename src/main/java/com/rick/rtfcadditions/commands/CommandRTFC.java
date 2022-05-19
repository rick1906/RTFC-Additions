package com.rick.rtfcadditions.commands;

import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.Messenger;
import com.rick.rtfcadditions.commands.custom.AbstractCommand;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandRTFC extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "rtfc";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "Usage: /rtfc <command> [arg1] [arg2] ...";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length > 0) {
            AbstractCommand command;
            boolean result = false;
            Messenger.sendInfo(sender, "Received RTFC command: " + args[0]);
            try {
                command = AbstractCommand.create(args);
                result = command.run(sender);
            } catch (InvalidCommandException ex) {
                Messenger.sendWarn(sender, ex.getMessage());
            } catch (Exception ex) {
                Messenger.sendWarn(sender, ex.getMessage() + "(" + ex.getClass().getName() + ")");
                DebugUtils.logException(ex);
            }
            if (result) {
                Messenger.sendInfo(sender, "Success");
            } else {
                Messenger.sendInfo(sender, "Failure");
            }
        } else {
            throw new WrongUsageException("No arguments supplied", new Object[0]);
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return null;
    }

}
