package com.rick.rtfcadditions.profiler;

/**
 *
 * @author Rick
 */
public class ProfilerStatsComponent extends ProfilerComponent
{
    protected TimeStats.ByName stats = new TimeStats.ByName();

    @Override
    public void reset()
    {
        stats = new TimeStats.ByName();
    }

    public TimeStats.ByName getStats()
    {
        return stats;
    }

    public void resetStats()
    {
        stats.reset();
    }
}
