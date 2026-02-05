package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.Config;

/**
 * Client-side event handlers for screen interactions and rendering.
 *
 * <p>This class listens to NeoForge screen events on the client side and delegates to the
 * appropriate handler classes. It handles keyboard input, mouse input, screen initialization, and
 * overlay rendering.
 *
 * <h2>Handled Events</h2>
 *
 * <ul>
 *   <li>{@link ScreenEvent.KeyPressed.Pre} - Keyboard input for sort/cycle keybinds
 *   <li>{@link ScreenEvent.MouseButtonPressed.Pre} - Mouse input for sort/cycle keybinds
 *   <li>{@link ScreenEvent.Init.Post} - Screen initialization for button injection
 *   <li>{@link ScreenEvent.Render.Post} - Screen rendering for feedback overlay
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>This class is only loaded on the client via {@link Dist#CLIENT}.
 *
 * @see SortKeyHandler
 * @see ScreenButtonInjector
 * @see SortFeedback
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID, value = Dist.CLIENT)
public class ClientEvents {

  /** Private constructor to prevent instantiation of this utility class. */
  private ClientEvents() {}

  /**
   * Handles keyboard input events on screens.
   *
   * <p>Delegates to {@link SortKeyHandler#onKeyPressed(ScreenEvent.KeyPressed.Pre)} for processing
   * sort and preference cycle keybinds.
   *
   * @param event the key pressed event
   */
  @SubscribeEvent
  public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
    SortKeyHandler.onKeyPressed(event);
  }

  /**
   * Handles mouse button input events on screens.
   *
   * <p>Delegates to {@link SortKeyHandler#onMouseClicked(ScreenEvent.MouseButtonPressed.Pre)} for
   * processing sort and preference cycle keybinds bound to mouse buttons.
   *
   * @param event the mouse button pressed event
   */
  @SubscribeEvent
  public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
    if (SlotLockInputHandler.onMouseClicked(event)) {
      event.setCanceled(true);
      return;
    }
    SortKeyHandler.onMouseClicked(event);
  }

  /**
   * Handles screen initialization events.
   *
   * <p>Delegates to {@link ScreenButtonInjector#onScreenInit(ScreenEvent.Init.Post)} to inject sort
   * buttons into supported container screens.
   *
   * @param event the screen initialization event
   */
  @SubscribeEvent
  public static void onScreenInit(ScreenEvent.Init.Post event) {
    ScreenButtonInjector.onScreenInit(event);
  }

  /**
   * Appends lock tooltip text to item tooltips for items in locked player inventory slots.
   *
   * <p>This complements {@link SlotLockRenderer#renderLockTooltip}, which handles empty locked
   * slots. Together they ensure the lock tooltip is visible on all locked slots per FR-LOCK-003a.
   *
   * @param event the item tooltip event
   */
  @SubscribeEvent
  public static void onItemTooltip(ItemTooltipEvent event) {
    if (!Config.showLockTooltip) {
      return;
    }
    if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)) {
      return;
    }
    Slot hoveredSlot = screen.getSlotUnderMouse();
    if (hoveredSlot == null || !(hoveredSlot.container instanceof Inventory)) {
      return;
    }
    int containerSlot = hoveredSlot.getContainerSlot();
    if (containerSlot < 0 || containerSlot > 35) {
      return;
    }
    if (!ClientLockedSlotsCache.isLocked(containerSlot)) {
      return;
    }
    Component modifierName = Component.translatable(Config.lockModifierKey.getTranslationKey());
    event.getToolTip().add(Component.translatable("tooltip.bnnch_sort.slot_locked", modifierName));
  }

  /**
   * Handles screen render events to display feedback overlays.
   *
   * <p>Renders the current feedback message (if any) as a centered overlay at the top of the screen
   * with a semi-transparent background.
   *
   * @param event the screen render event
   */
  @SubscribeEvent
  public static void onScreenRender(ScreenEvent.Render.Post event) {
    if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
      GuiGraphics guiGraphics = event.getGuiGraphics();
      SlotLockRenderer.renderLockOverlays(screen, guiGraphics);
      SlotLockRenderer.renderLockTooltip(screen, guiGraphics, event.getMouseX(), event.getMouseY());
    }

    Component message = SortFeedback.getDisplayMessage();
    if (message == null) {
      return;
    }

    GuiGraphics guiGraphics = event.getGuiGraphics();
    var font = Minecraft.getInstance().font;
    int screenWidth = event.getScreen().width;
    int textWidth = font.width(message);
    int x = (screenWidth - textWidth) / 2;
    int y = 10;

    guiGraphics.fill(x - 4, y - 2, x + textWidth + 4, y + font.lineHeight + 2, 0xAA000000);
    guiGraphics.drawString(font, message, x, y, 0xFFFFFF);
  }
}
