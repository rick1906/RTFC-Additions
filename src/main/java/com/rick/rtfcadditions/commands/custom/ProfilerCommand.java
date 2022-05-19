package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.commands.InvalidCommandException;
import com.rick.rtfcadditions.profiler.Profiler;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Rick
 */
public class ProfilerCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (args.length == 2) {
                    setLimit(args[1]);
                } else if (args.length > 2) {
                    return false;
                }
                if (!Profiler.getProfiler().isStarted()) {
                    Profiler.getProfiler().start();
                    return true;
                } else {
                    throw new InvalidCommandException("Already started");
                }
            }
            if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("finish")) {
                if (Profiler.getProfiler().isStarted() || Profiler.getProfiler().getTotalTicks() > 0) {
                    Profiler.getProfiler().finish();
                    return true;
                } else {
                    throw new InvalidCommandException("Already stopped");
                }
            }
            if (args[0].equalsIgnoreCase("pause")) {
                if (Profiler.getProfiler().isStarted()) {
                    Profiler.getProfiler().stop();
                    return true;
                } else {
                    throw new InvalidCommandException("Already stopped");
                }
            }
            if (args[0].equalsIgnoreCase("reset")) {
                Profiler.getProfiler().reset();
                return true;
            }
            if (args[0].equalsIgnoreCase("limit")) {
                if (args.length == 2) {
                    setLimit(args[1]);
                    return true;
                } else if (args.length > 2) {
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("period")) {
                if (args.length == 2) {
                    setPeriod(args[1]);
                    return true;
                } else if (args.length > 2) {
                    return false;
                }
            }
        }
        return false;
    }

    public void setLimit(String limit)
    {
        try {
            if (limit.equalsIgnoreCase("none")) {
                Profiler.getProfiler().setTotalTicksLimit(-1);
            } else {
                Profiler.getProfiler().setTotalTicksLimit(Math.max(-1, Long.valueOf(limit)));
            }
        } catch (Exception ex) {
            throw new InvalidCommandException("Invalid limit value");
        }
    }

    public void setPeriod(String period)
    {
        long p;
        try {
            p = Long.valueOf(period);
        } catch (Exception ex) {
            throw new InvalidCommandException("Invalid period value");
        }
        if (p >= 1) {
            Profiler.getProfiler().setUpdatePeriodTicks(p);
        } else {
            throw new InvalidCommandException("Invalid period value");
        }
    }

}
