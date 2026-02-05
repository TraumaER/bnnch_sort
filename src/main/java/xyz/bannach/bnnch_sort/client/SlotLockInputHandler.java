package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.ModifierKey;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;

/**
 * Client-side input handler for slot locking via modifier+click.
 *
 * <h2>Side: Client-only</h2>
 *
 * @since 1.1.0
 */
public class SlotLockInputHandler {

  private SlotLockInputHandler() {}

  /**
   * Handles mouse click events for slot locking.
   *
   * <p>Returns true if the event was consumed (slot lock toggled), false otherwise.
   *
   * @param event the mouse button pressed event
   * @return true if the click was consumed
   */
  public static boolean onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
    if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
      return false;
    }

    // Only left click
    if (event.getButton() != 0) {
      return false;
    }

    // Check modifier key
    if (!isModifierDown()) {
      return false;
    }

    // Check hovered slot is player inventory (0-35)
    Slot hoveredSlot = screen.getSlotUnderMouse();
    if (hoveredSlot == null) {
      return false;
    }
    if (!(hoveredSlot.container instanceof Inventory)) {
      return false;
    }
    int slotIndex = hoveredSlot.getContainerSlot();
    if (slotIndex < 0 || slotIndex > 35) {
      return false;
    }

    // Send toggle to server
    PacketDistributor.sendToServer(new ToggleLockPayload(slotIndex));

    // Optimistic client update for immediate feedback
    ClientLockedSlotsCache.toggleLocal(slotIndex);

    // Play click sound
    Minecraft.getInstance()
        .getSoundManager()
        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));

    return true;
  }

  private static boolean isModifierDown() {
    ModifierKey key = Config.lockModifierKey;
    return switch (key) {
      case ALT -> Screen.hasAltDown();
      case CONTROL -> Screen.hasControlDown();
      case SHIFT -> Screen.hasShiftDown();
    };
  }
}
