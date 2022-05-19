package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.Messenger;
import com.rick.rtfcadditions.minetweaker.MineTweakerTweaks;
import java.util.ConcurrentModificationException;
import minetweaker.MineTweakerImplementationAPI;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Rick
 */
public class ScriptsCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (args.length <= 0) {
            reload(sender);
            return true;
        } else {
            String cmd = args[0];
            if (cmd.equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }
            return false;
        }
    }

    private void reload(ICommandSender sender)
    {
        int attempts = 0;
        int maxAttempts = 3;
        int errors;
        boolean tryAgain = false;
        do {
            MineTweakerTweaks.resetMainLoggers();
            MineTweakerTweaks.currentErrors.clear();
            MineTweakerTweaks.currentExceptions.clear();
            MineTweakerImplementationAPI.reload();
            errors = MineTweakerTweaks.currentErrors.size();
            Messenger.sendInfo(sender, "MineTweaker: scripts reloaded (" + errors + " errors)");
            if (errors == 1 && MineTweakerTweaks.currentExceptions.size() == 1) {
                tryAgain = MineTweakerTweaks.currentExceptions.get(0) instanceof ConcurrentModificationException;
            }
            attempts++;
            if (tryAgain && attempts < maxAttempts) {
                Messenger.sendInfo(sender, "MineTweaker: retrying...");
            }
        } while (attempts < maxAttempts && errors > 0 && tryAgain);
        MineTweakerTweaks.currentErrors.clear();
    }

}
