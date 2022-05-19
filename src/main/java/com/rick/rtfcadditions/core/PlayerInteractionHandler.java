package com.rick.rtfcadditions.core;

import binnie.core.machines.TileEntityMachine;
import com.bioxx.tfc.Blocks.Flora.BlockFruitLeaves;
import com.bioxx.tfc.Core.TFC_Core;
import com.bioxx.tfc.TileEntities.TEBarrel;
import com.bioxx.tfc.TileEntities.TEFruitLeaves;
import com.bioxx.tfc.TileEntities.TEFruitTreeWood;
import com.bioxx.tfc.api.TFCItems;
import com.bioxx.tfc.api.TFC_ItemHeat;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import forestry.core.items.ItemPipette;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author Rick
 */
public class PlayerInteractionHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
        boolean ret;
        if (event.world.isRemote) {
            ret = handleClientInteract(event, te);
        } else {
            ret = handleServerInteract(event, te);
        }
        if (ret) {
            return;
        }
        if (te instanceof TEBarrel && handleBarrel(event, (TEBarrel)te)) {
            return;
        }
        if (event.entityPlayer.getHeldItem() != null && event.entityPlayer.getHeldItem().getItem() == TFCItems.stick) {
            if (te instanceof TEFruitLeaves || te instanceof TEFruitTreeWood) {
                event.entityPlayer.swingItem();
                if (!event.world.isRemote) {
                    int tx = event.x;
                    int ty = event.y;
                    int tz = event.z;
                    for (int iy = ty; iy <= ty + 2; iy++) {
                        for (int ix = tx - 1; ix <= tx + 1; ix++) {
                            for (int iz = tz - 1; iz <= tz + 1; iz++) {
                                if (iy <= 255 && !(iy == ty && ix == tx && iz == tz)) {
                                    Block block = event.world.getBlock(ix, iy, iz);
                                    if (block instanceof BlockFruitLeaves) {
                                        block.onBlockClicked(event.world, ix, iy, iz, event.entityPlayer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean handleBarrel(PlayerInteractEvent event, TEBarrel te)
    {
        if (event.entityPlayer != null && !event.entityPlayer.worldObj.isRemote) {
            EntityPlayer player = event.entityPlayer;
            ItemStack is = player.getHeldItem();
            if (is != null && is.getItem() instanceof ItemPipette) {
                ItemPipette pi = (ItemPipette)is.getItem();
                FluidStack fs = pi.drain(is, 1000, false);
                if (fs != null && fs.amount > 0) {
                    FluidStack orig = fs.copy();
                    boolean result = te.addLiquid(fs);
                    if (result && fs.amount != orig.amount) {
                        pi.drain(is, orig.amount - fs.amount, true);
                        player.setCurrentItemOrArmor(0, is);
                        sendHeldItemUpdate(player);
                        event.setCanceled(true);
                        return true;
                    }
                } else {
                    FluidStack bfs = te.getFluidStack();
                    if (bfs != null && bfs.amount > 0) {
                        int filled = pi.fill(is, bfs, false);
                        int maxFilled = pi.getCapacity(is);
                        if (filled > 0 && !te.getSealed()) {
                            filled = Math.min(pi.fill(is, bfs, true), maxFilled);
                            te.drainLiquid(filled);
                            player.setCurrentItemOrArmor(0, is);
                            sendHeldItemUpdate(player);
                            event.setCanceled(true);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean handleServerInteract(PlayerInteractEvent event, TileEntity te)
    {
        if (te != null) {
            if (te instanceof IInventory) {
                TFC_Core.handleItemTicking((IInventory)te, event.world, te.xCoord, te.yCoord, te.zCoord, false);
            } else if (te instanceof TileEntityMachine) {
                TFC_Core.handleItemTicking(((TileEntityMachine)te).getInventory(), event.world, te.xCoord, te.yCoord, te.zCoord, false);
            }
        }
        return false;
    }

    private boolean handleClientInteract(PlayerInteractEvent event, TileEntity te)
    {
        ItemStack is = event.entityPlayer.getHeldItem();
        if (is != null && te != null && te instanceof TileEntityMachine) {
            float temp = TFC_ItemHeat.getTemp(is);
            if (temp > 0) {
                event.setCanceled(true);
                return true;
            }
        }
        return false;
    }

    private void sendHeldItemUpdate(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP)player).playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));
        }
    }
}
