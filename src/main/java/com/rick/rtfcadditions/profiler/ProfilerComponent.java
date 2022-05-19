package com.rick.rtfcadditions.profiler;

import mcp.mobius.mobiuscore.profiler.IProfilerBase;

/**
 *
 * @author Rick
 */
public class ProfilerComponent implements IProfilerBase
{
    @Override
    public void reset()
    {
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {

    }

    @Override
    public void start(Object key)
    {
        start();
    }

    @Override
    public void stop(Object key)
    {
        stop();
    }

    @Override
    public void start(Object key1, Object key2)
    {
        start(key1);
    }

    @Override
    public void stop(Object key1, Object key2)
    {
        stop(key1);
    }

    @Override
    public void start(Object key1, Object key2, Object key3)
    {
        start(key1, key2);
    }

    @Override
    public void stop(Object key1, Object key2, Object key3)
    {
        stop(key1, key2);
    }

    @Override
    public void start(Object key1, Object key2, Object key3, Object key4)
    {
        start(key1, key2, key3);
    }

    @Override
    public void stop(Object key1, Object key2, Object key3, Object key4)
    {
        stop(key1, key2, key3);
    }

}
