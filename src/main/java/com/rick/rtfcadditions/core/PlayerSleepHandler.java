package com.rick.rtfcadditions.core;

import com.bioxx.tfc.Core.TFC_Core;
import com.bioxx.tfc.Core.TFC_Time;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.util.ChatComponentTranslation;

/**
 *
 * @author Rick
 */
public class PlayerSleepHandler
{
    private static final int TIME_LIMIT_SECONDS = 300;
    private static final int SLEEP_UPDATE_PERIOD = 20;
    private static final boolean DEBUG = false;

    private int sleeping = 0;
    private int sleepingTicker = 0;
    private boolean sleepingCancel = false;
    private boolean sleepingNeedsUpdate = false;
    private double lastLogoutsUpdate = 0;
    private final HashMap<String, Double> logouts = new HashMap<String, Double>();
    
    private void writeInfo(String message)
    {
        System.out.println(this.getClass().getSimpleName() + " " + message);
    }

    private void writeDebug(String message)
    {
        if (DEBUG) {
            System.out.println(this.getClass().getSimpleName() + " " + message);
        }
    }

    private int countSleepingPlayers(World world)
    {
        int count = 0;
        writeDebug("player count is " + world.playerEntities.size());
        for (final Object playerObj : world.playerEntities) {
            if (playerObj instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer)playerObj;
                if (player.isPlayerSleeping()) {
                    writeDebug("found sleeping player");
                    count++;
                } else {
                    writeDebug("found player");
                }
            }
        }
        return count;
    }

    private void displaySleepingMessage(World world)
    {
        for (final Object playerObj : world.playerEntities) {
            if (playerObj instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer)playerObj;
                displaySleepingMessage(player);
            }
        }
    }

    private void displaySleepingMessage(EntityPlayer player)
    {
        writeDebug("message");
        if (sleepingCancel && player.isPlayerSleeping() && isNormalDimension(player.worldObj)) {
            String[] left = getLeftPlayerNames(player.worldObj);
            if (left.length > 2) {
                TFC_Core.sendInfoMessage(player, new ChatComponentTranslation("sleeping.cancel.var3"));
            } else if (left.length == 2) {
                TFC_Core.sendInfoMessage(player, new ChatComponentTranslation("sleeping.cancel.var2", left[0], left[1]));
            } else if (left.length == 1) {
                TFC_Core.sendInfoMessage(player, new ChatComponentTranslation("sleeping.cancel.var1", left[0]));
            }
        }
    }

    private void updateSleeping(World world)
    {
        writeDebug("update sleeping");
        if (isNormalDimension(world)) {
            boolean wasCancelled = sleepingCancel;
            sleeping = countSleepingPlayers(world);
            writeDebug("sleeping = " + sleeping);
            if (sleeping > 0) {
                String[] left = getLeftPlayerNames(world);
                if (left.length >= sleeping) {
                    sleepingCancel = true;
                } else {
                    sleepingCancel = false;
                }
            } else {
                sleepingCancel = false;
            }
            if (!wasCancelled && sleepingCancel) {
                displaySleepingMessage(world);
                writeInfo("sleeping cancel start");
            } else if (wasCancelled && !sleepingCancel) {
                writeInfo("sleeping cancel stop");
            }
        }
    }

    private void setSleepingNeedsUpdate(World world)
    {
        if (isNormalDimension(world)) {
            sleepingNeedsUpdate = true;
        }
    }

    private boolean isNormalDimension(World world)
    {
        return world.provider.dimensionId == 0;
    }
    
    private double getCurrentTimeSeconds(World world)
    {
        return (double)System.currentTimeMillis() / 1000;
    }
    
    private boolean isNight()
    {
        int h = TFC_Time.getHour();
        return h >= 18 || h <= 5;
    }
    
    private boolean needsRegisterLogout(World world)
    {
        return isNormalDimension(world) && isNight();
    }

    private String[] getLeftPlayerNames(World world)
    {
        updateLogouts(world);
        return logouts.keySet().toArray(new String[0]);
    }

    private void updateLogouts(World world)
    {
        if (!isNormalDimension(world)) {
            return;
        }

        double time = getCurrentTimeSeconds(world);
        if (time <= lastLogoutsUpdate) {
            return;
        } else {
            lastLogoutsUpdate = time;
        }

        ArrayList<String> toDelete = new ArrayList<String>();
        double timeLimit = time - TIME_LIMIT_SECONDS;
        for (final HashMap.Entry<String, Double> entry : logouts.entrySet()) {
            if (entry.getValue() < timeLimit) {
                toDelete.add(entry.getKey());
            }
        }
        for (String key : toDelete) {
            logouts.remove(key);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerSleepInBedEvent(final PlayerSleepInBedEvent event)
    {
        EntityPlayer player = event.entityPlayer;
        if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
            writeDebug("in bed event " + event.result);
            setSleepingNeedsUpdate(player.worldObj);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerWakeUpEvent(final PlayerWakeUpEvent event)
    {
        EntityPlayer player = event.entityPlayer;
        if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
            writeDebug("wake up event");
            setSleepingNeedsUpdate(player.worldObj);
        }
    }

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event)
    {
        if (sleeping > 0 && sleepingCancel && event.phase == TickEvent.Phase.END) {
            writeDebug("tick event");
            EntityPlayer player = event.player;
            if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
                if (player.sleepTimer > 50) {
                    player.sleepTimer = 50;
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTickEvent(TickEvent.WorldTickEvent event)
    {
        if (sleepingNeedsUpdate && isNormalDimension(event.world) && event.phase == TickEvent.Phase.START) {
            sleepingNeedsUpdate = false;
            updateSleeping(event.world);
        } else if (sleepingCancel) {
            sleepingTicker++;
            if (sleepingTicker >= SLEEP_UPDATE_PERIOD) {
                sleepingTicker = 0;
                updateSleeping(event.world);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        EntityPlayer player = event.player;
        if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
            String name = player.getDisplayName();
            logouts.remove(name);
            updateLogouts(player.worldObj);
            updateSleeping(player.worldObj);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        EntityPlayer player = event.player;
        if (player != null && player.worldObj != null && !player.worldObj.isRemote) {
            if (needsRegisterLogout(player.worldObj)) {
                String name = player.getDisplayName();
                logouts.put(name, getCurrentTimeSeconds(player.worldObj));
            }
            updateLogouts(player.worldObj);
            updateSleeping(player.worldObj);
        }
    }
}
