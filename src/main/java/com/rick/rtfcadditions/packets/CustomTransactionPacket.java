package com.rick.rtfcadditions.packets;

import com.bioxx.tfc.Handlers.Network.AbstractPacket;
import com.bioxx.tfc.TerraFirmaCraft;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C0EPacketClickWindow;

/**
 *
 * @author Rick
 */
public class CustomTransactionPacket extends AbstractPacket
{
    private int windowId;
    private C0EPacketClickWindow internalPacket;

    public CustomTransactionPacket()
    {
    }

    public CustomTransactionPacket(int windowId, int slotId, int usedButton, int mode, ItemStack is, short transactionId)
    {
        this.windowId = windowId;
        this.internalPacket = new C0EPacketClickWindow(windowId, slotId, usedButton, mode, is, transactionId);
    }

    @Override
    public void encodeInto(ChannelHandlerContext chc, ByteBuf bb)
    {
        try {
            PacketBuffer buf = new PacketBuffer(bb);
            buf.writeByte(this.windowId);
            internalPacket.writePacketData(buf);
        } catch (IOException ex) {
            TerraFirmaCraft.LOG.info("--------------------------------------------------");
            TerraFirmaCraft.LOG.catching(ex);
            TerraFirmaCraft.LOG.info("--------------------------------------------------");
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext chc, ByteBuf bb)
    {
        try {
            PacketBuffer buf = new PacketBuffer(bb);
            this.internalPacket = new C0EPacketClickWindow();
            this.windowId = buf.readByte();
            internalPacket.readPacketData(buf);
        } catch (IOException ex) {
            TerraFirmaCraft.LOG.info("--------------------------------------------------");
            TerraFirmaCraft.LOG.catching(ex);
            TerraFirmaCraft.LOG.info("--------------------------------------------------");
        }
    }

    @Override
    public void handleClientSide(EntityPlayer ep)
    {

    }

    @Override
    public void handleServerSide(EntityPlayer ep)
    {
        if (this.windowId == 0 && ep instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)ep;
            Container current = player.openContainer;
            player.openContainer = player.inventoryContainer;
            player.playerNetServerHandler.processClickWindow(this.internalPacket);
            player.openContainer.setPlayerIsPresent(ep, true);
            player.openContainer = current;
        }
    }

}
