package me.psychedelicpalimpsest;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;



public class PuppeteerCommandRegistry {
    public static final Map<String, BaseCommand> COMMAND_MAP = new HashMap<>();
    public static final Map<String, String> COMMAND_DESC_MAP = new HashMap<>();
    public static final Map<String, Boolean> COMMAND_NEEDS_BARITONE_MAP = new HashMap<>();
    public static final List<BaseCommand> COMMANDS = new ArrayList<>();




    static {
        Reflections reflections = new Reflections("me.psychedelicpalimpsest");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(PuppeteerCommand.class);

        for (Class<?> aClass : annotated) {
            BaseCommand command = null;

            for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
                if (constructor.getParameterCount() != 0) continue;
                constructor.setAccessible(true);

                try {
                    Object instance = constructor.newInstance();
                    command = (BaseCommand) instance;

                    break;
                } catch (Exception e) {
                    System.err.println("Error instantiating " + aClass.getName() + ", are you sure it implements baseCommand?");
                    e.printStackTrace();
                }
            }
            if (command == null) {
                throw new RuntimeException("Error instantiating " + aClass.getName() + ", A puppeteer-command annotated class MUST have a no-arg constructor!");
            }
            String commandName = aClass.getAnnotation(PuppeteerCommand.class).cmd();
            String commandDesc = aClass.getAnnotation(PuppeteerCommand.class).description();
            Boolean commandBaritone = aClass.getAnnotation(PuppeteerCommand.class).needs_baritone();

            COMMAND_MAP.put(commandName, command);
            COMMAND_NEEDS_BARITONE_MAP.put(commandName, commandBaritone);
            COMMAND_DESC_MAP.put(commandName, commandDesc);
            COMMANDS.add(command);
        }
    }

}
