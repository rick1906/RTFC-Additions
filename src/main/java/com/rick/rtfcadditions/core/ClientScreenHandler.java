package com.rick.rtfcadditions.core;

import com.rick.rtfcadditions.Messenger;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Rick
 */
public abstract class ClientScreenHandler
{
    private final GuiScreen gui;
    private boolean tickHandler = false;
    private boolean isKeyPressed = false;
    private int keyPressed = -1;

    public ClientScreenHandler(GuiScreen gui)
    {
        this.gui = gui;
    }

    public final GuiScreen getGUI()
    {
        return this.gui;
    }

    public abstract void handleOpen(GuiScreen gui);

    public abstract void handleKeyUp(int keycode);

    public abstract void handleKeyDown(int keycode);

    public abstract void handleClose();

    public abstract void handleTick(TickEvent.ClientTickEvent event);

    protected final void enableTickHandler()
    {
        tickHandler = true;
    }

    protected final void disableTickHandler()
    {
        tickHandler = false;
    }

    protected final boolean processKeyState()
    {
        if (FMLClientHandler.instance().isGUIOpen(GuiChat.class)) {
            return false;
        }
        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey();
            if (!isKeyPressed || keyPressed != key) {
                if (isKeyPressed) {
                    handleKeyUp(keyPressed);
                }
                isKeyPressed = true;
                keyPressed = key;
                handleKeyDown(key);
                return true;
            }
        } else if (isKeyPressed) {
            handleKeyUp(keyPressed);
            isKeyPressed = false;
            keyPressed = -1;
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public final void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.ClientTickEvent.Phase.START) {
            processKeyState();
        }
        if (tickHandler) {
            handleTick(event);
        }
    }

}
