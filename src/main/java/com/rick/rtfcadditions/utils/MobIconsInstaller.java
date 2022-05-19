package com.rick.rtfcadditions.utils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Rick
 */
public class MobIconsInstaller
{
    private static final long EMPTY_ICON_SIZE = 868;

    public void install(File configDirectory)
    {
        File rootDirectory = configDirectory.getParentFile();
        if (rootDirectory.exists() && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            File jmapDirectory = new File(rootDirectory, "journeymap");
            File iconsDirectory = new File(new File(new File(jmapDirectory, "icon"), "entity"), "2D");
            File tfcIconsDirectory = new File(new File(new File(iconsDirectory, "terrafirmacraft"), "textures"), "mob");
            installTfcIcons(tfcIconsDirectory);
        }
    }

    public void installTfcIcons(File directory)
    {
        installIcon("Bear.png", directory);
    }

    private boolean installIcon(String name, File destDirectory)
    {
        URL inputUrl = getClass().getResource("/assets/rtfcadditions/mobicons/" + name);
        File dest = new File(destDirectory, name);
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        if (dest.exists() && dest.length() != EMPTY_ICON_SIZE) {
            return false;
        }
        try {
            FileUtils.copyURLToFile(inputUrl, dest);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

}
