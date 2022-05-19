package com.rick.rtfccore.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;

/**
 *
 * @author ???
 */
public abstract class InfiniteFluidsHooks
{
    public static boolean fluidIsInfinite(Block block, World world)
    {
        if (world.provider.isHellWorld) {
            return block.getMaterial() == Material.lava;
        }
        return block.getMaterial() == Material.water;
    }

    public static void maybeCreateSourceBlock(BlockFluidClassic block, World world, int x, int y, int z)
    {
        if (!block.isSourceBlock(world, x, y, z) && fluidIsInfinite(block, world)) {
            int adjacentSourceBlocks
                = (block.isSourceBlock(world, x - 1, y, z) ? 1 : 0)
                + (block.isSourceBlock(world, x + 1, y, z) ? 1 : 0)
                + (block.isSourceBlock(world, x, y, z - 1) ? 1 : 0)
                + (block.isSourceBlock(world, x, y, z + 1) ? 1 : 0);
            int densityDir = BlockFluidClassic.getDensity(world, x, y, z) > 0 ? -1 : 1;
            if (adjacentSourceBlocks >= 2 && (world.getBlock(x, y + densityDir, z).getMaterial().isSolid() || block.isSourceBlock(world, x, y + densityDir, z))) {
                world.setBlockMetadataWithNotify(x, y, z, 0, 3); // 0: source block. 3: block update and notify clients
            }
        }
    }
}
