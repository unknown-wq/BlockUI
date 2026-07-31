package com.ldtteam.common.config;

import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

/**
 * Stand-in for NeoForge's {@code net.neoforged.neoforge.common.ModConfigSpec.ConfigValue} and friends.
 * <p>
 * <b>Port contract K4 (a §10 cut).</b> {@code ModConfigSpec} has no counterpart in Fabric and none in vanilla:
 * NightConfig, the TOML file, the per-world server config and the config screen all disappear with it. What is
 * kept is exactly the call-site shape the dependent mods rely on - {@code XXX.get()}, {@code XXX.set(v)} and
 * the {@code BooleanValue}/{@code IntValue}/... nesting - backed by a plain in-memory field holding the old
 * TOML default. Values are therefore <b>not persisted and not synchronised</b> between client and server.
 * <p>
 * The nesting mirrors {@code ModConfigSpec} one-to-one, so a dependent mod only has to rewrite the import:
 * {@code net.neoforged.neoforge.common.ModConfigSpec} -&gt; {@code com.ldtteam.common.config.ConfigValue}.
 *
 * @param <T> value type
 */
public class ConfigValue<T> implements Supplier<T>
{
    private final String path;
    private final String translationKey;
    @Nullable
    private final String comment;
    private final T defaultValue;

    private T value;

    ConfigValue(final String path, final String translationKey, @Nullable final String comment, final T defaultValue)
    {
        this.path = path;
        this.translationKey = translationKey;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public T get()
    {
        return value;
    }

    public void set(final T newValue)
    {
        this.value = newValue;
    }

    public T getDefault()
    {
        return defaultValue;
    }

    /**
     * Dot separated config path, eg. {@code category.key}.
     */
    public String getPath()
    {
        return path;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }

    @Nullable
    public String getComment()
    {
        return comment;
    }

    /**
     * No-op: there is no backing file to write to.
     */
    public void save()
    {
        // Intentionally left empty - see class javadoc.
    }

    /**
     * No-op: the value is the cache.
     */
    public void clearCache()
    {
        // Intentionally left empty - see class javadoc.
    }

    @Override
    public String toString()
    {
        return path + " = " + value;
    }

    public static class BooleanValue extends ConfigValue<Boolean>
    {
        BooleanValue(final String path, final String translationKey, @Nullable final String comment, final boolean defaultValue)
        {
            super(path, translationKey, comment, defaultValue);
        }

        public boolean getAsBoolean()
        {
            return get();
        }
    }

    public static class IntValue extends ConfigValue<Integer>
    {
        private final int min;
        private final int max;

        IntValue(final String path,
            final String translationKey,
            @Nullable final String comment,
            final int defaultValue,
            final int min,
            final int max)
        {
            super(path, translationKey, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Integer newValue)
        {
            super.set(Math.clamp(newValue.longValue(), min, max));
        }

        public int getAsInt()
        {
            return get();
        }
    }

    public static class LongValue extends ConfigValue<Long>
    {
        private final long min;
        private final long max;

        LongValue(final String path,
            final String translationKey,
            @Nullable final String comment,
            final long defaultValue,
            final long min,
            final long max)
        {
            super(path, translationKey, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Long newValue)
        {
            super.set(Math.clamp(newValue, min, max));
        }

        public long getAsLong()
        {
            return get();
        }
    }

    public static class DoubleValue extends ConfigValue<Double>
    {
        private final double min;
        private final double max;

        DoubleValue(final String path,
            final String translationKey,
            @Nullable final String comment,
            final double defaultValue,
            final double min,
            final double max)
        {
            super(path, translationKey, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(final Double newValue)
        {
            super.set(Math.clamp(newValue, min, max));
        }

        public double getAsDouble()
        {
            return get();
        }
    }

    public static class EnumValue<V extends Enum<V>> extends ConfigValue<V>
    {
        EnumValue(final String path, final String translationKey, @Nullable final String comment, final V defaultValue)
        {
            super(path, translationKey, comment, defaultValue);
        }
    }

    /**
     * Mirror of {@code ModConfigSpec.RestartType}. Nothing acts on it any more - there is no config file whose
     * reload could require a restart - but it is kept so {@code requires(...)} call sites still compile.
     */
    public enum RestartType
    {
        NONE,
        WORLD,
        GAME;
    }

    /**
     * Mirror of {@code ModConfigSpec.Builder}, reduced to a token: it exists so the
     * {@code MyConfig(Builder builder)} constructor shape of the dependent mods survives.
     */
    public static final class Builder
    {
        public Builder()
        {
            // Intentionally left empty - see class javadoc.
        }
    }
}
