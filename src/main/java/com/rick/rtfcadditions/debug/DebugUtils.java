package com.rick.rtfcadditions.debug;

import com.rick.rtfcadditions.helpers.ClassHelper;
import com.rick.rtfcadditions.Reference;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.Level;

/**
 *
 * @author Rick
 */
public class DebugUtils
{
    public static void log(Level logLevel, String message)
    {
        FMLLog.log(Reference.MODID, logLevel, "%s", message);
    }

    public static void print(String message)
    {
        log(Level.INFO, message);
    }

    public static void logInfo(String message)
    {
        log(Level.INFO, message);
    }

    public static void logWarn(String message)
    {
        log(Level.WARN, message);
    }

    public static void logDebug(String message)
    {
        log(Level.DEBUG, message);
    }

    public static void logException(Throwable ex)
    {
        logWarn("Exception " + ex.getClass().getName() + ": " + ex.getMessage());
        printStackTrace(ex);
    }

    public static void printStackTrace()
    {
        printStackTrace(new Throwable().getStackTrace(), 1);
    }

    public static void printStackTrace(Throwable ex)
    {
        printStackTrace(ex.getStackTrace());
    }

    public static void printStackTrace(StackTraceElement[] stackTrace)
    {
        printStackTrace(stackTrace, 0);
    }

    public static void printStackTrace(StackTraceElement[] stackTrace, int startIndex)
    {
        for (int i = startIndex; i < stackTrace.length; ++i) {
            print("#" + i + " " + stackTrace[i].toString());
        }
    }

    public static String dumpVariable(Object obj)
    {
        if (obj == null) {
            return "null";
        } else if (obj.getClass().isPrimitive()) {
            return String.valueOf(obj);
        } else if (obj instanceof String) {
            return String.valueOf(obj);
        } else {
            String hash = Integer.toHexString(System.identityHashCode(obj));
            return obj.getClass().getName() + "#" + hash;
        }
    }

    private static String dumpNbt(NBTBase nbt, String prefix)
    {
        if (nbt == null) {
            return "null";
        }
        if (nbt instanceof NBTTagCompound) {
            return dumpNbt((NBTTagCompound)nbt, prefix);
        }
        if (nbt instanceof NBTTagList) {
            return dumpNbt((NBTTagList)nbt, prefix);
        }
        if (nbt instanceof NBTTagByte) {
            return String.valueOf(((NBTTagByte)nbt).func_150290_f());
        }
        if (nbt instanceof NBTTagDouble) {
            return String.valueOf(((NBTTagDouble)nbt).func_150286_g());
        }
        if (nbt instanceof NBTTagFloat) {
            return String.valueOf(((NBTTagFloat)nbt).func_150288_h());
        }
        if (nbt instanceof NBTTagInt) {
            return String.valueOf(((NBTTagInt)nbt).func_150287_d());
        }
        if (nbt instanceof NBTTagLong) {
            return String.valueOf(((NBTTagLong)nbt).func_150291_c());
        }
        if (nbt instanceof NBTTagShort) {
            return String.valueOf(((NBTTagShort)nbt).func_150289_e());
        }
        if (nbt instanceof NBTTagString) {
            return String.valueOf(((NBTTagString)nbt).func_150285_a_());
        }
        return dumpVariable(nbt);
    }

    private static String dumpNbt(NBTTagList nbt, String prefix)
    {
        StringBuilder result = new StringBuilder();

        result.append("[");
        if (nbt.tagCount() > 0) {
            NBTTagList copy = (NBTTagList)nbt.copy();
            while (copy.tagCount() > 0) {
                NBTBase val = copy.removeTag(0);
                result.append(System.lineSeparator());
                result.append(prefix);
                result.append(dumpNbt(val, prefix + "  "));
            }
            result.append(System.lineSeparator());
            result.append(prefix);
        }
        result.append("]");
        return result.toString();
    }

    private static String dumpNbt(NBTTagCompound nbt, String prefix)
    {
        StringBuilder result = new StringBuilder();
        Set keys = nbt.func_150296_c();
        result.append("{");
        if (keys.size() > 0) {
            for (Object keyObj : keys) {
                String key = String.valueOf(keyObj);
                NBTBase val = nbt.getTag(key);
                result.append(System.lineSeparator());
                result.append(prefix);
                result.append(key);
                result.append(" = ");
                result.append(dumpNbt(val, prefix + "  "));
            }
            result.append(System.lineSeparator());
            result.append(prefix);
        }
        result.append("}");
        return result.toString();
    }

    public static String dumpNbt(NBTTagCompound nbt)
    {
        return dumpNbt(nbt, "");
    }

    public static void printNbt(NBTTagCompound nbt)
    {
        String dump = dumpNbt(nbt);
        String[] lines = dump.split(System.lineSeparator());
        for (String line : lines) {
            print(line);
        }
    }

    public static void printEventListeners(Event event, EventBus bus)
    {
        int busId;
        try {
            java.lang.reflect.Field field = EventBus.class.getField("busID");
            field.setAccessible(true);
            busId = (Integer)field.get(bus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
        IEventListener[] listeners = event.getListenerList().getListeners(busId);
        for (int i = 0; i < listeners.length; ++i) {
            logInfo(listeners[i].toString());
        }
    }
}
