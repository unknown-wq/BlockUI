package com.ldtteam.blockui.util;

import com.ldtteam.blockui.mod.Log;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import java.util.Objects;

/**
 * Utility class for throwing errors which is safe during production.
 */
public class SafeError
{
    /**
     * Safe error throw call that only throws an exception during development, but logs an error in production instead so no crashes to desktop may occur.
     *
     * @param exception the exception instance.
     */
    public static void throwInDev(final RuntimeException exception)
    {
        // NeoForge FMLEnvironment.isProduction() has no Fabric counterpart; the equivalent question
        // "am I in a dev workspace?" is FabricLoader#isDevelopmentEnvironment (inverted).
        if (FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            throw Util.pauseInIde(exception);
        }
        else
        {
            Log.getLogger().error(exception.getMessage(), exception);
        }
    }

    /**
     * @param value        the object reference to check for nullity
     * @param errorMessage detail message to be used in the event that a {@code NullPointerException} is thrown
     * @see Objects#requireNonNull(Object, String)
     */
    public static void requireNonNull(final Object value, final String errorMessage)
    {
        if (value == null)
        {
            throwInDev(new NullPointerException(errorMessage));
        }
    }

    /**
     * @param value        the object reference to check for nullity
     * @param defaultValue default value for production environment
     * @param errorMessage detail message to be used in the event that a {@code NullPointerException} is thrown
     * @see Objects#requireNonNull(Object, String)
     */
    public static <T> T requireNonNull(final T value, final T defaultValue, final String errorMessage)
    {
        if (value == null)
        {
            throwInDev(new NullPointerException(errorMessage));
            return defaultValue;
        }
        return value;
    }
}
