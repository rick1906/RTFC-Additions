package com.rick.rtfcadditions.core;

import com.bioxx.tfc.Containers.ContainerCreativeTFC;
import com.bioxx.tfc.Containers.ContainerTFC;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.Food.ItemMeal;
import com.bioxx.tfc.TerraFirmaCraft;
import com.bioxx.tfc.api.Constant.Global;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.Tools.IKnife;
import com.rick.rtfcadditions.helpers.InventoryHelper;
import com.rick.rtfcadditions.Messenger;
import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.packets.RequestContainerUpdatePacket;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import org.lwjgl.input.Mouse;

public class ClientContainerHandler extends ClientScreenHandler
{
    private final GuiContainer container;
    private final boolean isNormalContainer;
    private long spamTimer;
    
    public ClientContainerHandler(GuiContainer container)
    {
        super(container);
        this.container = container;
        this.isNormalContainer = checkIsNormal(container);
        this.spamTimer = TFC_Time.getTotalTicks();
    }
    
    private boolean checkIsNormal(GuiContainer container)
    {
        if (container.inventorySlots == null || container.inventorySlots.inventorySlots == null) {
            return false;
        }
        if (container.inventorySlots instanceof ContainerPlayer) {
            return false;
        }
        if (container.inventorySlots instanceof ContainerCreativeTFC) {
            return false;
        }
        if (container.inventorySlots.inventorySlots.size() <= 1) {
            return false;
        }
        try {
            return container.inventorySlots.canInteractWith(ClientHandler.getPlayer());
        } catch (Exception ex) {
            return false;
        }
    }
    
    protected boolean checkHotbarKeys(int keycode)
    {
        if (container.mc.displayWidth <= 0 || container.mc.displayHeight <= 0) {
            return false;
        }
        if (TFC_Time.getTotalTicks() <= spamTimer + 5) {
            return false;
        }
        
        int mouseX = Mouse.getEventX() * container.width / container.mc.displayWidth;
        int mouseY = container.height - 1 - Mouse.getEventY() * container.height / container.mc.displayHeight;
        int sizeInventory = container.inventorySlots.inventorySlots.size();
        Slot activeSlot = null;
        for (int j = 0; j < sizeInventory; ++j) {
            Slot slot = container.inventorySlots.getSlot(j);
            if (InventoryHelper.isMouseOverSlot(container, slot, mouseX, mouseY) && slot.func_111238_b()) {
                activeSlot = slot;
            }
        }
        if (activeSlot == null || !activeSlot.canTakeStack(ClientHandler.getPlayer())) {
            return false;
        }
        
        if (keycode == 31 && activeSlot.getHasStack() && activeSlot.getStack() != null && activeSlot.getStack().getItem() instanceof IFood && TFC_Time.getTotalTicks() > spamTimer + 5) {
            spamTimer = TFC_Time.getTotalTicks();
            applyFoodCombining(activeSlot);
        } else if (keycode == 32 && TFC_Time.getTotalTicks() > spamTimer + 5) {
            spamTimer = TFC_Time.getTotalTicks();
            applyFoodTrimming();
        }
        return false;
    }
    
    private boolean applyFoodCombining(Slot activeSlot)
    {
        Item iType = activeSlot.getStack().getItem();
        ItemStack activeIs = activeSlot.getStack();
        int run = 0;
        int containerSize = container.inventorySlots.inventorySlots.size();
        if (!(iType instanceof IFood) || (iType instanceof ItemMeal)) {
            return false;
        }
        
        EntityPlayer player = ClientHandler.getPlayer();
        if (!InventoryHelper.getCraftingIsEmpty(player)) {
            return false;
        }
        if (InventoryHelper.getPlayerInventoryEmptySlotsCount(player) < 2) {
            return false;
        }
        
        int[] craftSlots = InventoryHelper.getCraftingSlots(player);
        boolean changed = false;
        do {
            ArrayList<Slot> toCraft = new ArrayList<Slot>();
            for (int i = 0; i < containerSize && toCraft.size() < craftSlots.length; i++) {
                Slot slot = container.inventorySlots.getSlot(i);
                if (slot.inventory != player.inventory && slot.canTakeStack(player)) {
                    ItemStack is = slot.getStack();
                    if (is != null && is.getItem() == iType && Food.areEqual(activeIs, is) && Food.getWeight(is) < Global.FOOD_MAX_WEIGHT) {
                        toCraft.add(slot);
                    }
                }
            }
            if (toCraft.size() > 1) {
                run++;
                changed = true;
                List<Slot> source = toCraft.size() > 6 ? toCraft.subList(0, 6) : toCraft;
                List<Slot> result = InventoryHelper.applyClientSideCrafting(container, source);
                if (result == null) {
                    break;
                }
            } else {
                break;
            }
        } while (run < containerSize * containerSize);
        
        if (run >= containerSize * containerSize) {
            DebugUtils.logWarn("Client-side food combining failed");
        }
        if (changed) {
            requestContainerUpdate();
        }
        
        return changed;
    }
    
    private boolean applyFoodTrimming()
    {
        EntityPlayer player = ClientHandler.getPlayer();
        if (!InventoryHelper.getCraftingIsEmpty(player)) {
            return false;
        }
        if (InventoryHelper.isPlayerInventoryFull(player)) {
            return false;
        }
        
        int knifeSlot = -1;
        for (int i = InventoryHelper.INV_NORMAL_START; i <= InventoryHelper.INV_NORMAL_END; i++) {
            ItemStack is = player.inventoryContainer.getSlot(i).getStack();
            if (is != null && is.getItem() instanceof IKnife) {
                knifeSlot = i;
                break;
            }
        }
        if (knifeSlot < 0) {
            return false;
        }
        
        Slot knife = player.inventoryContainer.getSlot(knifeSlot);
        int containerSize = container.inventorySlots.inventorySlots.size();
        boolean changed = false;
        for (int i = 0; i < containerSize && knife.getStack() != null; i++) {
            Slot slot = container.inventorySlots.getSlot(i);
            if (slot.inventory != player.inventory && slot.canTakeStack(player)) {
                ItemStack is = slot.getStack();
                int knifeDamage = knife.getStack().getItemDamage();
                if (knifeDamage >= knife.getStack().getMaxDamage()) {
                    break;
                }
                if (is != null && !(is.getItem() instanceof ItemMeal) && is.getItem() instanceof IFood && Food.getDecay(is) > 0
                    && Food.getDecayTimer(is) >= TFC_Time.getTotalHours()) {
                    changed = true;
                    ArrayList<Slot> toCraft = new ArrayList<Slot>();
                    toCraft.add(slot);
                    List<Slot> result = InventoryHelper.applyClientSideCrafting(container, toCraft);
                    if (result == null) {
                        break;
                    }
                }
            }
        }
        
        if (changed) {
            requestContainerUpdate();
        }
        
        return changed;
    }
    
    private void requestContainerUpdate()
    {
        if (container.inventorySlots instanceof ContainerTFC) {
            TerraFirmaCraft.PACKET_PIPELINE.sendToServer(new RequestContainerUpdatePacket(container.inventorySlots));
        }
        container.inventorySlots.detectAndSendChanges();
    }
    
    @Override
    public void handleKeyDown(int keycode)
    {
        if (isNormalContainer && (keycode == 31 || keycode == 32)) {
            checkHotbarKeys(keycode);
        }
    }
    
    @Override
    public void handleKeyUp(int keycode)
    {
        
    }
    
    @Override
    public void handleClose()
    {
        
    }
    
    @Override
    public void handleOpen(GuiScreen gui)
    {
        
    }
    
    @Override
    public void handleTick(TickEvent.ClientTickEvent event)
    {
    }
    
}
