package dev.schlaubi.mikbot.plugin.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pf4j.PluginWrapper;

import java.util.Objects;

/**
 * Main class of a plugin.
 *
 * @see PluginMain
 */
public abstract class Plugin extends org.pf4j.Plugin implements PluginInterface {
    @Nullable
    private final PluginContext context;

    /**
     * Fall back constructor
     *
     * @param wrapper the {@link PluginWrapper} provided by the plugin engine
     * @see Plugin#Plugin(PluginContext)
     * @deprecated Deprecated by PF4J (Use {@link Plugin#Plugin(PluginContext)} instead
     */
    @Deprecated
    public Plugin(@NotNull final PluginWrapper wrapper) {
        super(wrapper);
        this.context = null;
    }

    /**
     * Constructor of a plugin.
     *
     * @param context the {@link PluginContext} provided by the engine
     */
    public Plugin(@NotNull final PluginContext context) {
        super();
        Objects.requireNonNull(context, "Context needs not to be null");
        this.context = context;
    }

    /**
     * Getter for {@link PluginContext}.
     *
     * @return the context or {@code null} if it is a legacy plugin
     * @see Plugin#isLegacyPlugin()
     */
    @Nullable
    public PluginContext getContext() {
        return context;
    }

    /**
     * Checks whether the plugin is running in legacy mode.
     *
     * @return whether the plugin is running in legacy mode or not
     */
    public boolean isLegacyPlugin() {
        return context == null;
    }

    /**
     * Getter for {@link PluginContext}.
     *
     * @return the context or {@code null}
     * @see Plugin#isLegacyPlugin()
     * @throws IllegalStateException if this is a legacy plugin
     */
    @NotNull
    public PluginContext getContextSafe() {
        if (context == null) {
            throw new IllegalStateException("This plugin is a legacy plugin");
        }
        return context;
    }
}
