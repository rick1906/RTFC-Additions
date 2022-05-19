package com.rick.rtfccore;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 *
 * @author Rick
 */
public class RTFCCoreModContainer extends DummyModContainer
{
    public static final String MODID = "rtfccoremod";
    public static final String VERSION = "0.1.1";

    public RTFCCoreModContainer()
    {
        super(new ModMetadata());
        ModMetadata metadata = getMetadata();
        metadata.modId = MODID;
        metadata.version = VERSION;
        metadata.name = "RTFCCoreMod";
        metadata.description = "RTFC ASM additions";
        metadata.url = "http://www.google.com/";
        metadata.authorList.add("Rick, Joseph C. Sible");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event)
    {

    }

    @Subscribe
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(this);
    }

}
