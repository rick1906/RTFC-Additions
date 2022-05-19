package com.rick.rtfcadditions.commands.custom;

import com.bioxx.tfc.Core.TFC_Core;
import com.bioxx.tfc.Food.ItemMeal;
import com.rick.rtfcadditions.Messenger;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author Rick
 */
public class MakeBestFoodCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)sender;
            ItemStack is = player.inventory.getCurrentItem();
            if (is != null && is.getItem() instanceof ItemMeal) {
                int ci = player.inventory.currentItem;
                int[] prefs = TFC_Core.getPlayerFoodStats(player).getPrefTaste();
                NBTTagCompound nbt = is.getTagCompound();
                nbt.setInteger("tasteSweet", prefs[0]);
                nbt.setInteger("tasteSour", prefs[1]);
                nbt.setInteger("tasteSalty", prefs[2]);
                nbt.setInteger("tasteBitter", prefs[3]);
                nbt.setInteger("tasteUmami", prefs[4]);
                is.setTagCompound(nbt);
                if (player instanceof EntityPlayerMP && !player.worldObj.isRemote) {
                    ((EntityPlayerMP)player).sendSlotContents(player.inventoryContainer, ci, is);
                }
                return true;
            } else {
                Messenger.sendInfo(sender, "Not a food");
                return false;
            }
        } else {
            Messenger.sendInfo(sender, "Not a player");
            return false;
        }
    }

}
