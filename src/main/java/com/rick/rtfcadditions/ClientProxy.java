package com.rick.rtfcadditions;

import com.rick.rtfcadditions.core.ClientHandler;
import com.rick.rtfcadditions.utils.MobIconsInstaller;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 *
 * @author Rick
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void initializeMods(FMLPreInitializationEvent event)
    {
        super.initializeMods(event);
        (new MobIconsInstaller()).install(event.getModConfigurationDirectory());
    }

    @Override
    public void registerEvents()
    {
        super.registerEvents();
        registerEventHandler(ClientHandler.getInstance(), true, false);
    }

}
