package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.api.SpecialLog;
import com.rick.rtfcadditions.commands.InvalidCommandException;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Rick
 */
public class LogCommand extends AbstractCommand
{
    @Override
    public boolean run(ICommandSender sender)
    {
        if (args.length <= 0) {
            return false;
        } else {
            SpecialLog log = SpecialLog.getLog(args[0]);
            if (log == null) {
                throw new InvalidCommandException("Log '" + args[0] + "' not found");
            }
            if (args.length >= 2) {
                String cmd = args[1];
                if (cmd.equalsIgnoreCase("enable") && args.length == 2) {
                    log.enable();
                    return true;
                }
                if (cmd.equalsIgnoreCase("disable") && args.length == 2) {
                    log.disable();
                    return true;
                }
                if (cmd.equalsIgnoreCase("trace") && args.length == 3) {
                    String flag = args[2];
                    if (flag.equalsIgnoreCase("start") || flag.equalsIgnoreCase("enable")) {
                        log.get().startTrace();
                        return true;
                    }
                    if (flag.equalsIgnoreCase("stop") || flag.equalsIgnoreCase("disable")) {
                        log.get().stopTrace();
                        return true;
                    }
                    throw new InvalidCommandException("Invalid log trace command");
                }
                if (args.length > 2) {
                    SpecialLog.Group group = log.get(args[1]);
                    if (group.isEmpty()) {
                        throw new InvalidCommandException("Log group '" + args[1] + "' is empty");
                    } else {
                        String gcmd = args[2];
                        if (gcmd.equalsIgnoreCase("enable") && args.length == 3) {
                            group.enable();
                            return true;
                        }
                        if (gcmd.equalsIgnoreCase("disable") && args.length == 3) {
                            group.disable();
                            return true;
                        }
                        if (gcmd.equalsIgnoreCase("trace") && args.length == 4) {
                            String flag = args[3];
                            if (flag.equalsIgnoreCase("start") || flag.equalsIgnoreCase("enable")) {
                                group.startTrace();
                                return true;
                            }
                            if (flag.equalsIgnoreCase("stop") || flag.equalsIgnoreCase("disable")) {
                                group.stopTrace();
                                return true;
                            }
                            throw new InvalidCommandException("Invalid log group trace command");
                        }
                        throw new InvalidCommandException("Invalid log group command");
                    }
                }
                throw new InvalidCommandException("Invalid log command");
            } else {
                throw new InvalidCommandException("No log command supplied");
            }
        }
    }

}
