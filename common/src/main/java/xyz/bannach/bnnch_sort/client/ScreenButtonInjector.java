package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import xyz.bannach.bnnch_sort.CommonConfig;
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
 * <p>Button injection can be disabled via the {@link CommonConfig#showSortButton} setting.
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>Button injection only occurs on the client.
 *
 * @see SortButton
 * @see CommonConfig#showSortButton
 * @see SortHandler#getTargetSlots
 * @since 1.0.0
 */
public class ScreenButtonInjector {

  /** Private constructor to prevent instantiation of this utility class. */
  private ScreenButtonInjector() {}

  /**
   * Creates a sort button for the given container screen, if applicable.
   *
   * <p>If the screen is a supported container type and the button is enabled in config, a
   * {@link SortButton} is returned. Returns null if the screen should not receive a button.
   *
   * <p>This method uses generic container detection: any menu with sortable container slots will
   * receive a sort button, automatically supporting modded containers like MetalBarrels,
   * Sophisticated Storage, and others without requiring hardcoded menu type checks.
   *
   * @param screen the container screen being initialized
   * @return a new {@link SortButton} to add, or null if not applicable
   */
  public static SortButton createButton(AbstractContainerScreen<?> screen) {
    if (!CommonConfig.showSortButton) {
      return null;
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
      return null;
    }

    return new SortButton(screen, sortRegion);
  }
}
