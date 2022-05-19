package com.rick.rtfcadditions.commands.custom;

import com.rick.rtfcadditions.commands.InvalidCommandException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Rick
 */
public abstract class AbstractCommand
{
    public static String getCommandClassName(String name)
    {
        return name.substring(0, 1).toUpperCase() + name.substring(1) + "Command";
    }

    public static String getCommandPackageName(String name)
    {
        return AbstractCommand.class.getPackage().getName();
    }

    public static Class<?> getCommandClass(String name)
    {
        String cClassName = getCommandClassName(name);
        String cPackageName = getCommandPackageName(name);
        Class<?> c;
        try {
            c = Class.forName(cPackageName + "." + cClassName);
        } catch (ClassNotFoundException ex) {
            c = findCommandClass(name, cPackageName);
        }
        if (c == null) {
            throw new InvalidCommandException("Command '" + name + "' not found");
        }
        if (Modifier.isAbstract(c.getModifiers())) {
            throw new InvalidCommandException("Command '" + name + "' is abstract");
        }
        return c;
    }

    public static Class<?> findCommandClass(String name, String packageName)
    {
        File jarFile = getJarFile(AbstractCommand.class);
        return findClassCaseInsensitive(jarFile, name, packageName);
    }

    public static Class<?> findClassCaseInsensitive(File jarFile, String name, String packageName)
    {
        List<String> classes = getJarClasses(jarFile, packageName);
        String className = packageName + "." + name;
        for (String cn : classes) {
            if (cn.equalsIgnoreCase(className)) {
                try {
                    return Class.forName(cn);
                } catch (ClassNotFoundException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public static File getJarFile(Class c)
    {
        return new File(c.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    public static List<String> getJarClasses(File jarFile, String packageName)
    {
        if (!jarFile.isFile()) {
            return Collections.emptyList();
        }
        String packagePath = packageName.replace('.', '/');
        ArrayList<String> classes = new ArrayList<>();
        try {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(packagePath + "/") && name.endsWith(".class")) {
                    classes.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'));
                }
            }
        } catch (IOException ex) {
            return Collections.emptyList();
        }
        return classes;
    }

    public static AbstractCommand create(String[] allArgs)
    {
        if (allArgs.length > 0) {
            String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
            return create(allArgs[0], args);
        } else {
            throw new InvalidCommandException("No command name supplied");
        }
    }

    public static AbstractCommand create(String name, String[] args)
    {
        Class<?> c = getCommandClass(name);
        Constructor cons = null;
        for (int i = 0; i <= args.length; ++i) {
            Class<?>[] conArgs = new Class<?>[i];
            Arrays.fill(conArgs, String.class);
            try {
                cons = c.getConstructor(conArgs);
            } catch (NoSuchMethodException | SecurityException ex) {
                continue;
            }
            break;
        }
        if (cons == null) {
            throw new InvalidCommandException("Invalid subcommand for command '" + name + "'");
        }

        Object commandObj;
        String[] objArgs = new String[cons.getParameterCount()];
        System.arraycopy(args, 0, objArgs, 0, objArgs.length);
        try {
            commandObj = cons.newInstance((Object[])objArgs);
        } catch (InstantiationException | IllegalAccessException | InvalidCommandException | InvocationTargetException ex) {
            throw new InvalidCommandException("Error initializing command '" + name + "'");
        }

        AbstractCommand command;
        if (commandObj instanceof AbstractCommand) {
            command = (AbstractCommand)commandObj;
        } else {
            throw new InvalidCommandException("Invalid command object for command '" + name + "'");
        }

        String[] extraArgs = Arrays.copyOfRange(args, objArgs.length, args.length);
        if (!command.setArguments(extraArgs)) {
            throw new InvalidCommandException("Illegal arguments for command '" + name + "'");
        }
        return command;
    }

    protected String[] args = null;

    public abstract boolean run(ICommandSender sender);

    public boolean setArguments(String[] args)
    {
        this.args = args;
        return true;
    }

}
