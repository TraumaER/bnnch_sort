package xyz.bannach.bnnch_sort.commands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * NeoForge event subscriber that wires the {@link RegisterCommandsEvent} to the platform-agnostic
 * {@link ModCommands#register} method.
 *
 * <h2>Side: NeoForge</h2>
 *
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID)
public class ModCommandsNeoForge {

  private ModCommandsNeoForge() {}

  /**
   * Registers all mod commands during the command registration event.
   *
   * @param event the command registration event
   */
  @SubscribeEvent
  public static void onRegisterCommands(RegisterCommandsEvent event) {
    ModCommands.register(
        event.getDispatcher(),
        () -> new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder),
        () -> Config.showSortButton);
  }
}
