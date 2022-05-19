package com.rick.rtfcadditions;

import com.rick.rtfcadditions.core.ClientHandler;
import com.rick.rtfcadditions.debug.DebugUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

/**
 *
 * @author Rick
 */
public class Messenger
{
    public static final boolean DEBUG = true;

    public static void write(String message)
    {
        DebugUtils.print(message);
    }

    public static void debug(String message)
    {
        if (DEBUG) {
            write(message);
        }
    }

    public static void debugSend(String message)
    {
        if (DEBUG) {
            write(message);
            send(message);
        }
    }

    public static IChatComponent format(IChatComponent text, EnumChatFormatting color, boolean bold, boolean italic)
    {
        text.getChatStyle().setColor(color).setBold(bold).setItalic(italic);
        return text;
    }

    public static IChatComponent format(IChatComponent text, EnumChatFormatting color, boolean bold)
    {
        text.getChatStyle().setColor(color).setBold(bold);
        return text;
    }

    public static IChatComponent format(IChatComponent text, EnumChatFormatting color)
    {
        text.getChatStyle().setColor(color);
        return text;
    }

    public static IChatComponent format(IChatComponent text, boolean bold, boolean italic)
    {
        text.getChatStyle().setBold(bold).setItalic(italic);
        return text;
    }

    public static IChatComponent format(String text, EnumChatFormatting color, boolean bold, boolean italic)
    {
        return format(new ChatComponentText(text), color, bold, italic);
    }

    public static IChatComponent format(String text, EnumChatFormatting color, boolean bold)
    {
        return format(new ChatComponentText(text), color, bold);
    }

    public static IChatComponent format(String text, EnumChatFormatting color)
    {
        return format(new ChatComponentText(text), color);
    }

    public static IChatComponent format(String text, boolean bold, boolean italic)
    {
        return format(new ChatComponentText(text), bold, italic);
    }

    public static boolean send(IChatComponent text)
    {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            MinecraftServer server = MinecraftServer.getServer();
            if (server != null) {
                server.getConfigurationManager().sendChatMsg(text);
                return true;
            }
        } else {
            ClientHandler.getPlayer().addChatMessage(text);
            return true;
        }
        return false;
    }

    public static boolean send(String text)
    {
        return send(new ChatComponentText(text));
    }

    public static void send(ICommandSender player, IChatComponent text)
    {
        player.addChatMessage(text);
    }

    public static void send(ICommandSender player, String text)
    {
        player.addChatMessage(new ChatComponentText(text));
    }

    public static boolean sendInfo(IChatComponent text)
    {
        return send(format(text, EnumChatFormatting.GRAY, false, true));
    }

    public static boolean sendInfo(String text)
    {
        return sendInfo(new ChatComponentText(text));
    }

    public static void sendInfo(ICommandSender player, IChatComponent text)
    {
        player.addChatMessage(format(text, EnumChatFormatting.GRAY, false, true));
    }

    public static void sendInfo(ICommandSender player, String text)
    {
        sendInfo(player, new ChatComponentText(text));
    }

    public static boolean sendWarn(IChatComponent text)
    {
        return send(format(text, EnumChatFormatting.LIGHT_PURPLE, false, true));
    }

    public static boolean sendWarn(String text)
    {
        return sendWarn(new ChatComponentText(text));
    }

    public static void sendWarn(ICommandSender player, IChatComponent text)
    {
        player.addChatMessage(format(text, EnumChatFormatting.LIGHT_PURPLE, false, true));
    }

    public static void sendWarn(ICommandSender player, String text)
    {
        sendWarn(player, new ChatComponentText(text));
    }

}
