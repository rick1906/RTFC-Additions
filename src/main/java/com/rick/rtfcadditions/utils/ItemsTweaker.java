package com.rick.rtfcadditions.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;

/**
 *
 * @author Rick
 */
public abstract class ItemsTweaker
{
    public static void applyTweaks()
    {
        for (Object obj : Item.itemRegistry) {
            if (obj instanceof Item) {
                Item item = (Item)obj;
                if (item instanceof ItemPotion && item.getItemStackLimit() < 16) {
                    item.setMaxStackSize(16);
                }
            }
        }
    }
}
