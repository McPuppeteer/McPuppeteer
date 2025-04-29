package me.psychedelicpalimpsest;

import java.lang.annotation.*;

/**
 * Register your command with the PuppeteerCommandRegistry. Keep in mind,
 * you MUST implement BaseCommand, and have a no argument constructor.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PuppeteerCommand {
    String cmd();
    boolean needs_baritone() default false;
}