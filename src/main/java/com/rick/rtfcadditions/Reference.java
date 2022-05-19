package com.rick.rtfcadditions;

/**
 *
 * @author Rick
 */
public class Reference
{
    public static final String MODID = "RTFCAdditions";
    public static final String NAME = "RTFC Additions";
    public static final String VERSION = "@MOD_VERSION@";

    public static final String DEPENDENCY_ID_TFC = "terrafirmacraft";
    public static final String DEPENDENCY_ID_FORESTRY = "Forestry";

    public static final String DEPENDENCIES = ""
        + "required-after:" + DEPENDENCY_ID_TFC + ";"
        + "required-after:" + DEPENDENCY_ID_FORESTRY;
}
