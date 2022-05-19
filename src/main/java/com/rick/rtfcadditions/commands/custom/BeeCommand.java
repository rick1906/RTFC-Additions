package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.Messenger;
import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.mods.forestry.BeeTweaker;
import forestry.api.apiculture.IBee;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public class BeeCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)sender;
            ItemStack is = player.inventory.getStackInSlot(player.inventory.currentItem);
            if (is != null) {
                IBee bee = BeeTweaker.getBee(is);
                IBee result = null;
                if (bee != null) {
                    try {
                        if (args.length <= 0) {
                            result = BeeTweaker.transformForTesting(bee);
                        } else if (args.length > 1) {
                            if (args[0].equalsIgnoreCase("flower")) {
                                result = BeeTweaker.transformForFlower(bee, args[1]);
                            } else if (args[0].equalsIgnoreCase("effect")) {
                                result = BeeTweaker.transformForEffect(bee, args[1]);
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } catch (Exception ex) {
                        DebugUtils.logWarn("Failed to transform bee");
                        DebugUtils.logException(ex);
                        return false;
                    }
                }
                if (result != null) {
                    ItemStack rs = BeeTweaker.getBeeStack(result, is);
                    if (rs != null) {
                        rs.stackSize = is.stackSize;
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, rs);
                        if (player instanceof EntityPlayerMP && !player.worldObj.isRemote) {
                            EntityPlayerMP pmp = (EntityPlayerMP)player;
                            pmp.sendSlotContents(player.inventoryContainer, player.inventory.currentItem, rs);
                        }
                        return true;
                    } else {
                        Messenger.sendWarn(sender, "Failed to create item stack");
                        return false;
                    }
                }
            } else {
                Messenger.sendWarn(sender, "No item stack selected");
                return false;
            }
        }
        Messenger.sendWarn(sender, "Not a player");
        return false;
    }

}
