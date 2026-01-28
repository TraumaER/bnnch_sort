package xyz.bannach.betterinventorysorter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

/**
 * Client-side mod bus event handlers.
 *
 * <p>This class listens to events on the mod event bus (as opposed to the game event bus)
 * that are specific to client-side initialization, such as key mapping registration.</p>
 *
 * <h2>Handled Events</h2>
 * <ul>
 *   <li>{@link RegisterKeyMappingsEvent} - Registers mod keybindings with the game</li>
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 * <p>This class is only loaded on the client via {@link Dist#CLIENT}.</p>
 *
 * @since 1.0.0
 * @see SortKeyHandler
 */
@EventBusSubscriber(modid = Betterinventorysorter.MODID, value = Dist.CLIENT)
public class ClientModBusEvents {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ClientModBusEvents() {}

    /**
     * Handles key mapping registration.
     *
     * <p>Delegates to {@link SortKeyHandler#register(RegisterKeyMappingsEvent)}
     * to register the sort and preference cycle keybindings.</p>
     *
     * @param event the key mapping registration event
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        SortKeyHandler.register(event);
    }
}
