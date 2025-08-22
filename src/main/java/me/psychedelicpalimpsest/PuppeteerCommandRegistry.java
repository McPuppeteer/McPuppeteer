/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;

import java.lang.reflect.Constructor;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import org.reflections.Reflections;

public class PuppeteerCommandRegistry {
  public static final Map<String, BaseCommand> COMMAND_MAP = new HashMap<>();
  public static final Map<String, String> COMMAND_DESC_MAP = new HashMap<>();
  public static final Map<String, String[]> COMMAND_REQUIREMENTS_MAP = new HashMap<>();
  public static final Map<String, BaseCommand.CommandContext> COMMAND_CONTEXT_MAP = new HashMap<>();
  public static final List<BaseCommand> COMMANDS = new ArrayList<>();

  static {
    Reflections reflections = new Reflections("me.psychedelicpalimpsest");
    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(PuppeteerCommand.class);

    for (Class<?> aClass : annotated) {
      BaseCommand command = null;

      String commandName = aClass.getAnnotation(PuppeteerCommand.class).cmd();
      String commandDesc = aClass.getAnnotation(PuppeteerCommand.class).description();
      BaseCommand.CommandContext commandContext =
          aClass.getAnnotation(PuppeteerCommand.class).cmd_context();
      String[] requirements = aClass.getAnnotation(PuppeteerCommand.class).mod_requirements();

      boolean canRun = true;
      for (String r : requirements)
        canRun &= FabricLoader.getInstance().getModContainer(r).isPresent();

      /* We need to find a no argument constructor, then create an object with it */
      if (canRun)
        for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
          if (constructor.getParameterCount() != 0) continue;
          constructor.setAccessible(true);

          try {
            Object instance = constructor.newInstance();
            command = (BaseCommand) instance;

            break;
          } catch (Exception e) {
            LOGGER.error(
                "Error instantiating "
                    + aClass.getName()
                    + ", are you sure it implements baseCommand?",
                e);
          }
        }
      if (command == null && canRun) {
        LOGGER.error(
            "Error instantiating "
                + aClass.getName()
                + ", A puppeteer-command annotated class MUST have a no-arg constructor!");
      }

      COMMAND_MAP.put(commandName, command);
      COMMAND_REQUIREMENTS_MAP.put(commandName, requirements);
      COMMAND_DESC_MAP.put(commandName, commandDesc);
      COMMAND_CONTEXT_MAP.put(commandName, commandContext);
      COMMANDS.add(command);
    }
  }
}
