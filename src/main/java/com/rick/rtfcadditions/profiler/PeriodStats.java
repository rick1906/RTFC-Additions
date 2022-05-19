package com.rick.rtfcadditions.profiler;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rick
 */
public class PeriodStats
{
    private static final long TICKTIME_NS = 50000000L;

    private static final int KEEP_TICKS = 10;

    private boolean live;
    private final TimeStats tickStats;
    private final TimeStats tickOutStats;
    private final TimeStats badTickStats = new TimeStats();
    private final TimeStats badTickOutStats = new TimeStats();
    private final TimeStats.ByComponent cTickStats = new TimeStats.ByComponent();
    private final TimeStats.ByComponent cBadTickStats = new TimeStats.ByComponent();
    private final ArrayList<TickStats> worstTicks = new ArrayList<>();

    public PeriodStats(TimeStats tickStats, TimeStats tickOutStats)
    {
        this.live = true;
        this.tickStats = tickStats;
        this.tickOutStats = tickOutStats;
    }

    public PeriodStats()
    {
        this.live = false;
        this.tickStats = new TimeStats();
        this.tickOutStats = new TimeStats();
    }

    public long getTicksCount()
    {
        return tickStats.count;
    }

    public void addTickStats(TickStats tstats)
    {
        long tickTime = tstats.getTime();
        addWorstTick(tstats);
        cTickStats.add(tstats.getStatsByComponent());
        if (tickTime > TICKTIME_NS) {
            badTickStats.add(tickTime);
            cBadTickStats.add(tstats.getStatsByComponent());
        }
    }

    public void addTickOutStats(long tickOutTime)
    {
        if (tickOutTime > TICKTIME_NS) {
            badTickOutStats.add(tickOutTime);
        }
    }

    private boolean addWorstTick(TickStats tstats)
    {
        int length = worstTicks.size();
        if (length <= 0) {
            worstTicks.add(tstats);
            return true;
        }
        if (tstats.getTime() <= worstTicks.get(0).getTime()) {
            if (length >= KEEP_TICKS) {
                return false;
            } else {
                worstTicks.add(0, tstats);
                return true;
            }
        }
        for (int i = 1; i < length; ++i) {
            if (tstats.getTime() <= worstTicks.get(i).getTime()) {
                worstTicks.add(i, tstats);
                if (length >= KEEP_TICKS) {
                    worstTicks.remove(0);
                }
                return true;
            }
        }
        worstTicks.add(tstats);
        if (length >= KEEP_TICKS) {
            worstTicks.remove(0);
        }
        return true;
    }

    public void add(PeriodStats stats)
    {
        live = false;
        tickStats.add(stats.tickStats);
        tickOutStats.add(stats.tickOutStats);
        badTickStats.add(stats.badTickStats);
        badTickOutStats.add(stats.badTickOutStats);
        cTickStats.add(stats.cTickStats);
        cBadTickStats.add(stats.cBadTickStats);
        for (TickStats tstats : stats.worstTicks) {
            addWorstTick(tstats);
        }
    }

    public void reset()
    {
        if (!live) {
            tickStats.reset();
            tickOutStats.reset();
        }
        badTickStats.reset();
        badTickOutStats.reset();
        cTickStats.reset();
        cBadTickStats.reset();
        worstTicks.clear();
    }

    public List<String> reportTicks(String prefix, int countByTotal, int countByAverage)
    {
        List<String> report = Lists.newArrayList();
        report.add(prefix + "Profiled ticks: " + tickStats.getRelativeString(tickStats, true));
        report.add(prefix + "Profiled bad ticks: " + badTickStats.getRelativeString(tickStats, true));
        report.add(prefix + "Out-tick intervals: " + tickOutStats.getRelativeString());
        report.add(prefix + "Bad out-tick intervals: " + badTickOutStats.getRelativeString(tickOutStats, true));
        if (tickStats.count >= tickOutStats.count && tickStats.count > 0) {
            String total = "Total running time = " + TimeStats.formatMs(tickStats.sumTime + tickOutStats.sumTime);
            total += "; full tick period average = " + TimeStats.formatMs((double)(tickStats.sumTime + tickOutStats.sumTime) / tickStats.count);
            report.add(prefix + total);
        }
        String nextPrefix = prefix + "  ";
        report.addAll(cTickStats.report(nextPrefix, countByTotal, countByAverage, tickStats));
        return report;
    }

    public List<String> reportBadTicks(String prefix, int countByTotal, int countByAverage)
    {
        List<String> report = Lists.newArrayList();
        report.add(prefix + "Bad ticks info: " + badTickStats.getRelativeString(tickStats, true));
        String nextPrefix = prefix + "  ";
        report.addAll(cBadTickStats.report(nextPrefix, countByTotal, countByAverage, badTickStats));
        return report;
    }

    public List<String> reportWorstTicks(String prefix, int count, int countByTotal, int countByAverage)
    {
        List<String> report = Lists.newArrayList();
        int countSaved = worstTicks.size();
        int countWorst = count >= 0 ? Math.min(count, countSaved) : countSaved;
        if (countWorst > 0) {
            report.add(prefix + "Profiled ticks with worst time (" + count + " of " + badTickStats.count + "):");
            String nextPrefix = prefix + "  ";
            for (int i = 0; i < countWorst; ++i) {
                TickStats tstats = worstTicks.get(countSaved - i - 1);
                report.addAll(tstats.report(nextPrefix, countByTotal, countByAverage));
            }
        }
        return report;
    }

    public static class ReportSettings
    {
        public int normalTickCountItemsByTotal = 3;
        public int normalTickCountItemsByAverage = 3;
        public int badTickCountItemsByTotal = 3;
        public int badTickCountItemsByAverage = 3;
        public int worstTickCountItemsByTotal = 8;
        public int worstTickCountItemsByAverage = 8;
        public int worstTicksCount = 3;
        public boolean print = true;

        public ReportSettings()
        {
        }

        public ReportSettings(int level)
        {
            if (level > 0) {
                normalTickCountItemsByTotal = -1;
                normalTickCountItemsByAverage = 5;
                badTickCountItemsByTotal = -1;
                badTickCountItemsByAverage = 5;
                worstTickCountItemsByTotal = -1;
                worstTickCountItemsByAverage = 5;
                worstTicksCount = 5;
            } else if (level < 0) {
                normalTickCountItemsByTotal = 0;
                normalTickCountItemsByAverage = 0;
                badTickCountItemsByTotal = 0;
                badTickCountItemsByAverage = 0;
                worstTickCountItemsByTotal = 3;
                worstTickCountItemsByAverage = 3;
                worstTicksCount = 1;
            }
        }

        public List<String> report(String prefix, PeriodStats stats)
        {
            List<String> report = Lists.newArrayList();
            report.addAll(stats.reportTicks(prefix, normalTickCountItemsByTotal, normalTickCountItemsByAverage));
            report.addAll(stats.reportBadTicks(prefix, badTickCountItemsByTotal, badTickCountItemsByAverage));
            report.addAll(stats.reportWorstTicks(prefix, worstTicksCount, worstTickCountItemsByTotal, worstTickCountItemsByAverage));
            return report;
        }
    }
}
