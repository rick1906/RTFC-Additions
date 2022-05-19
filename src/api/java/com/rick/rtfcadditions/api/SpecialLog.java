package com.rick.rtfcadditions.api;

import com.google.common.collect.Lists;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rick
 */
public abstract class SpecialLog
{
    private static final SimpleDateFormat defaultDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
    private static final SimpleDateFormat defaultDateFormatRotate = new SimpleDateFormat("HH:mm:ss");
    private static final List<SpecialLog> logs = Lists.newArrayList();
    private static Path logsDir = null;

    public static Path getDefaultLogsDirectory()
    {
        if (logsDir == null) {
            File root = ServerApi.getRootDirectory();
            if (root != null) {
                logsDir = Paths.get(root.getPath(), "debug", "logs");
            }
        }
        return logsDir;
    }

    public static SpecialLog getLog(String id)
    {
        for (SpecialLog log : logs) {
            if (log.matches(id)) {
                return log;
            }
        }
        return null;
    }

    private final String id;
    private final String[] shortcuts;
    private final List<LogWriter> writers;
    private boolean enabled = false;
    private boolean trace = false;

    public SpecialLog(String id, String... shortcuts)
    {
        this.id = id;
        this.shortcuts = shortcuts;
        this.writers = Lists.newArrayList();
        this.writers.add(null);
    }

    public SpecialLog register(boolean enable)
    {
        initialize();
        logs.add(this);
        if (enable) {
            enable();
        } else {
            disable();
        }
        return this;
    }

    public SpecialLog register()
    {
        initialize();
        logs.add(this);
        return this;
    }

    public boolean unregister()
    {
        return logs.remove(this);
    }

    protected abstract void initialize();

    public boolean matches(String id)
    {
        return this.id.equals(id) || shortcuts != null && Arrays.asList(shortcuts).contains(id);
    }

    public String id()
    {
        return id;
    }

    public Path getLogsDirectory()
    {
        return getDefaultLogsDirectory();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void enable()
    {
        enabled = true;
    }

    public void disable()
    {
        enabled = false;
    }

    public Group get()
    {
        return new Group(writers);
    }

    public Group get(String code)
    {
        return new Group(writers, code);
    }

    public Group get(String... codes)
    {
        return new Group(writers, codes);
    }

    private void write(List<LogWriter> writers, String n, String s, Level level, int dropTrace)
    {
        if (!enabled) {
            return;
        }

        StackTraceElement[] tr = null;
        if (trace && dropTrace >= 0) {
            Throwable t = new Throwable();
            tr = t.getStackTrace();
        }

        for (LogWriter lw : writers) {
            if (lw != null && lw.isEnabled(level)) {
                if (trace && lw.isTrace() && tr != null) {
                    lw.write(n, s, level, tr, getDropTrace(dropTrace + 1, tr));
                } else {
                    lw.write(n, s, level, null, 0);
                }
            }
        }
    }

    private int getDropTrace(int dropTrace, StackTraceElement[] trace)
    {
        String name = SpecialLog.class.getName();
        for (int i = dropTrace; i < trace.length; ++i) {
            if (!trace[i].getClassName().startsWith(name)) {
                return i;
            }
        }
        return dropTrace;
    }

    private void updateTraceState()
    {
        trace = false;
        for (LogWriter lw : writers) {
            if (lw != null && lw.isTrace()) {
                trace = true;
            }
        }
    }

    protected void registerMainLog(String name, Level level, int mode)
    {
        this.writers.set(0, new FileLogWriter("", name, level, mode));
    }

    protected void registerMainLog(String name, Level level)
    {
        registerMainLog(name, level, LoggingPool.APPEND);
    }

    protected void registerMainLog(String name)
    {
        registerMainLog(name, Level.ALL, LoggingPool.APPEND);
    }

    protected void registerSecondaryLog(String caterory, String name, Level level, int mode)
    {
        this.writers.add(new FileLogWriter(caterory, name, level, mode));
    }

    protected void registerSecondaryLog(String caterory, String name, Level level)
    {
        registerSecondaryLog(caterory, name, level, LoggingPool.APPEND);
    }

    protected void registerSecondaryLog(String caterory, String name)
    {
        registerSecondaryLog(caterory, name, Level.ALL, LoggingPool.APPEND);
    }

    protected void registerCategoryLog(String caterory, Level level, int mode)
    {
        this.writers.add(new CategoryLogWriter(caterory, level, mode));
    }

    protected void registerCategoryLog(String caterory, Level level)
    {
        registerCategoryLog(caterory, level, LoggingPool.APPEND);
    }

    protected void registerCategoryLog(String caterory)
    {
        registerCategoryLog(caterory, Level.ALL, LoggingPool.APPEND);
    }

    protected void registerFMLLog(String name, Level level)
    {
        this.writers.add(new FMLLogWriter(name, level));
    }

    protected void registerFMLLog(String name)
    {
        registerFMLLog(name, Level.ALL);
    }

    protected void registerFMLLogAsMain(String name, Level level)
    {
        this.writers.set(0, new FMLLogWriter(name, level));
    }

    protected void registerFMLLogAsMain(String name)
    {
        registerFMLLog(name, Level.ALL);
    }

    public class Group
    {
        private final List<LogWriter> writers;

        private Group(List<LogWriter> writers)
        {
            this.writers = writers;
        }

        private Group(List<LogWriter> writers, String pattern)
        {
            this.writers = Lists.newArrayList();
            for (LogWriter lw : writers) {
                if (lw != null && lw.matches(pattern)) {
                    this.writers.add(lw);
                }
            }
        }

        private Group(List<LogWriter> writers, String[] patterns)
        {
            this.writers = Lists.newArrayList();
            for (LogWriter lw : writers) {
                if (lw != null && lw.matches(patterns)) {
                    this.writers.add(lw);
                }
            }
        }

        public boolean isEmpty()
        {
            return writers.isEmpty();
        }

        public boolean isEnabled()
        {
            if (!enabled) {
                return false;
            }
            for (LogWriter lw : writers) {
                if (lw != null && lw.isEnabled()) {
                    return true;
                }
            }
            return false;
        }

        public void enable()
        {
            for (LogWriter lw : writers) {
                if (lw != null) {
                    lw.enable();
                }
            }
        }

        public void disable()
        {
            for (LogWriter lw : writers) {
                if (lw != null) {
                    lw.disable();
                }
            }
        }

        public void startTrace()
        {
            for (LogWriter lw : writers) {
                if (lw != null) {
                    lw.startTrace();
                }
            }
            updateTraceState();
        }

        public void stopTrace()
        {
            for (LogWriter lw : writers) {
                if (lw != null) {
                    lw.stopTrace();
                }
            }
            updateTraceState();
        }

        public void message(Level level, String n, String s)
        {
            write(writers, n, s, level, 1);
        }

        public void message(Level level, String s)
        {
            write(writers, null, s, level, 1);
        }

        public void warn(String n, String s)
        {
            write(writers, n, s, Level.WARN, 1);
        }

        public void warn(String s)
        {
            write(writers, null, s, Level.WARN, 1);
        }

        public void error(String n, String s)
        {
            write(writers, n, s, Level.ERROR, 1);
        }

        public void error(String s)
        {
            write(writers, null, s, Level.ERROR, 1);
        }

        public void info(String n, String s)
        {
            write(writers, n, s, Level.INFO, 1);
        }

        public void info(String s)
        {
            write(writers, null, s, Level.INFO, 1);
        }

        public void debug(String n, String s)
        {
            write(writers, n, s, Level.DEBUG, 1);
        }

        public void debug(String s)
        {
            write(writers, null, s, Level.DEBUG, 1);
        }

        public void trace(String n, String s)
        {
            write(writers, n, s, Level.TRACE, 1);
        }

        public void trace(String s)
        {
            write(writers, null, s, Level.TRACE, 1);
        }

    }

    private interface LogWriter
    {
        public boolean matches(String s);

        public boolean matches(String[] patterns);

        public void enable();

        public void disable();

        public void startTrace();

        public void stopTrace();

        public String getPathCode();

        public boolean isEnabled();

        public boolean isEnabled(Level level);

        public boolean isTrace();

        public void write(String n, String s, Level level, StackTraceElement[] trace, int dropTrace);
    }

    private abstract class BaseLogWriter implements LogWriter
    {
        protected final Level level;
        protected boolean enabled = true;
        protected boolean trace = false;

        public BaseLogWriter(Level level)
        {
            this.level = level;
        }

        @Override
        public void enable()
        {
            enabled = true;
        }

        @Override
        public void disable()
        {
            enabled = false;
        }

        @Override
        public void startTrace()
        {
            trace = true;
        }

        @Override
        public void stopTrace()
        {
            trace = false;
        }

        @Override
        public boolean isEnabled()
        {
            return enabled;
        }

        @Override
        public boolean isTrace()
        {
            return trace;
        }

        @Override
        public boolean isEnabled(Level level)
        {
            return enabled && level.isAtLeastAsSpecificAs(this.level);
        }

        @Override
        public abstract boolean matches(String s);

        @Override
        public boolean matches(String[] patterns)
        {
            for (String s : patterns) {
                if (matches(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class FMLLogWriter extends BaseLogWriter
    {
        private final String name;
        private final Logger log;

        public FMLLogWriter(String name, Level level)
        {
            super(level);
            this.name = name;
            this.log = LogManager.getLogger(name);
        }

        @Override
        public boolean matches(String s)
        {
            return (name + "/").startsWith(s + "/");
        }

        @Override
        public String getPathCode()
        {
            return name;
        }

        @Override
        public void write(String n, String s, Level level, StackTraceElement[] trace, int dropTrace)
        {
            log.log(level, s);
            if (trace != null) {
                log.log(level, "# Stack trace:");
                for (int i = dropTrace; i < trace.length; ++i) {
                    log.log(level, "#" + (i - dropTrace) + ": " + trace[i].toString());
                }
            }
        }
    }

    private class CategoryLogWriter extends BaseLogWriter
    {
        protected final String category;
        protected final int mode;
        private File directory = null;

        public CategoryLogWriter(String category, Level level, int mode)
        {
            super(level);
            this.category = category;
            this.mode = mode;
        }

        @Override
        public boolean matches(String s)
        {
            return (category + "/").startsWith(s + "/");
        }

        @Override
        public String getPathCode()
        {
            return category;
        }

        public File getTargetDirectory()
        {
            if (directory == null) {
                directory = initTargetDirectory();
            }
            return directory;
        }

        public Thread getThread()
        {
            return Thread.currentThread();
        }

        public SimpleDateFormat getDateFormat()
        {
            if (mode == LoggingPool.APPEND) {
                return defaultDateFormat;
            } else {
                return defaultDateFormatRotate;
            }
        }

        protected File initTargetDirectory()
        {
            Path root = getLogsDirectory();
            if (root != null) {
                String path = category.replace("/", File.separator);
                if (path.length() > 0) {
                    return root.resolve(path).toFile();
                } else {
                    return root.toFile();
                }
            } else {
                throw new NullPointerException("Logs root directory is invalid");
            }
        }

        @Override
        public void write(String n, String s, Level level, StackTraceElement[] trace, int dropTrace)
        {
            if (n != null && n.length() > 0) {
                File target = new File(getTargetDirectory(), n + ".log");
                LoggingProcessor.execute(new LoggingProcessor.LogCommand(target, mode, s, new Date(), getDateFormat(), getThread(), level, trace, dropTrace));
            }
        }
    }

    private class FileLogWriter extends CategoryLogWriter
    {
        protected final String name;
        private File file = null;

        public FileLogWriter(String category, String name, Level level, int mode)
        {
            super(category, level, mode);
            this.name = name;
        }

        @Override
        public boolean matches(String s)
        {
            return (getPathCode() + "/").startsWith(s + "/");
        }

        @Override
        public String getPathCode()
        {
            return category.length() > 0 ? (category + "/" + name) : name;
        }

        public File getTargetFile()
        {
            if (file == null) {
                file = new File(getTargetDirectory(), name + ".log");
            }
            return file;
        }

        @Override
        public void write(String n, String s, Level level, StackTraceElement[] trace, int dropTrace)
        {
            if (n != null && n.length() > 0) {
                s = "[" + n + "] " + s;
            }
            LoggingProcessor.execute(new LoggingProcessor.LogCommand(getTargetFile(), mode, s, new Date(), getDateFormat(), getThread(), level, trace, dropTrace));
        }

    }
}
