package com.rick.rtfcadditions.helpers;

import com.bioxx.tfc.GUI.GuiInventoryTFC;
import com.bioxx.tfc.TerraFirmaCraft;
import com.rick.rtfcadditions.core.ClientHandler;
import com.rick.rtfcadditions.packets.CustomTransactionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public final class GuiInventoryHelper extends GuiInventoryTFC
{
    public GuiInventoryHelper()
    {
        super(ClientHandler.getPlayer());
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    protected void handleMouseClick(Slot par1Slot, int slotId, int mouseButtonClicked, int mode)
    {
        short trId = this.inventorySlots.getNextTransactionID(player.inventory);
        ItemStack itemstack = this.inventorySlots.slotClick(slotId, mouseButtonClicked, mode, this.player);
        TerraFirmaCraft.PACKET_PIPELINE.sendToServer(new CustomTransactionPacket(0, slotId, mouseButtonClicked, mode, itemstack, trId));
    }

}
