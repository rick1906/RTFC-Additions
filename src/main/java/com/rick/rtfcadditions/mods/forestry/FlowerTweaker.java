package com.rick.rtfcadditions.mods.forestry;

import com.bioxx.tfc.api.TFCBlocks;
import forestry.api.apiculture.FlowerManager;
import forestry.api.genetics.IFlower;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

/**
 *
 * @author Rick
 */
public abstract class FlowerTweaker
{
    public static void registerDefaultPatches()
    {
        // mushrooms
        registerFlower(TFCBlocks.fungi, 0, 1, FlowerManager.FlowerTypeMushrooms);

        // cactus
        registerFlower(TFCBlocks.cactus, FlowerManager.FlowerTypeCacti);

        // jungle
        registerFlower(TFCBlocks.vine, FlowerManager.FlowerTypeJungle); // liana
        registerFlower(TFCBlocks.sapling, 15, FlowerManager.FlowerTypeJungle); // kapok sapling
        registerFlower(TFCBlocks.leaves, 15, FlowerManager.FlowerTypeJungle); // kapok leaves

        // gourd
        registerFlower(TFCBlocks.berryBush, FlowerManager.FlowerTypeGourd); // vegetables
        registerFlower(TFCBlocks.pumpkin, FlowerManager.FlowerTypeGourd); // pumpkin

        // wheat
        registerFlower(TFCBlocks.crops, FlowerManager.FlowerTypeWheat);

        // vanilla
        registerFlower(TFCBlocks.flowers, 0, 5, FlowerManager.FlowerTypeVanilla);
        registerFlower(TFCBlocks.flowers2, 0, 8, FlowerManager.FlowerTypeVanilla);

        // end
        registerFlower(TFCBlocks.tallGrass, 1, FlowerManager.FlowerTypeEnd); // paportnik
    }

    public static boolean registerFlower(Block block, String... flowerTypes)
    {
        return registerFlower(block, OreDictionary.WILDCARD_VALUE, flowerTypes);
    }

    public static boolean registerFlower(Block block, int meta, String... flowerTypes)
    {
        double weight = meta == OreDictionary.WILDCARD_VALUE ? 0.0 : 1.0;
        return registerFlower(block, meta, weight, flowerTypes);
    }

    public static boolean registerFlower(Block block, int metaFrom, int metaTo, String... flowerTypes)
    {
        boolean result = false;
        for (int meta = metaFrom; meta <= metaTo; ++meta) {
            result = registerFlower(block, meta, flowerTypes) || result;
        }
        return result;
    }

    public static boolean registerFlower(Block block, int metaFrom, int metaTo, double weight, String... flowerTypes)
    {
        boolean result = false;
        for (int meta = metaFrom; meta <= metaTo; ++meta) {
            result = registerFlower(block, meta, weight, flowerTypes) || result;
        }
        return result;
    }

    public static boolean registerFlower(Block block, int meta, double weight, String... flowerTypes)
    {
        ArrayList<String> toRegister = new ArrayList<>();
        for (String flower : flowerTypes) {
            Set<IFlower> fls = FlowerManager.flowerRegistry.getAcceptableFlowers(flower);
            boolean exists = false;
            for (IFlower fl : fls) {
                if (fl.getBlock() == block && (fl.getMeta() == meta || fl.getMeta() == OreDictionary.WILDCARD_VALUE)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                toRegister.add(flower);
            }
        }
        if (toRegister.isEmpty()) {
            return false;
        } else if (weight > 0 && meta != OreDictionary.WILDCARD_VALUE) {
            FlowerManager.flowerRegistry.registerPlantableFlower(block, meta, weight, toRegister.toArray(new String[0]));
            return true;
        } else {
            FlowerManager.flowerRegistry.registerAcceptableFlower(block, meta, toRegister.toArray(new String[0]));
            return true;
        }
    }

}
