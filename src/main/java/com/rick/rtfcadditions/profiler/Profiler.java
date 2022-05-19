package com.rick.rtfcadditions.profiler;

import com.google.common.collect.Lists;
import com.rick.rtfcadditions.RTFCAdditions;
import com.rick.rtfcadditions.api.ServerApi;
import com.rick.rtfcadditions.debug.DebugUtils;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import mcp.mobius.mobiuscore.profiler.DummyProfiler;
import mcp.mobius.mobiuscore.profiler.ProfilerSection;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 *
 * @author Rick
 */
public class Profiler
{
    private final TickProfiler tickProfiler = new TickProfiler();
    private final HashMap<ProfilerSection, ProfilerStatsComponent> components = new HashMap<>();

    private final PeriodStats globalStats = new PeriodStats();
    private final PeriodStats.ReportSettings settingsReportTotal = new PeriodStats.ReportSettings();
    private final PeriodStats.ReportSettings settingsReportPeriodic = new PeriodStats.ReportSettings(-1);
    private final PeriodStats.ReportSettings settingsReportLog = new PeriodStats.ReportSettings(1);

    private long totalTicks = 0;
    private long totalTicksLimit = -1;
    private long startTimestamp = -1;
    private long profilerThreadId = -1;
    private boolean profilerStarted = false;

    private PrintWriter logWriter = null;
    private boolean logWriterLoaded = false;

    private static Profiler instance = null;

    public static Profiler getProfiler()
    {
        if (instance == null) {
            instance = new Profiler();
        }
        return instance;
    }

    public Profiler()
    {
        components.put(ProfilerSection.DIMENSION_TICK, new WorldTickProfiler());
        components.put(ProfilerSection.DIMENSION_BLOCKTICK, new BlockUpdateProfiler());
        components.put(ProfilerSection.EVENT_INVOKE, new EventProfiler());
        components.put(ProfilerSection.TILEENT_UPDATETIME, new TileEntityProfiler());
        components.put(ProfilerSection.ENTITY_UPDATETIME, new EntityProfiler());
    }

    public PeriodStats.ReportSettings getReportSettings()
    {
        return settingsReportTotal;
    }

    public PeriodStats.ReportSettings getPeriodicReportSettings()
    {
        return settingsReportPeriodic;
    }

    public PeriodStats.ReportSettings getLogReportSettings()
    {
        return settingsReportLog;
    }

    public long getTotalTicks()
    {
        return totalTicks;
    }

    public long getCurrentPeriodTicks()
    {
        return tickProfiler.getCurrentTicks();
    }

    public PeriodStats getGlobalStats()
    {
        return globalStats;
    }

    public PeriodStats getCurrentPeriodStats()
    {
        return tickProfiler.getCurrentStats();
    }

    public void setUpdatePeriodTicks(long ticks)
    {
        tickProfiler.setUpdateEveryTicks(ticks);
    }

    public long getUpdatePeriodTicks()
    {
        return tickProfiler.getUpdateEveryTicks();
    }

    public void setTotalTicksLimit(long ticks)
    {
        totalTicksLimit = ticks;
    }

    public long getTotalTicksLimit()
    {
        return totalTicksLimit;
    }

    public boolean isStarted()
    {
        return ProfilerSection.TICK.getProfiler() == tickProfiler;
    }

    public void start()
    {
        profilerThreadId = Thread.currentThread().getId();
        if (totalTicks == 0) {
            tickProfiler.reset();
            startTimestamp = System.currentTimeMillis();
        }
        for (ProfilerSection section : components.keySet()) {
            startSection(section, components.get(section));
        }
        startSection(ProfilerSection.TICK, tickProfiler);
        if (totalTicksLimit >= 0) {
            DebugUtils.logInfo("Profiler started (" + totalTicks + " ticks now, limited " + totalTicksLimit + ").");
        } else {
            DebugUtils.logInfo("Profiler started (" + totalTicks + " ticks now, stops by request).");
        }
    }

    public void stop()
    {
        for (ProfilerSection section : components.keySet()) {
            stopSection(section, components.get(section));
        }
        stopSection(ProfilerSection.TICK, tickProfiler);
        profilerStarted = false;
        DebugUtils.logInfo("Profiler stopped (" + totalTicks + " ticks profiled).");
        resetLogWriter();
    }

    public void reset()
    {
        resetLogWriter();
        tickProfiler.reset();
        globalStats.reset();
        totalTicks = 0;
    }

    public void finish()
    {
        onFinish();
    }

    protected void resetLogWriter()
    {
        if (logWriter != null) {
            logWriter.flush();
            logWriter.close();
            logWriter = null;
        }
        logWriterLoaded = false;
    }

    protected File getLogFile()
    {
        File minecraftDir = ServerApi.getRootDirectory();
        if (minecraftDir != null && startTimestamp >= 0) {
            File debugDir = new File(minecraftDir, "debug");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            return new File(debugDir, "profiler." + dateFormat.format(new Date(startTimestamp)) + ".log");
        }
        return null;
    }

    protected PrintWriter getLogWriter()
    {
        if (!logWriterLoaded) {
            logWriterLoaded = true;
            File logFile = getLogFile();
            File logDir = logFile.getParentFile();
            try {
                logDir.mkdirs();
                logWriter = new PrintWriter(logFile);
            } catch (Exception ex) {
                DebugUtils.logWarn("Failed to open profiler log: " + logFile.getPath());
                return null;
            }
        }
        return logWriter;
    }

    protected void startSection(ProfilerSection section, ProfilerComponent component)
    {
        section.setProfiler(component);
        section.activate();
        component.reset();
    }

    protected void stopSection(ProfilerSection section, ProfilerComponent component)
    {
        if (section.getProfiler() == component) {
            section.desactivate();
            section.setProfiler(new DummyProfiler());
        } else {
            section.desactivate();
        }
        component.reset();
    }

    protected void onPeriodicUpdate()
    {
        printReport(reportPeriod(settingsReportPeriodic), settingsReportPeriodic);
        logReport(reportPeriod(settingsReportLog), settingsReportLog);
    }

    protected void onFinish()
    {
        tickProfiler.closePeriod();
        printReport(reportTotal(settingsReportTotal), settingsReportTotal);
        logReport(reportTotal(settingsReportLog), settingsReportLog);
        stop();
        reset();
    }

    public void printTotalReport()
    {
        printReport(reportTotal(settingsReportTotal), settingsReportTotal);
    }

    public void printCurrentPeriodReport()
    {
        printReport(reportPeriod(settingsReportPeriodic), settingsReportPeriodic);
    }

    public List<String> getTotalReport()
    {
        return reportTotal(settingsReportTotal);
    }

    public List<String> getCurrentPeriodReport()
    {
        return reportPeriod(settingsReportPeriodic);
    }

    protected void printReport(List<String> report, PeriodStats.ReportSettings settings)
    {
        if (settings.print) {
            for (String s : report) {
                DebugUtils.logInfo(s);
            }
        }
    }

    protected void logReport(List<String> report, PeriodStats.ReportSettings settings)
    {
        Date date = new Date();
        PrintWriter writer = getLogWriter();
        if (writer != null) {
            writer.println("[" + date.toString() + "]");
            for (String s : report) {
                writer.println(s);
            }
            writer.println("");
            writer.flush();
        }
    }

    protected List<String> reportTotal(PeriodStats.ReportSettings settings)
    {
        List<String> report = Lists.newArrayList();
        String header = "Profiler global update (total " + totalTicks;
        if (totalTicksLimit >= 0) {
            header += "/" + totalTicksLimit + " ticks):";
        } else {
            header += " ticks):";
        }
        report.add(header);
        report.addAll(settings.report("  ", globalStats));
        return report;
    }

    protected List<String> reportPeriod(PeriodStats.ReportSettings settings)
    {
        long ticks = tickProfiler.getCurrentTicks();
        PeriodStats stats = tickProfiler.getCurrentStats();
        List<String> report = Lists.newArrayList();
        String header = "Profiler periodic update (period of " + ticks + " ticks, total " + totalTicks;
        if (totalTicksLimit >= 0) {
            header += "/" + totalTicksLimit + " ticks):";
        } else {
            header += " ticks):";
        }
        report.add(header);
        report.addAll(settings.report("  ", stats));
        return report;
    }

    public class TickProfiler extends ProfilerComponent
    {
        private final TimeStats tickStats = new TimeStats();
        private final TimeStats tickOutStats = new TimeStats();
        private final PeriodStats currentStats = new PeriodStats(tickStats, tickOutStats);
        private long updateEveryTicks = 200;
        private long currentTicks = 0;

        public void setUpdateEveryTicks(long ticks)
        {
            updateEveryTicks = ticks;
        }

        public long getUpdateEveryTicks()
        {
            return updateEveryTicks;
        }

        public PeriodStats getCurrentStats()
        {
            return currentStats;
        }

        public long getCurrentTicks()
        {
            return currentTicks;
        }

        @Override
        public void reset()
        {
            closePeriod();
            tickStats.reset();
            tickOutStats.reset();
            currentStats.reset();
            currentTicks = 0;
            resetComponents();
        }

        @Override
        public void start()
        {
            long time = System.nanoTime();
            long delta = tickOutStats.stop(time);
            currentStats.addTickOutStats(delta);
            profilerStarted = true;
            tickStats.start();
        }

        @Override
        public void stop()
        {
            if (profilerStarted) {
                long time = System.nanoTime();
                long delta = tickStats.stop(time);
                currentTicks++;
                totalTicks++;
                updateStats(delta);
            }
            tickOutStats.start();
        }

        public void closePeriod()
        {
            if (currentTicks > 0) {
                globalStats.add(currentStats);
                tickStats.resetCounters();
                tickOutStats.resetCounters();
                currentStats.reset();
                currentTicks = 0;
            }
        }

        protected void updateStats(long tickTime)
        {
            TickStats tstats = getTickStats(tickTime);
            currentStats.addTickStats(tstats);
            if (totalTicksLimit >= 0 && totalTicks >= totalTicksLimit) {
                if (currentTicks > 0) {
                    updateGlobalStats();
                }
                onFinish();
            } else if (currentTicks >= updateEveryTicks) {
                updateGlobalStats();
            }
            resetComponentStats();
        }

        protected void updateGlobalStats()
        {
            globalStats.add(currentStats);
            onPeriodicUpdate();
            tickStats.resetCounters();
            tickOutStats.resetCounters();
            currentStats.reset();
            currentTicks = 0;
        }

        public TimeStats.ByComponent getComponentStats()
        {
            TimeStats.ByComponent all = new TimeStats.ByComponent();
            for (ProfilerSection section : components.keySet()) {
                TimeStats.ByName stats = components.get(section).getStats();
                all.add(section, stats);
            }
            return all;
        }

        public void resetComponentStats()
        {
            for (ProfilerSection section : components.keySet()) {
                components.get(section).resetStats();
            }
        }

        public void resetComponents()
        {
            for (ProfilerSection section : components.keySet()) {
                components.get(section).reset();
            }
        }

        public TickStats getTickStats(long tickTime)
        {
            return new TickStats(tickTime, totalTicks, getComponentStats());
        }

    }

    public class EntityProfiler extends ProfilerStatsComponent
    {
        @Override
        public void start(Object key)
        {
            if (key instanceof Entity) {
                Entity en = (Entity)key;
                if (!en.worldObj.isRemote) {
                    stats.start(en.getClass().getName());
                }
            }
        }

        @Override
        public void stop(Object key)
        {
            long time = System.nanoTime();
            if (key instanceof Entity && profilerStarted) {
                Entity en = (Entity)key;
                if (!en.worldObj.isRemote) {
                    stats.stop(en.getClass().getName(), time);
                }
            }
        }
    }

    public class TileEntityProfiler extends ProfilerStatsComponent
    {
        @Override
        public void start(Object key)
        {
            if (key instanceof TileEntity) {
                TileEntity te = (TileEntity)key;
                if (!te.getWorldObj().isRemote) {
                    stats.start(te.getClass().getName());
                }
            }
        }

        @Override
        public void stop(Object key)
        {
            long time = System.nanoTime();
            if (key instanceof TileEntity && profilerStarted) {
                TileEntity te = (TileEntity)key;
                if (!te.getWorldObj().isRemote) {
                    stats.stop(te.getClass().getName(), time);
                }
            }
        }
    }

    public class WorldTickProfiler extends ProfilerStatsComponent
    {
        @Override
        public void start(Object key)
        {
            if (key instanceof World) {
                Integer dim = ((World)key).provider.dimensionId;
                if (!DimensionManager.getWorld(dim).isRemote) {
                    stats.start(dim.toString());
                }
            }
        }

        @Override
        public void stop(Object key)
        {
            long time = System.nanoTime();
            if (key instanceof World && profilerStarted) {
                Integer dim = ((World)key).provider.dimensionId;
                if (!DimensionManager.getWorld(dim).isRemote) {
                    stats.stop(dim.toString(), time);
                }
            }
        }
    }

    public class BlockUpdateProfiler extends ProfilerStatsComponent
    {
        @Override
        public void start(Object key)
        {
            if (key instanceof Integer) {
                Integer dim = (Integer)key;
                if (!DimensionManager.getWorld(dim).isRemote) {
                    stats.start(dim.toString());
                }
            }
        }

        @Override
        public void stop(Object key)
        {
            long time = System.nanoTime();
            if (key instanceof Integer && profilerStarted) {
                Integer dim = (Integer)key;
                if (!DimensionManager.getWorld(dim).isRemote) {
                    stats.stop(dim.toString(), time);
                }
            }
        }
    }

    public class EventProfiler extends ProfilerStatsComponent
    {
        @Override
        public void start()
        {
            long threadId = Thread.currentThread().getId();
            if (threadId == profilerThreadId) {
                stats.startUndefined();
            }
        }

        @Override
        public void stop(Object event, Object pkg, Object handler, Object mod)
        {
            long time = System.nanoTime();
            long threadId = Thread.currentThread().getId();
            if (profilerStarted && threadId == profilerThreadId) {
                stats.stopUndefined(getEventName(event.getClass()) + "|" + (String)pkg, time);
            }
        }

        private String getEventName(Class c)
        {
            String simpleName = c.getSimpleName();
            if (c.isMemberClass()) {
                Class<?> enclosingClass = c.getEnclosingClass();
                simpleName = getEventName(enclosingClass) + "." + simpleName;
            }
            return simpleName;
        }
    }
}
