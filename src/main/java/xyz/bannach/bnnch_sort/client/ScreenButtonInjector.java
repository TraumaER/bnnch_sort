package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.server.SortHandler;

/**
 * Injects sort buttons into supported container screens.
 *
 * <p>This class handles the automatic addition of {@link SortButton} widgets to container screens
 * during their initialization. The button is positioned to the right of the container GUI.
 *
 * <h2>Container Detection</h2>
 *
 * <p>Uses generic detection to support any container with sortable slots, including:
 *
 * <ul>
 *   <li>Vanilla containers - chests, shulker boxes
 *   <li>Modded containers - MetalBarrels, Sophisticated Storage, Iron Chests, etc.
 *   <li>Player inventory screen
 * </ul>
 *
 * <p>The detection automatically excludes containers with special slots (furnaces, crafting tables,
 * etc.) by leveraging {@link SortHandler#getTargetSlots}.
 *
 * <h2>Configuration</h2>
 *
 * <p>Button injection can be disabled via the {@link Config#showSortButton} setting.
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>Button injection only occurs on the client.
 *
 * @see SortButton
 * @see Config#showSortButton
 * @see SortHandler#getTargetSlots
 * @since 1.0.0
 */
public class ScreenButtonInjector {

  /** Private constructor to prevent instantiation of this utility class. */
  private ScreenButtonInjector() {}

  /**
   * Handles screen initialization to inject sort buttons.
   *
   * <p>Called after a screen is initialized. If the screen is a supported container type and the
   * button is enabled in config, a {@link SortButton} is added to the screen's widget list.
   *
   * <p>This method uses generic container detection: any menu with sortable container slots will
   * receive a sort button, automatically supporting modded containers like MetalBarrels,
   * Sophisticated Storage, and others without requiring hardcoded menu type checks.
   *
   * @param event the screen initialization event
   */
  public static void onScreenInit(ScreenEvent.Init.Post event) {
    if (!Config.showSortButton) {
      return;
    }
    if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
      return;
    }

    var menu = screen.getMenu();
    int sortRegion;

    // Try container region first (works for vanilla chests, shulker boxes, and any modded
    // containers)
    if (!SortHandler.getTargetSlots(menu, SortHandler.REGION_CONTAINER).isEmpty()) {
      sortRegion = SortHandler.REGION_CONTAINER;
    } else if (menu instanceof InventoryMenu) {
      // Fallback to player inventory for standalone inventory screen
      sortRegion = SortHandler.REGION_PLAYER_MAIN;
    } else {
      return;
    }

    SortButton sortButton = new SortButton(screen, sortRegion);
    event.addListener(sortButton);
  }
}
