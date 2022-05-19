package com.rick.rtfcadditions.utils;

import codechicken.nei.api.ItemInfo;
import com.rick.rtfcadditions.debug.DebugUtils;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public abstract class NEITweaker
{
    public static void applyTweaks()
    {
        for (ItemStack key : ItemInfo.hiddenItems.keys()) {
            ItemStack val = ItemInfo.hiddenItems.get(key);
            DebugUtils.logDebug("NEI Hidden: " + key.getUnlocalizedName() + " | " + val.getItem().getClass().getName());
        }
    }
}
