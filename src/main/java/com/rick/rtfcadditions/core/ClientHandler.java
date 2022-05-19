package com.rick.rtfcadditions.core;

import com.bioxx.tfc.Core.TFC_Time;
import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.Messenger;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

/**
 *
 * @author Rick
 */
public class ClientHandler
{
    public static int USE_POTION_DELAY = 40;

    public static ClientHandler getInstance()
    {
        if (_instance == null) {
            _instance = new ClientHandler();
        }
        return _instance;
    }

    public static EntityPlayer getPlayer()
    {
        return Minecraft.getMinecraft().thePlayer;
    }

    private static ClientHandler _instance = null;

    private GuiScreen gui = null;
    private final List<ClientScreenHandler> guiHandlers = new ArrayList<ClientScreenHandler>();

    private ItemStack lastUsedItem = null;
    private long lastUsedTime = Integer.MIN_VALUE;

    private ClientHandler()
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChangeGUI(GuiOpenEvent event)
    {
        if (event.gui instanceof GuiScreenRealmsProxy) {
            event.setCanceled(true);
            return;
        }
        if (gui != event.gui) {
            if (gui != null) {
                handleGuiClose();
            }
            gui = event.gui;
            handleGuiOpen(gui);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerUseItemStart(PlayerUseItemEvent.Start event)
    {
        if (!event.entity.worldObj.isRemote) {
            return; // do not run on server side
        }
        if (event.item != null && event.item == lastUsedItem) {
            long time = TFC_Time.getTotalTicks();
            if (event.item.getItem() instanceof ItemPotion && time < lastUsedTime + USE_POTION_DELAY) {
                Minecraft.getMinecraft().playerController.onStoppedUsingItem(getPlayer());
                event.setCanceled(true);
                return;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerUseItemFinish(PlayerUseItemEvent.Finish event)
    {
        if (!event.entity.worldObj.isRemote) {
            return; // do not run on server side
        } else {
            lastUsedItem = event.item;
            lastUsedTime = TFC_Time.getTotalTicks();
        }
    }

    private void handleGuiOpen(GuiScreen gui)
    {
        if (gui instanceof GuiContainer) {
            registerScreenHandler(new ClientContainerHandler((GuiContainer)gui));
        }
    }

    private void registerScreenHandler(ClientScreenHandler handler)
    {
        FMLCommonHandler.instance().bus().register(handler);
        guiHandlers.add(handler);
    }

    private void handleGuiClose()
    {
        for (ClientScreenHandler sh : guiHandlers) {
            FMLCommonHandler.instance().bus().unregister(sh);
            sh.handleClose();
        }
        guiHandlers.clear();
    }

}
