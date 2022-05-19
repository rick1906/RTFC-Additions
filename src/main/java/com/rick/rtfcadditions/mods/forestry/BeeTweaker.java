package com.rick.rtfcadditions.mods.forestry;

import binnie.genetics.genetics.AlleleHelper;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.EnumTolerance;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosome;
import forestry.api.genetics.IChromosomeType;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.BeeGenome;
import forestry.core.genetics.Chromosome;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Rick
 */
public abstract class BeeTweaker
{
    public static IChromosome[] transformForTesting(IChromosome[] chromosomes)
    {
        setActiveAllele(chromosomes, EnumBeeChromosome.TEMPERATURE_TOLERANCE, AlleleHelper.getAllele(EnumTolerance.BOTH_5));
        setActiveAllele(chromosomes, EnumBeeChromosome.HUMIDITY_TOLERANCE, AlleleHelper.getAllele(EnumTolerance.BOTH_5));
        setActiveAllele(chromosomes, EnumBeeChromosome.CAVE_DWELLING, AlleleHelper.getAllele(true));
        setActiveAllele(chromosomes, EnumBeeChromosome.NOCTURNAL, AlleleHelper.getAllele(true));
        return chromosomes;
    }

    public static IChromosome[] transformForFlower(IChromosome[] chromosomes, String flowerType)
    {
        setActiveAllele(chromosomes, EnumBeeChromosome.FLOWER_PROVIDER, AlleleHelper.getAllele(flowerType));
        return chromosomes;
    }

    public static IChromosome[] transformForEffect(IChromosome[] chromosomes, String effectType)
    {
        setActiveAllele(chromosomes, EnumBeeChromosome.EFFECT, AlleleHelper.getAllele(effectType));
        return chromosomes;
    }

    public static IBee transformForTesting(IBee bee)
    {
        IChromosome[] chromosomes = transformForTesting(getChromosomesCopy(bee));
        return getBee(chromosomes);
    }

    public static IBee transformForFlower(IBee bee, String flowerType)
    {
        IChromosome[] chromosomes = transformForFlower(getChromosomesCopy(bee), flowerType);
        return getBee(chromosomes);
    }

    public static IBee transformForEffect(IBee bee, String effectType)
    {
        IChromosome[] chromosomes = transformForEffect(getChromosomesCopy(bee), effectType);
        return getBee(chromosomes);
    }

    public static IBee transformBeeForTestingEffect(IBee bee)
    {
        IChromosome[] chromosomes = getChromosomesCopy(bee);
        setActiveAllele(chromosomes, EnumBeeChromosome.TEMPERATURE_TOLERANCE, AlleleHelper.getAllele(EnumTolerance.BOTH_5));
        setActiveAllele(chromosomes, EnumBeeChromosome.HUMIDITY_TOLERANCE, AlleleHelper.getAllele(EnumTolerance.BOTH_5));
        setActiveAllele(chromosomes, EnumBeeChromosome.CAVE_DWELLING, AlleleHelper.getAllele(true));
        setActiveAllele(chromosomes, EnumBeeChromosome.NOCTURNAL, AlleleHelper.getAllele(true));
        BeeGenome genome = new BeeGenome(chromosomes);
        return new Bee(genome);
    }

    public static IChromosome[] getChromosomesCopy(IBee bee)
    {
        IGenome genome = bee.getGenome();
        IChromosome[] chromosomes = genome.getChromosomes();
        IChromosome[] copy = new IChromosome[chromosomes.length];
        System.arraycopy(chromosomes, 0, copy, 0, copy.length);
        return copy;
    }

    public static IBee getBee(ItemStack item)
    {
        return BeeManager.beeRoot.getMember(item);
    }

    public static IBee getBee(IChromosome[] chromosomes)
    {
        return new Bee(new BeeGenome(chromosomes));
    }
    
    public static ItemStack getBeeStack(IBee bee, EnumBeeType type)
    {
        return BeeManager.beeRoot.getMemberStack(bee, type.ordinal());
    }
    
    public static ItemStack getBeeStack(IBee bee, ItemStack original)
    {
        EnumBeeType type = BeeManager.beeRoot.getType(original);
        return type != EnumBeeType.NONE ? getBeeStack(bee, type) : null;
    }

    public static IAllele getActiveAllele(IChromosome[] chromosomes, IChromosomeType chromosomeType)
    {
        return chromosomes[chromosomeType.ordinal()].getActiveAllele();
    }

    public static void setActiveAllele(IChromosome[] chromosomes, IChromosomeType chromosomeType, IAllele allele)
    {
        chromosomes[chromosomeType.ordinal()] = new Chromosome(allele);
    }
}
