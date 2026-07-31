package com.ldtteam.common.config;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.config.AbstractConfiguration.ConfigWatcher;
import com.ldtteam.common.config.ConfigValue.Builder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mod root configuration.
 * <p>
 * <b>Port contract K4 (a §10 cut), partially restored.</b> NeoForge's {@code ModConfig} /
 * {@code ConfigTracker} / {@code ModConfigEvent} have no Fabric or vanilla counterpart, so each of the three
 * configuration objects gets a {@link ConfigStore} instead: a plain {@code config/<modid>-<type>.toml} file that
 * is read once when the tree is built and written back, debounced, whenever a value changes.
 * <p>
 * That replaces the NeoForge lifecycle as follows:
 * <ul>
 * <li>{@code ModConfigEvent.Loading} -&gt; {@link ConfigStore#load()} immediately after the configuration object
 * is constructed, and always <em>before</em> the watchers are primed - otherwise the watchers would cache the
 * compiled-in defaults and the first genuine on-disk value would never fire a listener;</li>
 * <li>{@code ModConfigEvent.Reloading} -&gt; nothing. The file is not watched; the only source of a change is an
 * explicit {@link #set(ConfigValue, Object)} from game code, which already goes through the watchers;</li>
 * <li>{@code ConfigTracker} login sync -&gt; still cut. The server config is loaded per <i>installation</i>, not
 * per world, and is not shipped to clients.</li>
 * </ul>
 * The {@code ModContainer} / {@code IEventBus} constructor parameters remain gone.
 */
public class Configurations<CLIENT extends AbstractConfiguration,
    SERVER extends AbstractConfiguration,
    COMMON extends AbstractConfiguration>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurations.class);

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

    private final ConfigStore[] stores;

    /**
     * Builds configuration tree. The file each configuration is persisted to is named after the mod id its
     * {@link AbstractConfiguration} reports, i.e. the one it passes to
     * {@link AbstractConfiguration#AbstractConfiguration(Builder, String)}.
     *
     * @param clientFactory client config factory, may be null
     * @param serverFactory server config factory, may be null
     * @param commonFactory common config factory, may be null
     */
    public Configurations(@Nullable final Function<Builder, CLIENT> clientFactory,
        @Nullable final Function<Builder, SERVER> serverFactory,
        @Nullable final Function<Builder, COMMON> commonFactory)
    {
        this(null, clientFactory, serverFactory, commonFactory);
    }

    /**
     * Builds configuration tree, naming the files after an explicit mod id.
     *
     * @param modId         mod id used for {@code config/<modId>-<type>.toml}; when null it is taken from the
     *                      configuration objects themselves
     * @param clientFactory client config factory, may be null
     * @param serverFactory server config factory, may be null
     * @param commonFactory common config factory, may be null
     */
    public Configurations(@Nullable final String modId,
        @Nullable final Function<Builder, CLIENT> clientFactory,
        @Nullable final Function<Builder, SERVER> serverFactory,
        @Nullable final Function<Builder, COMMON> commonFactory)
    {
        final List<AbstractConfiguration> configs = new ArrayList<>();
        final List<ConfigStore> builtStores = new ArrayList<>();

        // dont create client classes on server to avoid class loading issues
        clientConfig = BlockUI.isClient()
            ? createConfig(clientFactory, ConfigStore.Type.CLIENT, modId, configs, builtStores)
            : null;
        serverConfig = createConfig(serverFactory, ConfigStore.Type.SERVER, modId, configs, builtStores);
        commonConfig = createConfig(commonFactory, ConfigStore.Type.COMMON, modId, configs, builtStores);

        activeConfigs = configs.toArray(AbstractConfiguration[]::new);
        stores = builtStores.toArray(ConfigStore[]::new);

        // every store has already been loaded by createConfig, so the watchers cache what is actually on disk
        // and the first genuine change still fires a listener
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            cfg.watchers.forEach(ConfigWatcher::cacheLastValue);
        }

        registerShutdownFlush();
    }

    @Nullable
    private <T extends AbstractConfiguration> T createConfig(@Nullable final Function<Builder, T> factory,
        final ConfigStore.Type type,
        @Nullable final String modId,
        final List<AbstractConfiguration> configs,
        final List<ConfigStore> builtStores)
    {
        if (factory == null)
        {
            return null;
        }

        final ConfigStore store = new ConfigStore(type);
        if (modId != null)
        {
            // an explicit id wins; otherwise AbstractConfiguration's constructor binds the one it was given
            store.bindModId(modId);
        }

        final T config = factory.apply(new Builder(store));
        configs.add(config);
        builtStores.add(store);

        // must happen here, before the caller primes the watchers
        store.load();

        return config;
    }

    /**
     * Writes every pending change of every configuration to disk, right now. Called automatically on shutdown;
     * exposed for a mod that wants a hard flush at some other point.
     */
    public void saveAll()
    {
        for (final ConfigStore store : stores)
        {
            store.flush();
        }
    }

    /**
     * The debounced writer can be up to {@link ConfigStore#DEBOUNCE_MILLIS} behind, so the last edits of a
     * session need a guaranteed flush. Both lifecycle events exist in fabric-api 0.154.2; the JVM hook is the
     * belt to their braces and also covers a mod that never opens a world.
     */
    private void registerShutdownFlush()
    {
        try
        {
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveAll());
            if (BlockUI.isClient())
            {
                // client-only class, kept behind the bouncer so a dedicated server never loads it
                ClientConfigHelper.registerClient(this::saveAll);
            }
        }
        catch (final RuntimeException | LinkageError e)
        {
            // no Fabric API around (datagen, unit tests) - the shutdown hook below still covers us
            LOGGER.warn("Could not hook the config flush onto the game lifecycle", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll, "ldtteam-config-flush"));
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
