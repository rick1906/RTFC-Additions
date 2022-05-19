package com.rick.rtfcadditions.commands.custom;

import com.bioxx.tfc.Core.TFC_Climate;
import com.bioxx.tfc.Core.TFC_Core;
import com.bioxx.tfc.Core.TFC_Time;
import com.rick.rtfcadditions.Messenger;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author Rick
 */
public class GetTempCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)sender;
            float temp = TFC_Core.getCachedTemp(player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ, (int)TFC_Time.getTotalHours());
            float rain = TFC_Climate.getRainfall(player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
            String message = "Temperature: " + temp + "; rainfall: " + rain;
            Messenger.send(sender, message);
            return true;
        }
        return false;
    }
    
}
