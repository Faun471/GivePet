package me.faun.givepet.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotation.Description;
import dev.triumphteam.cmd.core.annotation.SubCommand;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

public class CommandManager {
    private static final HashMap<String, Command> commands = new HashMap<>();

    public static void loadCommands() {
        for (Method method : GivePetCommand.class.getDeclaredMethods()) {
            if (method.getAnnotations().length == 0) {
                break;
            }

            String name = method.getName();
            String[] alias = new String[]{};
            String commandDescription = null;
            String commandPermission = null;

            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof SubCommand command) {
                    name = command.value();
                    alias = command.alias();
                }

                if (annotation instanceof Description description) {
                    commandDescription = description.value();
                }

                if (annotation instanceof Permission permission) {
                    commandPermission = permission.value();
                }
            }

            commands.put(name, new Command(name, alias, commandDescription, commandPermission));
        }
    }

    public static HashMap<String, Command> getCommands() {
        return commands;
    }
}
