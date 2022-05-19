package com.rick.rtfcadditions.api;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import java.io.File;

/**
 *
 * @author Rick
 */
public final class ServerApi
{
    private static File configDir;
    private static File rootDir;

    private ServerApi()
    {
    }

    public static File getModConfigurationDirectory()
    {
        return configDir;
    }

    public static File getRootDirectory()
    {
        return rootDir;
    }

    public static void initialize(FMLPreInitializationEvent event)
    {
        configDir = event.getModConfigurationDirectory();
        rootDir = configDir != null ? configDir.getParentFile() : null;
    }

    public static void initializeServer(FMLServerStartingEvent event)
    {

    }
}
