package xyz.bannach.bnnch_sort.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import xyz.bannach.bnnch_sort.BnnchSort;

/**
 * Client-side mod bus event handlers.
 *
 * <p>This class listens to events on the mod event bus (as opposed to the game event bus) that are
 * specific to client-side initialization, such as key mapping registration.
 *
 * <h2>Handled Events</h2>
 *
 * <ul>
 *   <li>{@link RegisterKeyMappingsEvent} - Registers mod keybindings with the game
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>This class is only loaded on the client via {@link Dist#CLIENT}.
 *
 * @see SortKeyHandler
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusEvents {

  /** Private constructor to prevent instantiation of this utility class. */
  private ClientModBusEvents() {}

  /**
   * Handles key mapping registration.
   *
   * <p>Registers the sort and preference cycle keybindings from {@link SortKeyHandler}.
   *
   * @param event the key mapping registration event
   */
  @SubscribeEvent
  public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(SortKeyHandler.SORT_KEY);
    event.register(SortKeyHandler.CYCLE_PREFERENCE_KEY);
  }
}
