package xyz.bannach.bnnch_sort.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import xyz.bannach.bnnch_sort.Config;

/**
 * Renders lock overlays and tooltips on locked inventory slots.
 *
 * <h2>Side: Client-only</h2>
 *
 * @since 1.1.0
 */
public class SlotLockRenderer {

  private SlotLockRenderer() {}

  /**
   * Renders tinted overlays on all locked player inventory slots.
   *
   * @param screen the container screen being rendered
   * @param guiGraphics the graphics context
   */
  public static void renderLockOverlays(
      AbstractContainerScreen<?> screen, GuiGraphics guiGraphics) {
    int guiLeft = screen.getGuiLeft();
    int guiTop = screen.getGuiTop();

    for (Slot slot : screen.getMenu().slots) {
      if (!(slot.container instanceof Inventory)) {
        continue;
      }
      int containerSlot = slot.getContainerSlot();
      if (containerSlot < 0 || containerSlot > 35) {
        continue;
      }
      if (ClientLockedSlotsCache.isLocked(containerSlot)) {
        int x = guiLeft + slot.x;
        int y = guiTop + slot.y;
        guiGraphics.fill(x, y, x + 16, y + 16, Config.lockTintColor);
      }
    }
  }

  /**
   * Renders a tooltip on hovered empty locked slots.
   *
   * <p>Only renders when the hovered slot is a locked player inventory slot that is empty, to avoid
   * overlapping with item tooltips.
   *
   * @param screen the container screen being rendered
   * @param guiGraphics the graphics context
   * @param mouseX the mouse X position
   * @param mouseY the mouse Y position
   */
  public static void renderLockTooltip(
      AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (!Config.showLockTooltip) {
      return;
    }

    Slot hoveredSlot = screen.getSlotUnderMouse();
    if (hoveredSlot == null) {
      return;
    }
    if (!(hoveredSlot.container instanceof Inventory)) {
      return;
    }
    int containerSlot = hoveredSlot.getContainerSlot();
    if (containerSlot < 0 || containerSlot > 35) {
      return;
    }
    if (!ClientLockedSlotsCache.isLocked(containerSlot)) {
      return;
    }

    // Only show tooltip when slot is empty to avoid overlap with item tooltip
    if (!hoveredSlot.getItem().isEmpty()) {
      return;
    }

    Component modifierName =
        Component.translatable(Config.lockModifierKey.getTranslationKey());
    Component tooltip =
        Component.translatable("tooltip.bnnch_sort.slot_locked", modifierName);
    guiGraphics.renderTooltip(
        screen.getMinecraft().font, List.of(tooltip), java.util.Optional.empty(), mouseX, mouseY);
  }
}
