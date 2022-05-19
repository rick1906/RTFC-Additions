package com.rick.rtfcadditions;

import com.bioxx.tfc.TerraFirmaCraft;
import com.rick.rtfcadditions.api.LoggingProcessor;
import com.rick.rtfcadditions.api.ServerApi;
import com.rick.rtfcadditions.commands.CommandRTFC;
import com.rick.rtfcadditions.core.EntityHandler;
import com.rick.rtfcadditions.core.PlayerInteractionHandler;
import com.rick.rtfcadditions.core.ServerHandler;
import com.rick.rtfcadditions.minetweaker.MineTweakerTweaks;
import com.rick.rtfcadditions.packets.CustomTransactionPacket;
import com.rick.rtfcadditions.packets.RequestContainerUpdatePacket;
import com.rick.rtfcadditions.mods.forestry.FlowerTweaker;
import com.rick.rtfcadditions.utils.ItemsTweaker;
import com.rick.rtfcadditions.utils.NEITweaker;
import com.rick.rtfcadditions.utils.PotionsTweaker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 *
 * @author Rick
 */
public class CommonProxy
{
    public void initializeMods(FMLPreInitializationEvent event)
    {
        ServerApi.initialize(event);
        PotionsTweaker.initialize();
    }

    public void initializeMods(FMLInitializationEvent event)
    {
        FlowerTweaker.registerDefaultPatches();
        MineTweakerTweaks.register();
    }

    public void registerEvents()
    {
        registerEventHandler(new PlayerInteractionHandler(), true, false);
        registerEventHandler(new EntityHandler(), true, false);
        registerEventHandler(ServerHandler.getInstance(), false, true);
    }

    public void registerPackets()
    {
        TerraFirmaCraft.PACKET_PIPELINE.registerPacket(CustomTransactionPacket.class);
        TerraFirmaCraft.PACKET_PIPELINE.registerPacket(RequestContainerUpdatePacket.class);
    }

    public void registerCommands(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandRTFC());
    }

    public void applyTweaks()
    {
        ItemsTweaker.applyTweaks();
        PotionsTweaker.register();
    }

    public void applyTweaksOnStartup(FMLServerStartingEvent event)
    {
        ServerApi.initializeServer(event);
        MineTweakerTweaks.registerComponentLoggers();
        NEITweaker.applyTweaks();
    }

    public void onServerShutdown(FMLServerStoppedEvent event)
    {
        LoggingProcessor.getPool().reset();
    }

    protected final void registerEventHandler(Object target, boolean forgeBus, boolean fmlBus)
    {
        if (forgeBus) {
            MinecraftForge.EVENT_BUS.register(target);
        }
        if (fmlBus) {
            FMLCommonHandler.instance().bus().register(target);
        }
    }
}
