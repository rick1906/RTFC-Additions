package com.rick.rtfcadditions.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Rick
 */
public class LoggingPool
{
    public static int APPEND = 0x00;
    public static int ROTATE = 0x01;
    public static int REWRITE = 0x02;

    private final HashMap<File, StreamWrapper> pool = new HashMap<>();
    private final Timer timer = new Timer();
    private long timeout = 15000L;
    private int zipAfter = 3;

    public LoggingPool()
    {
    }

    public void setZipAfter(int value)
    {
        zipAfter = value;
    }

    public int getZipAfter()
    {
        return zipAfter;
    }

    public void setTimeout(long milliseconds)
    {
        timeout = milliseconds;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public StreamWrapper get(File file, int mode) throws IOException
    {
        synchronized (pool) {
            StreamWrapper sw = pool.get(file);
            if (sw == null) {
                sw = new StreamWrapper(file, mode);
                pool.put(file, sw);
            }
            return sw;
        }
    }

    public boolean reset()
    {
        synchronized (pool) {
            boolean success = true;
            for (StreamWrapper sw : pool.values()) {
                try {
                    sw.close();
                } catch (IOException ex) {
                    success = false;
                }
            }
            pool.clear();
            return success;
        }
    }

    public boolean remove(File file)
    {
        synchronized (pool) {
            StreamWrapper sw = pool.remove(file);
            if (sw == null) {
                return false;
            }
            try {
                sw.close();
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }

    public boolean close(File file)
    {
        synchronized (pool) {
            StreamWrapper sw = pool.get(file);
            if (sw == null) {
                return false;
            }
            try {
                sw.close();
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }

    private boolean rotate(File file)
    {
        File dir = file.getParentFile();
        if (file.isFile() && dir.isDirectory()) {
            String name = file.getName();
            String baseName = name;
            String ending = "";
            int p = name.lastIndexOf(".");
            if (p >= 0) {
                baseName = name.substring(0, p);
                ending = name.substring(p);
            }
            return rotate(file, dir, baseName, ending, 1);
        }
        return false;
    }

    private boolean rotate(File file, File dir, String baseName, String ending, int index)
    {
        if (index > zipAfter) {
            return rotateZip(file, dir, baseName, ending);
        } else {
            File next = new File(dir, baseName + "." + index + ending);
            if (next.isFile() && !rotate(next, dir, baseName, ending, index + 1)) {
                return false;
            }
            return file.renameTo(next);
        }
    }

    private boolean rotateZip(File file, File dir, String baseName, String ending)
    {
        long mtime = file.lastModified();
        if (mtime <= 0) {
            return false;
        }
        int index = 0;
        String dateSuffix = (new SimpleDateFormat("yyyyMMdd")).format(new Date(mtime));
        do {
            index++;
            File gz = new File(dir, baseName + "." + dateSuffix + "-" + index + ending + ".gz");
            if (!gz.exists()) {
                return compressAndRotate(file, gz);
            }
        } while (index < 1000);
        return false;
    }

    private boolean compressAndRotate(File file, File gz)
    {
        try {
            compress(file, gz);
        } catch (IOException ex) {
            return false;
        }
        return file.delete();
    }

    private void compress(File file, File gz) throws IOException
    {
        byte[] buffer = new byte[4096];
        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(gz))) {
            try (FileInputStream input = new FileInputStream(file)) {
                int size;
                while ((size = input.read(buffer)) > 0) {
                    output.write(buffer, 0, size);
                }
            }
            output.finish();
        }
    }

    public class StreamWrapper
    {
        private final Object lock = new Object();
        private final File file;
        private boolean opened;
        private FileOutputStream stream;
        private Timeout timeout;

        private StreamWrapper(File file, int mode) throws IOException
        {
            this.file = file;
            this.opened = true;
            this.stream = openFile(file, mode);
            this.timeout = startTimer();
        }

        private FileOutputStream openFile(File file, int mode) throws IOException
        {
            if ((mode & ROTATE) == ROTATE) {
                rotate(file);
            }
            File dir = file.getParentFile();
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            return new FileOutputStream(file, (mode & REWRITE) != REWRITE);
        }

        private void resetTimer()
        {
            if (timeout != null) {
                timeout.cancel();
            }
            this.timeout = startTimer();
        }

        private Timeout startTimer()
        {
            return new Timeout(file);
        }

        private void reload() throws IOException
        {
            StreamWrapper sw = get(file, APPEND);
            if (sw != this && sw.stream != null) {
                throw new IllegalStateException("Stream is already closed");
            } else {
                this.stream = new FileOutputStream(file, true);
                resetTimer();
            }
        }

        public void write(byte[] bytes) throws IOException
        {
            synchronized (lock) {
                if (!opened) {
                    reload();
                }
                resetTimer();
                stream.write(bytes);
            }
        }

        public void writeAndFlush(byte[] bytes) throws IOException
        {
            synchronized (lock) {
                if (!opened) {
                    reload();
                }
                resetTimer();
                stream.write(bytes);
                stream.flush();
            }
        }

        public void flush() throws IOException
        {
            synchronized (lock) {
                if (!opened) {
                    reload();
                }
                resetTimer();
                stream.flush();
            }
        }

        public void close() throws IOException
        {
            synchronized (lock) {
                if (timeout != null) {
                    timeout.cancel();
                    timeout = null;
                }
                if (opened) {
                    opened = false;
                    stream.close();
                    stream = null;
                }
            }
        }

    }

    private class Timeout extends TimerTask
    {
        private final File file;

        public Timeout(File file)
        {
            this.file = file;
            this.schedule();
        }

        public final void schedule()
        {
            if (timeout > 0) {
                timer.schedule(this, timeout);
            }
        }

        @Override
        public void run()
        {
            close(file);
        }

    }
}
