package com.rick.rtfcadditions.profiler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import mcp.mobius.mobiuscore.profiler.ProfilerSection;

/**
 *
 * @author Rick
 */
public class TimeStats
{
    private static final long L30E9 = 30000000000L;
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
    private static final NumberFormat formatter = new DecimalFormat("#0.00", symbols);

    private boolean started = false;
    private long startedTime = 0;

    public long count = 0;
    public long countMiss = 0;
    public long sumTime = 0;
    public long maxTime = 0;
    public long minTime = Long.MAX_VALUE;

    public void start()
    {
        if (started) {
            countMiss++;
            started = false;
        }
        started = true;
        startedTime = System.nanoTime();
    }

    public long stop(long nanoTime)
    {
        started = false;
        long delta = nanoTime - startedTime;
        if (delta >= 0 && delta <= L30E9) {
            count++;
            sumTime += delta;
            maxTime = Math.max(maxTime, delta);
            minTime = Math.min(minTime, delta);
            return delta;
        } else {
            countMiss++;
            return -1;
        }
    }

    public void add(long delta)
    {
        if (delta >= 0 && delta <= L30E9) {
            count++;
            sumTime += delta;
            maxTime = Math.max(maxTime, delta);
            minTime = Math.min(minTime, delta);
        } else {
            countMiss++;
        }
    }

    public void add(TimeStats stats)
    {
        count += stats.count;
        countMiss += stats.countMiss;
        sumTime += stats.sumTime;
        maxTime = Math.max(maxTime, stats.maxTime);
        minTime = Math.min(minTime, stats.minTime);
    }

    public void reset()
    {
        started = false;
        count = 0;
        countMiss = 0;
        sumTime = 0;
        maxTime = 0;
        minTime = Long.MAX_VALUE;
    }

    public void resetCounters()
    {
        count = 0;
        countMiss = 0;
        sumTime = 0;
        maxTime = 0;
        minTime = Long.MAX_VALUE;
    }

    public double getAverage()
    {
        return count > 0 ? ((double)sumTime / count) : Double.NaN;
    }

    public static String formatMs(double value)
    {
        return Double.isNaN(value) ? "NaN" : (formatter.format(value / 1000000D) + "ms");
    }

    public static String formatMs(long value)
    {
        return value == Long.MAX_VALUE ? "NaN" : formatMs((double)value);
    }

    @Override
    public String toString()
    {
        return getCountString() + ", " + getTimingsString();
    }

    public String getCountString()
    {
        String cnt = countMiss > 0 ? (count + " (miss=" + countMiss + ")") : String.valueOf(count);
        return "count=" + cnt;
    }

    public String getCountString(TimeStats stats)
    {
        String ptk = stats.count > 1 ? (" (" + formatter.format((double)count / stats.count) + "/tick)") : "";
        return getCountString() + ptk;
    }

    public String getTimingsString()
    {
        String avg = count > 0 ? formatMs((double)sumTime / count) : "NaN";
        String max = count > 0 ? formatMs((double)maxTime) : "NaN";
        String min = count > 0 ? formatMs((double)minTime) : "NaN";
        return "avg=" + avg + ", max=" + max + ", min=" + min;
    }

    public String getRelativeString(TimeStats stats, boolean withTotal)
    {
        String tot = formatMs(sumTime);
        String pct = stats.sumTime > 0 ? (formatter.format(100 * (double)sumTime / stats.sumTime) + "%") : "NaN%";
        if (withTotal) {
            return "total=" + tot + " (" + pct + "), " + getCountString() + ", " + getTimingsString();
        } else {
            String ptk = stats.count > 0 ? (" (" + formatMs((double)sumTime / stats.count) + "/tick)") : "";
            return pct + ptk + ", " + getCountString(stats) + ", " + getTimingsString();
        }
    }

    public String getRelativeString(TimeStats stats)
    {
        return getRelativeString(stats, false);
    }

    public String getRelativeString()
    {
        String tot = formatMs(sumTime);
        return "total=" + tot + ", " + toString();
    }

    public static class TotalComparator implements Comparator<Entry<String, TimeStats>>
    {
        @Override
        public int compare(Entry<String, TimeStats> o1, Entry<String, TimeStats> o2)
        {
            return Long.compare(o2.getValue().sumTime, o1.getValue().sumTime);
        }
    }

    public static class AverageComparator implements Comparator<Entry<String, TimeStats>>
    {
        @Override
        public int compare(Entry<String, TimeStats> o1, Entry<String, TimeStats> o2)
        {
            return Double.compare(o2.getValue().getAverage(), o1.getValue().getAverage());
        }
    }

    public static class ByName
    {
        private final TimeStats total = new TimeStats();
        private final HashMap<String, TimeStats> named = new HashMap<>();
        private final Stack<TimeStats> stack = new Stack<>();

        public TimeStats getTotal()
        {
            return total;
        }

        public int getMaxCallsPerEntry()
        {
            int count = 0;
            for (TimeStats ts : named.values()) {
                count = (int)Math.max(count, ts.count);
            }
            return count;
        }

        public TimeStats getByName(String name)
        {
            return named.get(name);
        }

        public Set<String> getNames()
        {
            return named.keySet();
        }

        public int getEntriesCount()
        {
            return named.size();
        }

        public List<Entry<String, TimeStats>> getEntries()
        {
            return new ArrayList<>(named.entrySet());
        }

        public List<Entry<String, TimeStats>> getSortedEntries()
        {
            List<Entry<String, TimeStats>> list = getEntries();
            list.sort(new TotalComparator());
            return list;
        }

        public List<Entry<String, TimeStats>> getSortedByAverageEntries()
        {
            List<Entry<String, TimeStats>> list = getEntries();
            list.sort(new AverageComparator());
            return list;
        }

        public void startUndefined()
        {
            TimeStats stats = new TimeStats();
            stack.push(stats);
            stats.start();
        }

        public void stopUndefined()
        {
            if (!stack.isEmpty()) {
                stack.pop();
            }
        }

        public boolean stopUndefined(String name, long time)
        {
            if (stack.isEmpty()) {
                return false;
            }
            TimeStats stats = stack.pop();
            long delta = stats.stop(time);
            total.add(delta);
            synchronized (named) {
                TimeStats existing = named.get(name);
                if (existing != null) {
                    existing.add(stats);
                } else {
                    named.put(name, stats);
                }
            }
            return true;
        }

        public void start(String name)
        {
            synchronized (named) {
                TimeStats stats = named.get(name);
                if (stats == null) {
                    stats = new TimeStats();
                    named.put(name, stats);
                }
                stats.start();
            }
        }

        public void stop(String name, long time)
        {
            synchronized (named) {
                TimeStats stats = named.get(name);
                if (stats == null) {
                    stats = new TimeStats();
                    named.put(name, stats);
                }
                long delta = stats.stop(time);
                total.add(delta);
            }
        }

        public void add(String name, TimeStats stats)
        {
            synchronized (named) {
                TimeStats existing = named.get(name);
                if (existing == null) {
                    existing = new TimeStats();
                    named.put(name, existing);
                }
                existing.add(stats);
                total.add(stats);
            }
        }

        public void add(ByName other)
        {
            synchronized (named) {
                for (String name : other.named.keySet()) {
                    TimeStats existing = named.get(name);
                    if (existing == null) {
                        existing = new TimeStats();
                        named.put(name, existing);
                    }
                    existing.add(other.named.get(name));
                }
                total.add(other.total);
            }
        }

        public void reset()
        {
            total.reset();
            named.clear();
            stack.clear();
        }

        @Override
        public String toString()
        {
            return total.toString();
        }

        public String getTotalString()
        {
            return total.toString();
        }

        public List<String> reportByTotal(String prefix, int count, TimeStats tickStats)
        {
            return reportByList(getSortedEntries(), prefix, count, tickStats);
        }

        public List<String> reportByAverage(String prefix, int count, TimeStats tickStats)
        {
            return reportByList(getSortedByAverageEntries(), prefix, count, tickStats);
        }

        private List<String> reportByList(List<Entry<String, TimeStats>> list, String prefix, int count, TimeStats tickStats)
        {
            if (count < 0) {
                count = list.size();
            } else {
                count = Math.min(count, list.size());
            }
            int remains = list.size() - count;
            List<String> report = new ArrayList<>();
            if (count <= 0) {
                return report;
            }
            for (int i = 0; i < count; ++i) {
                Entry<String, TimeStats> entry = list.get(i);
                String name = entry.getKey();
                TimeStats stats = entry.getValue();
                report.add(prefix + name + ": " + stats.getRelativeString(tickStats));
            }
            if (remains > 0) {
                report.add(prefix + "... (" + remains + " more)");
            }
            return report;
        }
    }

    public static class ByComponent
    {
        private final HashMap<ProfilerSection, ByName> data = new HashMap<>();

        public ByName getBySection(ProfilerSection section)
        {
            return data.get(section);
        }

        public void add(ByComponent cstats)
        {
            for (ProfilerSection section : cstats.data.keySet()) {
                add(section, cstats.data.get(section));
            }
        }

        public void add(ProfilerSection section, ByName ndata)
        {
            ByName existing = data.get(section);
            if (existing == null) {
                existing = new ByName();
                data.put(section, existing);
            }
            existing.add(ndata);
        }

        public void reset()
        {
            data.clear();
        }

        public List<String> report(String prefix, int countByTotal, int countByAverage, TimeStats tickStats)
        {
            ArrayList<String> report = new ArrayList<>();
            for (ProfilerSection section : TickStats.SECTIONS) {
                ByName stats = getBySection(section);
                TimeStats total = stats != null ? stats.getTotal() : new TimeStats();
                String sectionName = TickStats.getSectionName(section);
                String header = prefix + sectionName + ": " + total.getRelativeString(tickStats);
                report.add(header);
                if (stats != null) {
                    String nextPrefix = prefix + "  ";
                    report.addAll(stats.reportByTotal(nextPrefix, countByTotal, tickStats));
                    if (stats.getEntriesCount() > 0 && countByAverage != 0 && stats.getMaxCallsPerEntry() > 1) {
                        report.add(prefix + sectionName + " (sorted by average time):");
                        report.addAll(stats.reportByAverage(nextPrefix, countByAverage, tickStats));
                    }
                }
            }
            return report;
        }
    }
}
