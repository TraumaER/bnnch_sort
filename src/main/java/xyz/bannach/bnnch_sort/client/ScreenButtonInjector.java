package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.server.SortHandler;

/**
 * Injects sort buttons into supported container screens.
 *
 * <p>This class handles the automatic addition of {@link SortButton} widgets to container screens
 * during their initialization. The button is positioned to the right of the container GUI.
 *
 * <h2>Supported Screens</h2>
 *
 * <ul>
 *   <li>{@link ChestMenu} - Single and double chests
 *   <li>{@link ShulkerBoxMenu} - Shulker boxes
 *   <li>{@link InventoryMenu} - Player inventory screen
 * </ul>
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

    if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) {
      sortRegion = SortHandler.REGION_CONTAINER;
    } else if (menu instanceof InventoryMenu) {
      sortRegion = SortHandler.REGION_PLAYER_MAIN;
    } else {
      return;
    }

    SortButton sortButton = new SortButton(screen, sortRegion);
    event.addListener(sortButton);
  }
}
