package com.rick.rtfcadditions.utils;

import codechicken.nei.ItemStackSet;
import codechicken.nei.api.API;
import codechicken.nei.api.ItemFilter;
import codechicken.nei.recipe.BrewingRecipeHandler;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.rick.rtfcadditions.debug.DebugUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;

/**
 *
 * @author Rick
 */
public abstract class PotionsTweaker
{
    private static HashMap potionRequirements = null;
    private static HashMap potionAmplifiers = null;
    private static final HashSet<String> possibleIngridiends = new HashSet<>();

    public static void initialize()
    {
        possibleIngridiends.clear();
        PotionHelper.checkFlag(0, 0);
        try {
            Field field = ReflectionHelper.findField(PotionHelper.class, "field_77927_l", "potionRequirements");
            potionRequirements = (HashMap)field.get(null);
            DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": field 'potionRequirements' found");
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            DebugUtils.logWarn(PotionsTweaker.class.getSimpleName() + ": field 'potionRequirements' not found");
        }
        try {
            Field field = ReflectionHelper.findField(PotionHelper.class, "field_77928_m", "potionAmplifiers");
            potionAmplifiers = (HashMap)field.get(null);
            DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": field 'potionAmplifiers' found");
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            DebugUtils.logWarn(PotionsTweaker.class.getSimpleName() + ": field 'potionAmplifiers' not found");
        }
    }

    public static void register()
    {
        resetPotions();
        registerPotionRequirements();
    }

    private static void registerPotionRequirements()
    {
        if (potionRequirements != null && potionAmplifiers != null) {
            potionRequirements.put(Potion.digSpeed.getId(), "0 & 1 & 2 & 3 & 3+6");
            possibleIngridiends.add("+0+1+2+3&4-4+13");
        }
    }

    private static Method findObjectMethod(Object obj, String[] names, Class<?>... params)
    {
        for (String name : names) {
            try {
                return obj.getClass().getMethod(name, params);
            } catch (NoSuchMethodException | SecurityException ex) {
            }
        }
        return null;
    }

    private static void resetPotions()
    {
        possibleIngridiends.addAll(Arrays.asList(new String[] {
            PotionHelper.blazePowderEffect,
            PotionHelper.fermentedSpiderEyeEffect,
            PotionHelper.field_151423_m,
            PotionHelper.ghastTearEffect,
            PotionHelper.glowstoneEffect,
            PotionHelper.goldenCarrotEffect,
            PotionHelper.gunpowderEffect,
            PotionHelper.magmaCreamEffect,
            PotionHelper.redstoneEffect,
            PotionHelper.speckledMelonEffect,
            PotionHelper.spiderEyeEffect,
            PotionHelper.sugarEffect
        }));
        for (Object obj : Item.itemRegistry) {
            if (obj instanceof Item) {
                Item item = (Item)obj;
                Class methodClass = Item.class;
                boolean searchDone = false;
                if (FMLCommonHandler.instance().getSide() != Side.SERVER) {
                    if (!item.getClass().equals(Item.class)) {
                        Method method = findObjectMethod(item, new String[] { "func_150896_i", "getPotionEffect" }, ItemStack.class);
                        if (method != null) {
                            methodClass = method.getDeclaringClass();
                        } else {
                            DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": failed to find getPotionEffect in class " + item.getClass().getName());
                        }
                    }
                    if (!methodClass.equals(Item.class)) {
                        DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": found overriden getPotionEffect in '" + item.getUnlocalizedName() + "'");
                        try {
                            searchDone = true;
                            LinkedList<ItemStack> subItems = new LinkedList<>();
                            item.getSubItems(item, null, subItems);
                            for (ItemStack stack : subItems) {
                                String effect = item.getPotionEffect(stack);
                                if (effect != null) {
                                    possibleIngridiends.add(effect);
                                    DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": found ingridient '" + effect + "' in '" + stack.getUnlocalizedName() + "'");
                                }
                            }
                            item.setPotionEffect(null);
                        } catch (Exception ex) {
                            searchDone = false;
                        }
                    }
                }
                if (!searchDone) {
                    String effect = item.getPotionEffect(new ItemStack(item));
                    if (effect != null) {
                        item.setPotionEffect(null);
                        possibleIngridiends.add(effect);
                        DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": found ingridient '" + effect + "' in '" + item.getUnlocalizedName() + "'");
                    }
                }
            }
        }
    }

    public static void searchPotions()
    {
        for (ItemStack is : BrewingRecipeHandler.ingredients.values()) {
            String effect = is.getItem().getPotionEffect(is);
            if (effect != null) {
                possibleIngridiends.add(effect);
            }
        }

        TreeSet<Integer> allPotions = new TreeSet<>();
        HashSet<Integer> searchPotions = new HashSet<>();
        searchPotions.add(0);
        allPotions.add(0);
        do {
            HashSet<Integer> newPotions = new HashSet<>();
            for (Integer basePotion : searchPotions) {
                if (ItemPotion.isSplash(basePotion)) {
                    continue;
                }
                for (String ingred : possibleIngridiends) {
                    int result = PotionHelper.applyIngredient(basePotion, ingred);
                    if (ItemPotion.isSplash(result)) {
                        addPotion(ingred, basePotion, result, allPotions, newPotions);
                        continue;
                    }
                    List<?> baseMods = Items.potionitem.getEffects(basePotion);
                    List<?> newMods = Items.potionitem.getEffects(result);
                    if (basePotion > 0 && baseMods == newMods
                        || baseMods != null && (baseMods.equals(newMods) || newMods == null)
                        || basePotion == result
                        || levelModifierChanged(basePotion, result)) {
                        continue;
                    }
                    addPotion(ingred, basePotion, result, allPotions, newPotions);
                }
            }
            searchPotions = newPotions;
        } while (!searchPotions.isEmpty());

        DebugUtils.logInfo(PotionsTweaker.class.getSimpleName() + ": found " + allPotions.size() + " potions");

        API.setItemListEntries(Items.potionitem, Iterables.transform(allPotions, new Function<Integer, ItemStack>()
        {
            @Override
            public ItemStack apply(Integer potionID)
            {
                return new ItemStack(Items.potionitem, 1, potionID);
            }
        }));

        API.addSubset("Items.Potions", new ItemStackSet().with(Items.potionitem));
        API.addSubset("Items.Potions.Splash", new ItemFilter()
        {
            @Override
            public boolean matches(ItemStack item)
            {
                return item.getItem() == Items.potionitem && (item.getItemDamage() & 0x4000) != 0;
            }
        });

        ItemStackSet positivepots = new ItemStackSet();
        ItemStackSet negativepots = new ItemStackSet();
        ItemStackSet neutralpots = new ItemStackSet();

        for (int potionID : allPotions) {
            List<PotionEffect> effectlist = Items.potionitem.getEffects(potionID);
            int type = 0;
            if (effectlist != null && !effectlist.isEmpty()) {
                for (PotionEffect potioneffect : effectlist) {
                    if (Potion.potionTypes[potioneffect.getPotionID()].isBadEffect()) {
                        type--;
                    } else {
                        type++;
                    }
                }
            }

            (type == 0 ? neutralpots : type > 0 ? positivepots : negativepots).add(new ItemStack(Items.potionitem, 1, potionID));
        }

        API.addSubset("Items.Potions.Positive", positivepots);
        API.addSubset("Items.Potions.Negative", negativepots);
        API.addSubset("Items.Potions.Neutral", neutralpots);
    }

    private static boolean levelModifierChanged(int basePotionID, int result)
    {
        int basemod = basePotionID & 0xE0;
        int resultmod = result & 0xE0;
        return basemod != 0 && basemod != resultmod;
    }

    private static void addPotion(String ingred, int basePotion, int result, TreeSet<Integer> allPotions, HashSet<Integer> newPotions)
    {
        if (allPotions.add(result)) {
            newPotions.add(result);
        }
    }
}
