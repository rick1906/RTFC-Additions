package com.rick.rtfccore;

import com.rick.rtfccore.transformers.InfiniteFluidsClassTransformer;
import com.rick.rtfccore.transformers.NEIPotionsClassTransformer;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Rick
 */
@MCVersion(value = "1.7.10")
public class RTFCLoadingPlugin implements IFMLLoadingPlugin
{
    public static boolean runtimeDeobfuscation;
    public static File location;

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {
            InfiniteFluidsClassTransformer.class.getName(),
            NEIPotionsClassTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass()
    {
        return RTFCCoreModContainer.class.getName();
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        runtimeDeobfuscation = (Boolean)data.get("runtimeDeobfuscationEnabled");
        location = (File)data.get("coremodLocation");
        initializeTransformers();
    }

    private void initializeTransformers()
    {
        InfiniteFluidsClassTransformer.setObfuscated(runtimeDeobfuscation);
    }

}
