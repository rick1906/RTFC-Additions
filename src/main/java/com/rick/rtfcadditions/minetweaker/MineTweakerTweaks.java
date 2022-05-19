package com.rick.rtfcadditions.minetweaker;

import com.google.common.collect.Lists;
import com.rick.rtfcadditions.api.ServerApi;
import com.rick.rtfcadditions.debug.DebugUtils;
import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.logger.FileLogger;
import minetweaker.runtime.ILogger;
import org.apache.logging.log4j.Level;

/**
 *
 * @author Rick
 */
public abstract class MineTweakerTweaks
{
    public static final List<String> currentErrors = Lists.newArrayList();
    public static final List<Throwable> currentExceptions = Lists.newArrayList();

    public static void register()
    {
        resetMainLoggers();
        MineTweakerAPI.registerClass(ImbuingStation.class);
    }

    public static void registerComponentLoggers()
    {
        if (MineTweakerAPI.server != null) {
            MineTweakerAPI.server.addMineTweakerCommand("randomthings", new String[] { "/minetweaker randomthings [HANDLER]", "    Outputs a list of all Random Things recipes." }, new RandomThingsLogger());
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static void resetMainLoggers()
    {
        boolean consoleLoggerFound = false;
        if (MineTweakerImplementationAPI.logger != null) {
            Field loggersField = null;
            List<ILogger> loggers = null;
            try {
                loggersField = MineTweakerImplementationAPI.logger.getClass().getDeclaredField("loggers");
            } catch (NoSuchFieldException | SecurityException ex) {
                DebugUtils.logWarn("Failed to find 'loggers' field in MineTweaker main logger: " + ex.getClass().getName());
            }
            if (loggersField != null) {
                try {
                    loggersField.setAccessible(true);
                    loggers = (List<ILogger>)loggersField.get(MineTweakerImplementationAPI.logger);
                } catch (Throwable ex) {
                    DebugUtils.logWarn("Failed to get 'loggers' field from MineTweaker main logger: " + ex.getClass().getName());
                }
            }
            if (loggers != null) {
                FileLogger logger = null;
                for (ILogger item : loggers) {
                    if (logger == null && item instanceof FileLogger) {
                        logger = (FileLogger)item;
                    }
                    if (item instanceof ConsoleLogger) {
                        consoleLoggerFound = true;
                    }
                }
                if (logger != null && loggers.remove(logger)) {
                    loggers.add(new MainLogger());
                    DebugUtils.logInfo("Successfully replaced MineTweaker file logger");
                }
            }
        }
        if (MineTweakerImplementationAPI.logger != null && !consoleLoggerFound) {
            MineTweakerImplementationAPI.logger.addLogger(new ConsoleLogger());
        }
    }

    private static File getLogFile()
    {
        File root = ServerApi.getRootDirectory();
        if (root != null) {
            return new File(root, "minetweaker.log");
        } else {
            return new File("minetweaker.log");
        }
    }

    private static File resetLogFile()
    {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    private static class MainLogger extends FileLogger
    {
        public MainLogger()
        {
            super(resetLogFile());
            logInfo("[" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "]");
        }

    }

    private static class ConsoleLogger implements ILogger
    {
        @Override
        public void logCommand(String string)
        {
        }

        @Override
        public void logInfo(String string)
        {
        }

        @Override
        public void logWarning(String string)
        {
            DebugUtils.logWarn("MineTweaker: " + string);
        }

        @Override
        public void logError(String string)
        {
            DebugUtils.log(Level.ERROR, "MineTweaker: " + string);
            currentErrors.add(string);
        }

        @Override
        public void logError(String string, Throwable thrwbl)
        {
            if (string != null && string.length() > 0) {
                DebugUtils.log(Level.ERROR, "MineTweaker: " + string);
                currentErrors.add(string);
            } else if (thrwbl != null) {
                currentErrors.add(thrwbl.getClass().getName());
            } else {
                currentErrors.add("NULL");
            }
            if (thrwbl != null) {
                DebugUtils.logException(thrwbl);
                currentExceptions.add(thrwbl);
            }
        }

    }
}
