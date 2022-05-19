package com.rick.rtfcadditions.core;

import com.rick.rtfcadditions.debug.DebugUtils;
import com.rick.rtfcadditions.Messenger;
import com.rick.rtfcadditions.RTFCAdditions;
import com.rick.rtfcadditions.api.ServerApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 *
 * @author Rick
 */
public class ServerHandler
{

    public static ServerHandler getInstance()
    {
        if (_instance == null) {
            _instance = new ServerHandler();
        }
        return _instance;
    }

    private static ServerHandler _instance = null;

    private final int ticksInterval = 13;
    private int ticks = 0;
    private long totalTicks = 0;
    private long stopServerAtTick = -1;
    private long stopServerMessageAtTick = -1;
    private boolean stopServerNeedsRestart = false;

    private List<String> runCommands = null;
    private final String endCommand = "&";

    private long lastSeenMtime = 0;
    private final long dropMtimeDiff = 5000;

    private volatile boolean runningStatusCheck = false;
    private volatile boolean needUpdateStatus = false;
    private final Object lock = new Object();

    private ServerHandler()
    {
    }

    public void cancelStopServer()
    {
        stopServerAtTick = -1;
        stopServerNeedsRestart = false;
        ChatComponentText text = new ChatComponentText("Server shutdown canceled!");
        text.getChatStyle().setBold(true).setColor(EnumChatFormatting.LIGHT_PURPLE);
        Messenger.send(text);
    }

    public void stopServer(int seconds, boolean restart)
    {
        DebugUtils.logInfo("Requested server shutdown!");
        stopServerAtTick = totalTicks + 20 * seconds;
        stopServerNeedsRestart = restart;
        sendShutdownMessage();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) {
            ticks++;
            if (ticks >= ticksInterval) {
                totalTicks += ticks;
                ticks = 0;
                handleStatusCheck();
                if (stopServerAtTick >= 0) {
                    handleShutdown();
                }
            }
        }
    }

    private void sendShutdownMessage()
    {
        String type = stopServerNeedsRestart ? "restarted" : "stopped";
        if (totalTicks <= stopServerMessageAtTick) {
            return;
        }
        if (stopServerAtTick >= 0 && stopServerAtTick > totalTicks) {
            int seconds = Math.round((stopServerAtTick - totalTicks) / 20.0f);
            ChatComponentText text = new ChatComponentText("Server will be " + type + " in " + seconds + " seconds!");
            text.getChatStyle().setBold(true).setColor(EnumChatFormatting.RED);
            Messenger.send(text);
        } else if (stopServerAtTick >= 0) {
            ChatComponentText text = new ChatComponentText("Server will be " + type + " NOW!");
            text.getChatStyle().setBold(true).setColor(EnumChatFormatting.RED);
            Messenger.send(text);
        }
        stopServerMessageAtTick = totalTicks;
    }

    private void handleShutdown()
    {
        if (stopServerAtTick >= 0 && stopServerAtTick > totalTicks) {
            int seconds1 = Math.round((stopServerAtTick - totalTicks) / 20.0f);
            int seconds2 = Math.round((stopServerAtTick - totalTicks - ticksInterval) / 20.0f);
            if (seconds1 != seconds2 && seconds1 % 5 == 0) {
                sendShutdownMessage();
            }
        } else if (stopServerAtTick >= 0) {
            sendShutdownMessage();
            if (stopServerNeedsRestart) {
                DebugUtils.logInfo("Server is now Restarting!");
                DebugUtils.logInfo("<RESTART_REQUESTED>");
            } else {
                DebugUtils.logInfo("Server is now Stopping!");
                DebugUtils.logInfo("<SHUTDOWN_REQUESTED>");
            }
            MinecraftServer server = MinecraftServer.getServer();
            server.initiateShutdown();
            stopServerAtTick = -1;
            stopServerNeedsRestart = false;
        }
    }

    private void handleUpdateStatus()
    {
        MinecraftServer server = MinecraftServer.getServer();
        if (runCommands != null) {
            for (String command : runCommands) {
                command = command.trim();
                if (command.equals(endCommand) || command.length() <= 0) {
                    continue;
                }
                try {
                    DebugUtils.logInfo("Executing command from external source: " + command);
                    server.getCommandManager().executeCommand(server, command);
                } catch (Exception ex) {
                    DebugUtils.logInfo(ex.toString());
                }
            }
            runCommands = null;
        }
    }

    private void handleStatusCheck()
    {
        boolean _needUpdateStatus;
        boolean _runningStatusCheck;
        synchronized (lock) {
            _needUpdateStatus = needUpdateStatus;
            _runningStatusCheck = runningStatusCheck;
            needUpdateStatus = false;
            runningStatusCheck = false;
        }
        if (_runningStatusCheck) {
            return;
        }
        if (_needUpdateStatus) {
            handleUpdateStatus();
        }
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean v = runStatusCheck();
                synchronized (lock) {
                    runningStatusCheck = false;
                    needUpdateStatus = v;
                }
            }
        }).start();
    }

    private boolean runStatusCheck()
    {
        File minecraftDir = ServerApi.getRootDirectory();
        if (minecraftDir != null) {
            File commandsFile = new File(minecraftDir, "run.server.commands");
            if (commandsFile.isFile()) {
                long mtime = commandsFile.lastModified();
                if (mtime > lastSeenMtime) {
                    long time = (new Date()).getTime();
                    int state = readCommands(commandsFile);
                    if (state == 1) {
                        mtime = commandsFile.lastModified();
                    }
                    if (state != 2 && state != 0 || time - mtime > dropMtimeDiff) {
                        lastSeenMtime = mtime;
                    }
                    return state > 0;
                }
            }
        }
        return false;
    }

    private int readCommands(File file)
    {
        String content;
        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            content = new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            DebugUtils.logWarn("Unable to read file " + file.getName());
            return -2;
        }
        if (content.length() > 0) {
            String[] lines = content.split("\\r?\\n");
            ArrayList<String> current = new ArrayList<>();
            ArrayList<String> extra = new ArrayList<>();
            boolean submit = false;
            for (String command : lines) {
                if (submit) {
                    extra.add(command);
                } else {
                    current.add(command);
                }
                if (command.trim().equals(endCommand)) {
                    submit = true;
                }
            }
            if (submit) {
                int state = 1;
                String newContent = String.join(System.lineSeparator(), extra);
                try {
                    if (newContent.trim().length() > 0) {
                        Files.write(file.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
                        state = 2;
                    } else {
                        Files.write(file.toPath(), new byte[0]);
                    }
                } catch (Exception ex) {
                    DebugUtils.logWarn("Unable to write file " + file.getName());
                    return -3;
                }
                runCommands = current;
                return state;
            }
            return 0;
        }
        return -1;
    }
}
