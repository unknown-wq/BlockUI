package com.ldtteam.common.config;

/**
 * Client bouncer class
 * <p>
 * TODO(port-26.2): DISABLED - NeoForge's generated config screen ({@code IConfigScreenFactory} +
 * {@code ConfigurationScreen}) has no Fabric counterpart, and with {@code ModConfigSpec} gone (contract K4)
 * there is no spec to generate a screen from either. Nothing calls this any more.
 *
 * <pre>
 * import net.neoforged.fml.ModContainer;
 * import net.neoforged.neoforge.client.gui.ConfigurationScreen;
 * import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
 *
 * public class ClientConfigHelper
 * {
 *     static void registerClient(final ModContainer modContainer)
 *     {
 *         modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
 *     }
 * }
 * </pre>
 */
public class ClientConfigHelper
{
    private ClientConfigHelper()
    {
        // Intentionally left empty.
    }
}
