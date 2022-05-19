package com.rick.rtfcadditions.api;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rick
 */
public final class LoggingProcessor
{
    private static Thread thread = null;
    private static LoggingProcessor instance = null;

    private static void initialize()
    {
        instance = new LoggingProcessor();
        thread = new Thread(instance.runner);
        thread.setDaemon(true);
        thread.start();
    }

    public static LoggingProcessor getInstance()
    {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    public static Thread getThread()
    {
        if (thread == null) {
            initialize();
        }
        return thread;
    }

    public static LoggingPool getPool()
    {
        return getInstance().pool;
    }

    public static void execute(Command command)
    {
        getInstance().queueCommand(command);
    }

    private final LoggingPool pool = new LoggingPool();
    private final ArrayDeque<Command> queue = new ArrayDeque<>();
    private final Runner runner = new Runner();
    private final Object lock = new Object();

    private LoggingProcessor()
    {
    }

    public void queueCommand(Command command)
    {
        synchronized (lock) {
            queue.add(command);
            lock.notify();
        }
    }

    private void runCommand(Command command, boolean isLast)
    {
        command.run(pool, isLast);
    }

    private class Runner implements Runnable
    {
        @Override
        public void run()
        {
            while (true) {
                Command command;
                synchronized (lock) {
                    command = queue.poll();
                    if (command == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                if (command != null) {
                    runCommand(command, queue.isEmpty());
                }
            }
        }
    }

    public interface Command
    {
        public void run(LoggingPool pool, boolean isLast);
    }

    public static class LogCommand implements Command
    {
        private final File file;
        private final int mode;
        private final SimpleDateFormat dateFromat;
        private final String message;
        private final Date date;
        private final Thread thread;
        private final Level level;
        private final StackTraceElement[] trace;
        private final int dropTrace;

        public LogCommand(File file, int mode, String message, Date date, SimpleDateFormat dateFormat, Thread thread, Level level, StackTraceElement[] trace, int dropTrace)
        {
            this.file = file;
            this.mode = mode;
            this.message = message;
            this.date = date;
            this.dateFromat = dateFormat;
            this.thread = thread;
            this.level = level;
            this.trace = trace;
            this.dropTrace = dropTrace;
        }

        protected String getMessagePrefix()
        {
            return "[" + dateFromat.format(date) + " | " + thread.getName() + "/" + level.toString() + "]: ";
        }

        protected void buildMessage(StringBuilder sb)
        {
            sb.append(getMessagePrefix());
            sb.append(message);
            sb.append(System.lineSeparator());
        }

        protected void buildMessage(StringBuilder sb, StackTraceElement[] trace, int dropTrace)
        {
            String prefix = getMessagePrefix();
            sb.append(prefix);
            sb.append(message);
            sb.append(System.lineSeparator());
            int length = prefix.length();
            String space = new String(new char[length]).replace("\0", " ");
            sb.append(space);
            sb.append("Stack trace:");
            sb.append(System.lineSeparator());
            for (int i = dropTrace; i < trace.length; ++i) {
                sb.append(space);
                sb.append("#");
                sb.append(String.valueOf(i - dropTrace));
                sb.append(": ");
                sb.append(trace[i].toString());
                sb.append(System.lineSeparator());
            }
        }

        @Override
        public void run(LoggingPool pool, boolean isLast)
        {
            try {
                StringBuilder sb = new StringBuilder();
                if (trace != null) {
                    buildMessage(sb, trace, dropTrace);
                } else {
                    buildMessage(sb);
                }
                if (isLast) {
                    pool.get(file, mode).writeAndFlush(sb.toString().getBytes());
                } else {
                    pool.get(file, mode).write(sb.toString().getBytes());
                }
            } catch (IOException ex) {
                Logger logger = LogManager.getLogger(LoggingProcessor.class);
                logger.warn("Caught exception when trying to write to log file " + file.getPath());
                logger.catching(Level.WARN, ex);
            }
        }
    }

}
