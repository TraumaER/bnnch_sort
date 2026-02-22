package xyz.bannach.bnnch_sort.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.server.SortHandler;
import xyz.bannach.bnnch_sort.services.Services;
import xyz.bannach.bnnch_sort.util.SlotUtils;

/**
 * Manages keybinding registration and input handling for sort operations.
 *
 * <p>This class defines the mod's keybindings and handles keyboard/mouse input on container
 * screens. It determines which inventory region to sort based on the slot under the cursor and
 * sends the appropriate network packet.
 *
 * <h2>Default Keybindings</h2>
 *
 * <ul>
 *   <li><strong>R</strong> ({@link #SORT_KEY}) - Sort the inventory region under the cursor
 *   <li><strong>P</strong> ({@link #CYCLE_PREFERENCE_KEY}) - Cycle through sort preferences
 * </ul>
 *
 * <h2>Region Detection</h2>
 *
 * <p>The sort region is determined by the slot under the mouse cursor:
 *
 * <ul>
 *   <li>Container slots → {@link SortHandler#REGION_CONTAINER}
 *   <li>Player inventory slots 9-35 → {@link SortHandler#REGION_PLAYER_MAIN}
 *   <li>Player inventory slots 0-8 → {@link SortHandler#REGION_PLAYER_HOTBAR}
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>Keybinding handling occurs on the client; sort requests are sent to the server.
 *
 * @see SortRequestPayload
 * @see CyclePreferencePayload
 * @see SortHandler
 * @since 1.0.0
 */
public class SortKeyHandler {

  /** Private constructor to prevent instantiation of this utility class. */
  private SortKeyHandler() {}

  /**
   * Keybinding for triggering inventory sort.
   *
   * <p>Default: R key
   */
  public static final KeyMapping SORT_KEY =
      new KeyMapping(
          "key.bnnch_sort.sort",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_R,
          "key.categories.bnnch_sort");

  /**
   * Keybinding for cycling sort preferences.
   *
   * <p>Default: P key
   */
  public static final KeyMapping CYCLE_PREFERENCE_KEY =
      new KeyMapping(
          "key.bnnch_sort.cycle_preference",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_P,
          "key.categories.bnnch_sort");

  /**
   * Handles keyboard input on container screens.
   *
   * <p>Checks if the pressed key matches either the sort or cycle preference keybind and performs
   * the appropriate action. Uses Post event to run after other mods (e.g., JEI) have had a chance
   * to process the key and cancel it if they're handling text input.
   *
   * <p>Keybinds are ignored when a text field has focus (e.g., vanilla rename screens).
   *
   * @param screen the screen on which the key was pressed
   * @param keyCode the GLFW key code of the pressed key
   * @param scanCode the platform-specific scan code of the pressed key
   * @return true if the event was consumed, false otherwise
   */
  public static boolean onKeyPressed(Screen screen, int keyCode, int scanCode) {
    if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
      return false;
    }

    // Don't process keybinds when typing in a text field (e.g., vanilla rename screens)
    if (containerScreen.getFocused() instanceof EditBox) {
      return false;
    }

    // Process our keybinds
    if (CYCLE_PREFERENCE_KEY.matches(keyCode, scanCode)) {
      Services.NETWORK.sendToServer(new CyclePreferencePayload());
      SortFeedback.showPreferenceChange(
          ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
      return true;
    }
    if (SORT_KEY.matches(keyCode, scanCode)) {
      handleSortInput(containerScreen);
      return true;
    }
    return false;
  }

  /**
   * Handles mouse button input on container screens.
   *
   * <p>Checks if the clicked mouse button matches either the sort or cycle preference keybind and
   * performs the appropriate action.
   *
   * @param screen the screen on which the mouse button was clicked
   * @param button the GLFW mouse button code
   * @return true if the event was consumed, false otherwise
   */
  public static boolean onMouseClicked(Screen screen, int button) {
    if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
      return false;
    }
    if (CYCLE_PREFERENCE_KEY.matchesMouse(button)) {
      Services.NETWORK.sendToServer(new CyclePreferencePayload());
      SortFeedback.showPreferenceChange(
          ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
      return true;
    }
    if (SORT_KEY.matchesMouse(button)) {
      handleSortInput(containerScreen);
      return true;
    }
    return false;
  }

  /**
   * Processes a sort input action on a container screen.
   *
   * <p>Determines the region from the hovered slot, validates it's sortable, and sends a sort
   * request to the server. Also displays feedback to the player.
   *
   * @param screen the container screen receiving input
   */
  private static void handleSortInput(AbstractContainerScreen<?> screen) {
    Slot hoveredSlot = screen.hoveredSlot;
    if (hoveredSlot == null) {
      return;
    }

    if (!isSlotSortable(hoveredSlot)) {
      return;
    }

    int region = determineRegion(hoveredSlot);
    AbstractContainerMenu menu = screen.getMenu();
    if (SortHandler.getTargetSlots(menu, region).isEmpty()) {
      return;
    }

    Services.NETWORK.sendToServer(new SortRequestPayload(region));
    SortFeedback.showSorted(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
  }

  /**
   * Checks if a slot is eligible for sorting.
   *
   * <p>A slot is sortable if:
   *
   * <ul>
   *   <li>It uses a sortable slot class ({@link Slot} or {@code SlotItemHandler})
   *   <li>It's not an armor or offhand slot (inventory slots 36+)
   *   <li>It's not part of a crafting grid
   * </ul>
   *
   * @param slot the slot to check
   * @return true if the slot can be sorted, false otherwise
   */
  private static boolean isSlotSortable(Slot slot) {
    // Reject special slot subclasses (ResultSlot, FurnaceResultSlot, FurnaceFuelSlot, ArmorSlot,
    // etc.)
    // Allow base Slot and NeoForge's SlotItemHandler (used by modded containers like MetalBarrels)
    if (!SlotUtils.isSortableSlotClass(slot)) {
      return false;
    }
    // Reject armor/offhand slots (Inventory slots outside 0-35)
    if (slot.container instanceof Inventory) {
      int index = slot.getContainerSlot();
      if (index < 0 || index > 35) {
        return false;
      }
    }
    // Reject crafting grid slots
    return !(slot.container instanceof CraftingContainer);
  }

  /**
   * Determines the sort region for a given slot.
   *
   * @param slot the slot to determine the region for
   * @return the region code (container, player main, or player hotbar)
   */
  private static int determineRegion(Slot slot) {
    if (!(slot.container instanceof Inventory)) {
      return SortHandler.REGION_CONTAINER;
    }
    int containerSlot = slot.getContainerSlot();
    if (containerSlot >= 0 && containerSlot <= 8) {
      return SortHandler.REGION_PLAYER_HOTBAR;
    } else if (containerSlot >= 9 && containerSlot <= 35) {
      return SortHandler.REGION_PLAYER_MAIN;
    }
    // Armor/offhand slots — should not reach here due to isSlotSortable guard
    return SortHandler.REGION_PLAYER_MAIN;
  }
}
