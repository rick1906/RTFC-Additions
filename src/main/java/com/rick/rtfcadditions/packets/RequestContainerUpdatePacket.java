package com.rick.rtfcadditions.packets;

import com.bioxx.tfc.Handlers.Network.AbstractPacket;
import com.rick.rtfcadditions.helpers.InventoryHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public class RequestContainerUpdatePacket extends AbstractPacket
{
    private int windowId;
    private List<Integer> slotIds;

    public RequestContainerUpdatePacket()
    {
    }

    public RequestContainerUpdatePacket(Container container)
    {
        this.windowId = container.windowId;
        this.slotIds = new ArrayList<Integer>();
        int containerSize = container.inventorySlots.size();
        for (int i = 0; i < containerSize; ++i) {
            ItemStack is1 = container.getSlot(i).getStack();
            ItemStack is2 = (ItemStack)container.inventoryItemStacks.get(i);
            if (!InventoryHelper.ItemStacksAreEqual(is1, is2)) {
                slotIds.add(i);
            }
        }
    }

    @Override
    public void encodeInto(ChannelHandlerContext chc, ByteBuf bb)
    {
        int size = this.slotIds.size();
        bb.writeByte(this.windowId);
        bb.writeByte(size);
        for (int i = 0; i < size; ++i) {
            bb.writeByte(this.slotIds.get(i));
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext chc, ByteBuf bb)
    {
        this.slotIds = new ArrayList<Integer>();
        this.windowId = bb.readByte();
        int size = bb.readByte();
        for (int i = 0; i < size; ++i) {
            int slotId = bb.readByte();
            this.slotIds.add(slotId);
        }
    }

    @Override
    public void handleClientSide(EntityPlayer ep)
    {
    }

    @Override
    public void handleServerSide(EntityPlayer ep)
    {
        if (ep.openContainer != null && this.windowId == ep.openContainer.windowId && ep instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)ep;
            int size = this.slotIds.size();
            for (int i = 0; i < size; ++i) {
                player.sendSlotContents(player.openContainer, i, player.openContainer.getSlot(i).getStack());
            }
        }
    }
}
