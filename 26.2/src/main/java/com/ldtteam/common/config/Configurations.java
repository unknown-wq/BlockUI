package com.ldtteam.common.config;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.config.AbstractConfiguration.ConfigWatcher;
import com.ldtteam.common.config.ConfigValue.Builder;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mod root configuration.
 * <p>
 * <b>Port contract K4 (a §10 cut).</b> NeoForge's {@code ModConfig} / {@code ConfigTracker} /
 * {@code ModConfigEvent} have no Fabric or vanilla counterpart. The three configuration objects are still
 * built and still hand out {@link ConfigValue}s, but they are pure in-memory defaults: nothing is written to
 * disk, nothing is loaded from disk and nothing is synchronised to clients. Consequently the
 * {@code ModContainer} / {@code IEventBus} constructor parameters are gone, and {@code onConfigLoad} /
 * {@code onConfigReload} collapsed into a single eager watcher priming at construction time.
 */
public class Configurations<CLIENT extends AbstractConfiguration,
    SERVER extends AbstractConfiguration,
    COMMON extends AbstractConfiguration>
{
    /**
     * Loaded clientside, not synced
     */
    @Nullable
    private final CLIENT clientConfig;

    /**
     * Loaded serverside (per world), synced on connection
     */
    @Nullable
    private final SERVER serverConfig;

    /**
     * Loaded both sides, not synced
     */
    @Nullable
    private final COMMON commonConfig;

    private final AbstractConfiguration[] activeConfigs;

    /**
     * Builds configuration tree.
     *
     * @param clientFactory client config factory, may be null
     * @param serverFactory server config factory, may be null
     * @param commonFactory common config factory, may be null
     */
    public Configurations(@Nullable final Function<Builder, CLIENT> clientFactory,
        @Nullable final Function<Builder, SERVER> serverFactory,
        @Nullable final Function<Builder, COMMON> commonFactory)
    {
        final List<AbstractConfiguration> configs = new ArrayList<>();

        // dont create client classes on server to avoid class loading issues
        clientConfig = BlockUI.isClient() ? createConfig(clientFactory, configs) : null;
        serverConfig = createConfig(serverFactory, configs);
        commonConfig = createConfig(commonFactory, configs);

        activeConfigs = configs.toArray(AbstractConfiguration[]::new);

        // there is no config load event any more - prime the watchers with the defaults right away
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            cfg.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    @Nullable
    private <T extends AbstractConfiguration> T createConfig(@Nullable final Function<Builder, T> factory,
        final List<AbstractConfiguration> configs)
    {
        if (factory == null)
        {
            return null;
        }

        final T config = factory.apply(new Builder());
        configs.add(config);
        return config;
    }

    @Nullable
    public CLIENT getClient()
    {
        return clientConfig;
    }

    @Nullable
    public SERVER getServer()
    {
        return serverConfig;
    }

    @Nullable
    public COMMON getCommon()
    {
        return commonConfig;
    }

    /**
     * Setter wrapper so watchers are fine. This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     */
    public <T> void set(final ConfigValue<T> configValue, final T value)
    {
        configValue.set(value);
        configValue.save();
        onConfigValueEdit(configValue);
    }

    /**
     * This should be called from any code that manually changes ConfigValues using set functions. (Mostly done by settings UIs)
     *
     * @param configValue which config value was changed
     */
    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            for (final ConfigWatcher<?> configWatcher : cfg.watchers)
            {
                if (configWatcher.isSameForgeConfig(configValue))
                {
                    configWatcher.compareAndFireChangeEvent();
                }
            }
        }
    }
}
