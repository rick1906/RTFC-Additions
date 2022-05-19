package com.rick.rtfcadditions.profiler;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import mcp.mobius.mobiuscore.profiler.ProfilerSection;

/**
 *
 * @author Rick
 */
public class TickStats
{
    public static final ProfilerSection[] SECTIONS = {
        ProfilerSection.DIMENSION_TICK,
        ProfilerSection.EVENT_INVOKE,
        ProfilerSection.DIMENSION_BLOCKTICK,
        ProfilerSection.ENTITY_UPDATETIME,
        ProfilerSection.TILEENT_UPDATETIME
    };

    public static final HashMap<ProfilerSection, String> SECTIONS_NAMES = new HashMap<>();

    static {
        SECTIONS_NAMES.put(ProfilerSection.DIMENSION_TICK, "World Tick");
        SECTIONS_NAMES.put(ProfilerSection.EVENT_INVOKE, "Events");
        SECTIONS_NAMES.put(ProfilerSection.DIMENSION_BLOCKTICK, "Blocks");
        SECTIONS_NAMES.put(ProfilerSection.ENTITY_UPDATETIME, "Entities");
        SECTIONS_NAMES.put(ProfilerSection.TILEENT_UPDATETIME, "Tile Entities");
    }

    public static String getSectionName(ProfilerSection section)
    {
        String name = SECTIONS_NAMES.get(section);
        return name != null && name.length() > 0 ? name : section.toString();
    }

    private TimeStats.ByComponent byComponent;
    private long tickTime;
    private long tickIndex;

    public TickStats(long tickTime, long tickIndex, TimeStats.ByComponent byComponent)
    {
        this.tickTime = tickTime;
        this.tickIndex = tickIndex;
        this.byComponent = byComponent;
    }

    public TimeStats.ByComponent getStatsByComponent()
    {
        return byComponent;
    }

    public long getTime()
    {
        return tickTime;
    }

    public long getIndex()
    {
        return tickIndex;
    }

    public List<String> report(String prefix, int countByTotal, int countByAverage)
    {
        TimeStats tickStats = new TimeStats();
        tickStats.count = 1;
        tickStats.sumTime = tickTime;
        tickStats.maxTime = tickTime;
        List<String> report = Lists.newArrayList();
        report.add(prefix + "ProfiledTick#" + tickIndex + ": " + TimeStats.formatMs(tickTime));
        String nextPrefix = prefix + "  ";
        report.addAll(byComponent.report(nextPrefix, countByTotal, countByAverage, tickStats));
        return report;
    }
}
