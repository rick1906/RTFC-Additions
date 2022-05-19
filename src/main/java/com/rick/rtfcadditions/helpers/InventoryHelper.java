package com.rick.rtfcadditions.helpers;

import com.bioxx.tfc.GUI.GuiInventoryTFC;
import com.rick.rtfcadditions.core.ClientHandler;
import com.rick.rtfcadditions.Messenger;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public abstract class InventoryHelper
{
    private static Method _isMouseOverSlot = null;
    private static Method _handleMouseClick = null;

    public final static int INV_NORMAL_START = 9;
    public final static int INV_NORMAL_END = 44;

    private final static int[] CRAFTING_SLOTS = new int[] { 4, 1, 2, 3, 45, 46, 47, 48, 49 };

    public static boolean isMouseOverSlot(GuiContainer container, Slot slot, int mouseX, int mouseY)
    {
        if (_isMouseOverSlot == null) {
            _isMouseOverSlot = ReflectionHelper.findMethod(GuiContainer.class, container, new String[] { "isMouseOverSlot", "func_146981_a" }, Slot.class, int.class, int.class);
        }
        return (Boolean)InvocationHelper.invoke(_isMouseOverSlot, container, slot, mouseX, mouseY);
    }

    public static void handleMouseClick(GuiContainer container, Slot slot, int sourceSlot, int destSlot, int clickType)
    {
        if (_handleMouseClick == null) {
            _handleMouseClick = ReflectionHelper.findMethod(GuiContainer.class, container, new String[] { "handleMouseClick", "func_146984_a" }, Slot.class, int.class, int.class, int.class);
        }
        InvocationHelper.invoke(_handleMouseClick, container, slot, sourceSlot, destSlot, clickType);
    }

    public static List<Slot> getInventorySlots(Container container, IInventory inventory)
    {
        ArrayList<Slot> slots = new ArrayList<Slot>();
        int size = container.inventorySlots.size();
        for (int i = 0; i < size; ++i) {
            Slot slot = container.getSlot(i);
            if (slot.inventory == inventory) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static boolean isInventoryFull(List<Slot> slots)
    {
        for (Slot slot : slots) {
            if (slot.getStack() == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInventoryFull(Container container, int startIndex, int endIndex)
    {
        for (int i = startIndex; i <= endIndex; ++i) {
            if (container.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInventoryFull(Container container)
    {
        return isInventoryFull(container, 0, container.inventorySlots.size() - 1);
    }

    public static boolean isPlayerInventoryFull(EntityPlayer player)
    {
        return isInventoryFull(player.inventoryContainer, INV_NORMAL_START, INV_NORMAL_END);
    }

    public static int getInventoryEmptySlotsCount(List<Slot> slots)
    {
        int count = 0;
        for (Slot slot : slots) {
            if (slot.getStack() == null) {
                count++;
            }
        }
        return count;
    }

    public static int getInventoryEmptySlotsCount(Container container, int startIndex, int endIndex)
    {
        int count = 0;
        for (int i = startIndex; i <= endIndex; ++i) {
            if (container.getSlot(i).getStack() == null) {
                count++;
            }
        }
        return count;
    }

    public static int getInventoryEmptySlotsCount(Container container)
    {
        return getInventoryEmptySlotsCount(container, 0, container.inventorySlots.size() - 1);
    }

    public static int getPlayerInventoryEmptySlotsCount(EntityPlayer player)
    {
        return getInventoryEmptySlotsCount(player.inventoryContainer, INV_NORMAL_START, INV_NORMAL_END);
    }

    public static ItemStack[] getInventoryLayout(List<Slot> slots)
    {
        int size = slots.size();
        ItemStack[] layout = new ItemStack[size];
        for (int i = 0; i < size; ++i) {
            layout[i] = slots.get(i).getStack();
        }
        return layout;
    }

    public static ItemStack[] getInventoryLayout(Container container, int startIndex, int endIndex)
    {
        int size = endIndex - startIndex + 1;
        ItemStack[] layout = new ItemStack[size];
        for (int i = 0; i < size; ++i) {
            layout[i] = container.getSlot(startIndex + i).getStack();
        }
        return layout;
    }

    public static ItemStack[] getInventoryLayout(Container container)
    {
        return getInventoryLayout(container, 0, container.inventorySlots.size() - 1);
    }

    public static List<Slot> getNewlyOccupiedSlots(ItemStack[] oldLayout, List<Slot> slots)
    {
        ArrayList<Slot> changed = new ArrayList<Slot>();
        int size = slots.size();
        for (int i = 0; i < size; ++i) {
            Slot slot = slots.get(i);
            if (i < oldLayout.length && oldLayout[i] == null && slot.getStack() != null) {
                changed.add(slot);
            }
        }
        return changed;
    }

    public static List<Slot> getNewlyOccupiedSlots(ItemStack[] oldLayout, Container container, int startIndex, int endIndex)
    {
        ArrayList<Slot> changed = new ArrayList<Slot>();
        int size = endIndex - startIndex + 1;
        for (int i = 0; i < size; ++i) {
            Slot slot = container.getSlot(startIndex + i);
            if (i < oldLayout.length && oldLayout[i] == null && slot.getStack() != null) {
                changed.add(slot);
            }
        }
        return changed;
    }

    public static List<Slot> getNewlyOccupiedSlots(ItemStack[] oldLayout, Container container)
    {
        return getNewlyOccupiedSlots(oldLayout, container, 0, container.inventorySlots.size() - 1);
    }

    public static boolean ItemStacksAreEqual(ItemStack is1, ItemStack is2)
    {
        if (is1 != null && is2 != null) {
            if (is1.isItemEqual(is2)) {
                int size1 = Math.max(is1.stackSize, 1);
                int size2 = Math.max(is2.stackSize, 1);
                return size1 == size2;
            }
            return false;
        }
        return is1 == null && is2 == null;
    }

    public static Slot intersectSlot(List<Slot> slots, ItemStack is)
    {
        int size = slots.size();
        for (int i = 0; i < size; ++i) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (ItemStacksAreEqual(stack, is)) {
                return slot;
            }
        }
        return null;
    }

    public static List<Slot> intersectInventories(List<Slot> slots, ItemStack[] layout)
    {
        ArrayList<Slot> found = new ArrayList<Slot>();
        int size = slots.size();
        for (int i = 0; i < size; ++i) {
            Slot slot = slots.get(i);
            ItemStack is1 = slot.getStack();
            if (is1 != null) {
                for (int j = 0; j < layout.length; ++j) {
                    ItemStack is2 = layout[j];
                    if (is2 != null && is1.isItemEqual(is2) && is1.stackSize == is2.stackSize) {
                        found.add(slot);
                        break;
                    }
                }
            }
        }
        return found;
    }

    public static int[] getCraftingSlots()
    {
        return CRAFTING_SLOTS;
    }

    public static int[] getCraftingSlots(EntityPlayer player)
    {
        int length = player.getEntityData().hasKey("craftingTable") ? 9 : 4;
        int[] result = new int[length];
        System.arraycopy(CRAFTING_SLOTS, 0, result, 0, length);
        return result;
    }

    public static boolean getCraftingIsEmpty(EntityPlayer player)
    {
        int[] slots = getCraftingSlots(ClientHandler.getPlayer());
        for (int i = 0; i < slots.length; ++i) {
            if (player.inventoryContainer.getSlot(slots[i]).getStack() != null) {
                return false;
            }
        }
        return true;
    }

    public static List<Slot> applyClientSideCrafting(GuiInventoryTFC guiInventory, List<Slot> source)
    {
        EntityPlayer player = ClientHandler.getPlayer();
        if (!getCraftingIsEmpty(player)) {
            return null;
        }

        int[] targets = getCraftingSlots(player);
        ArrayList<Slot> unused = new ArrayList<Slot>();
        Container container = guiInventory.inventorySlots;
        for (int i = 0; i < source.size(); ++i) {
            Slot slot = source.get(i);
            int sourceSlot = slot != null ? slot.slotNumber : -1;
            int destSlot = i < targets.length && container.getSlot(targets[i]).getStack() == null ? targets[i] : -1;
            if (sourceSlot == -1) {
                continue;
            }
            if (destSlot != -1 && slot != null) {
                ItemStack is = slot.getStack();
                if (is != null) {
                    handleMouseClick(guiInventory, slot, sourceSlot, destSlot, 7);
                }
            } else if (slot != null) {
                unused.add(slot);
            }
        }

        ItemStack[] layout = getInventoryLayout(container, INV_NORMAL_START, INV_NORMAL_END);
        List<Slot> result = null;
        if (container.getSlot(0).getStack() != null) {
            handleMouseClick(guiInventory, container.getSlot(0), 0, 0, 1);
            result = getNewlyOccupiedSlots(layout, container, INV_NORMAL_START, INV_NORMAL_END);
            result.addAll(unused);
        }

        for (int j = 0; j < targets.length; ++j) {
            int slotId = targets[j];
            if (container.getSlot(slotId).getStack() != null) {
                handleMouseClick(guiInventory, container.getSlot(slotId), slotId, 0, 1);
            }
        }

        return result;
    }

    public static List<Slot> applyClientSideCrafting(GuiContainer guiContainer, List<Slot> source)
    {
        EntityPlayer player = ClientHandler.getPlayer();
        GuiInventoryHelper guiInventory = new GuiInventoryHelper();
        Container playerContainer = guiInventory.inventorySlots;
        Container container = guiContainer.inventorySlots;
        ArrayList<Slot> playerSlots = new ArrayList<Slot>();
        ArrayList<Slot> containerSlots = new ArrayList<Slot>();
        for (int i = 0; i < source.size(); ++i) {
            Slot slot = source.get(i);
            if (slot != null && slot.inventory != player.inventory) {
                ItemStack is = slot.getStack();
                if (is != null && !is.isStackable() && !isInventoryFull(playerContainer, INV_NORMAL_START, INV_NORMAL_END)) {
                    int n = slot.slotNumber;
                    ItemStack[] layout1 = getInventoryLayout(playerContainer, INV_NORMAL_START, INV_NORMAL_END);
                    ItemStack[] layout2 = getInventoryLayout(container);
                    handleMouseClick(guiContainer, slot, n, 0, 1);
                    List<Slot> diff1 = getNewlyOccupiedSlots(layout1, playerContainer, INV_NORMAL_START, INV_NORMAL_END);
                    List<Slot> diff2 = getNewlyOccupiedSlots(layout2, container);
                    Slot moved1 = intersectSlot(diff1, is);
                    Slot moved2 = intersectSlot(diff2, is);
                    if (moved1 != null && moved2 != null) {
                        playerSlots.add(moved1);
                        containerSlots.add(moved2);
                    }
                }
            } else if (slot != null) {
                playerSlots.add(playerContainer.getSlot(slot.getSlotIndex()));
            } else {
                playerSlots.add(slot);
            }
        }

        boolean success = false;
        if (playerSlots.size() > 0) {
            ItemStack[] containerLayout = getInventoryLayout(guiContainer.inventorySlots);
            List<Slot> playerCrafted = applyClientSideCrafting(guiInventory, playerSlots);
            if (playerCrafted != null) {
                List<Slot> crafted = getNewlyOccupiedSlots(containerLayout, container);
                containerSlots.addAll(crafted);
                success = true;
            }
        }

        List<Slot> result = new ArrayList<Slot>();
        for (int i = 0; i < containerSlots.size(); ++i) {
            Slot slot = containerSlots.get(i);
            ItemStack is = slot.getStack();
            if (is != null && !isInventoryFull(container)) {
                int n = slot.slotNumber;
                ItemStack[] layout = getInventoryLayout(container);
                handleMouseClick(guiContainer, slot, n, 0, 1);
                List<Slot> diff = getNewlyOccupiedSlots(layout, container);
                result.addAll(diff);
            }
        }

        return success ? result : null;
    }

}
