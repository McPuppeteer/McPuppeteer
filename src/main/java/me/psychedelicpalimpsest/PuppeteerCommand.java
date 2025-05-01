package me.psychedelicpalimpsest;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;
import java.util.List;

/**
 * Register your command with the PuppeteerCommandRegistry. Keep in mind,
 * you MUST implement BaseCommand, and have a no argument constructor.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PuppeteerCommand {
    String cmd();
    String description();
    @Nullable
    String[] mod_requirements() default {};
}