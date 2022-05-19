package com.rick.rtfcadditions.commands.custom;

import com.bioxx.tfc.Core.TFC_Core;
import com.rick.rtfcadditions.Messenger;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author Rick
 */
public class GetTasteStatsCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            int[] stats = TFC_Core.getPlayerFoodStats((EntityPlayer)sender).getPrefTaste();
            String message = "";
            for (int i = 0; i < stats.length; ++i) {
                message += stats[i] + ";";
            }
            Messenger.send(sender, message);
            return true;
        }
        return false;
    }
    
}
