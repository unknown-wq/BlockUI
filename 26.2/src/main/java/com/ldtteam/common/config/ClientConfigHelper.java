package com.ldtteam.common.config;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

/**
 * Client bouncer class: everything in here touches a client-only Fabric API type, so it must never be loaded on
 * a dedicated server. {@link Configurations} only calls into it behind
 * {@link com.ldtteam.blockui.mod.BlockUI#isClient()}.
 * <p>
 * TODO(port-26.2): the generated config <i>screen</i> is still gone - NeoForge's {@code IConfigScreenFactory} +
 * {@code ConfigurationScreen} have no Fabric counterpart, and with {@code ModConfigSpec} gone (contract K4)
 * there is no spec to generate one from either.
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

    /**
     * Flushes the configuration one last time when the client shuts down, so the tail of a settings-screen
     * session survives even if it lands inside the writer's debounce window.
     *
     * @param flush {@link Configurations#saveAll()} of the configuration tree being registered
     */
    static void registerClient(final Runnable flush)
    {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> flush.run());
    }
}
