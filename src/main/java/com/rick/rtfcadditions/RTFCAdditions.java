package com.rick.rtfcadditions;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES)
public class RTFCAdditions
{
    public static final String CLIENT_PROXY_CLASS = "com.rick.rtfcadditions.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "com.rick.rtfcadditions.ServerProxy";
    
    @Mod.Instance(Reference.MODID)
    public static RTFCAdditions instance;

    @SidedProxy(clientSide = CLIENT_PROXY_CLASS, serverSide = SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.initializeMods(event);
    }

    @EventHandler
    public void initialize(FMLInitializationEvent event)
    {
        proxy.registerEvents();
        proxy.registerPackets();
        proxy.initializeMods(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.applyTweaks();
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        proxy.registerCommands(event);
        proxy.applyTweaksOnStartup(event);
    }
    
    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
        proxy.onServerShutdown(event);
    }

}
