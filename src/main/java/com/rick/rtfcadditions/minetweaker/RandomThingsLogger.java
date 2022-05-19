package com.rick.rtfcadditions.minetweaker;

import lumien.randomthings.Handler.ImbuingStation.ImbuingRecipe;
import lumien.randomthings.Handler.ImbuingStation.ImbuingRecipeHandler;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.player.IPlayer;
import minetweaker.api.server.ICommandFunction;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import modtweaker2.helpers.LogHelper;
import modtweaker2.helpers.StringHelper;

public class RandomThingsLogger implements ICommandFunction
{
    private static final List<String> validArguments = new LinkedList<String>();

    static {
        validArguments.add("Imbuing");
    }

    @Override
    public void execute(String[] arguments, IPlayer player)
    {
        List<String> args = StringHelper.toLowerCase(Arrays.asList(arguments));

        if (!validArguments.containsAll(args)) {
            if (player != null) {
                player.sendChat(MineTweakerImplementationAPI.platform.getMessage("Invalid arguments for command. Valid arguments: " + StringHelper.join(validArguments, ", ")));
            }
        } else {
            if (args.isEmpty() || args.contains("Imbuing")) {
                for (ImbuingRecipe recipe : ImbuingRecipeHandler.imbuingRecipes) {
                    MineTweakerAPI.logCommand(String.format("mods.randomthings.ImbuingStation.add(%s, %s, %s, %s, %s);",
                        LogHelper.getStackDescription(recipe.getResult()),
                        LogHelper.getStackDescription(recipe.toImbue()),
                        LogHelper.getStackDescription(recipe.getIngredients().get(0)),
                        LogHelper.getStackDescription(recipe.getIngredients().get(1)),
                        LogHelper.getStackDescription(recipe.getIngredients().get(2))));
                }
            }

            if (player != null) {
                player.sendChat(MineTweakerImplementationAPI.platform.getMessage("List generated; see minetweaker.log in your minecraft dir"));
            }
        }
    }
}
