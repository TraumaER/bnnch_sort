package xyz.bannach.bnnch_sort.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;

/**
 * A clickable button widget that triggers inventory sorting.
 *
 * <p>This button is injected into supported container screens by {@link ScreenButtonInjector}. It
 * displays a custom texture icon and shows a tooltip with the current sort preferences when
 * hovered. Clicking the button sends a sort request to the server.
 *
 * <h2>Positioning</h2>
 *
 * <p>The button positions itself to the right of the parent screen's GUI area, updating its
 * position each frame to handle screen resizing.
 *
 * <h2>Appearance</h2>
 *
 * <ul>
 *   <li>Size: 16x16 pixels
 *   <li>Background: Semi-transparent dark gray
 *   <li>Icon: Custom texture from mod resources
 *   <li>Tooltip: Shows current sort method and order when hovered
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>This widget only exists on the client.
 *
 * @see ScreenButtonInjector
 * @see SortRequestPayload
 * @since 1.0.0
 */
public class SortButton extends Button {

  /** The texture resource location for the button icon. */
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(BnnchSort.MODID, "textures/gui/sort_button.png");

  /** The size of the button in pixels (both width and height). */
  private static final int SIZE = 16;

  /** The parent container screen this button is attached to. */
  private final AbstractContainerScreen<?> parentScreen;

  /** The inventory region this button will sort when clicked. */
  private final int sortRegion;

  /**
   * Constructs a new sort button for the given screen and region.
   *
   * <p>The button's position is set dynamically during rendering based on the parent screen's GUI
   * position.
   *
   * @param parentScreen the container screen this button is attached to
   * @param sortRegion the inventory region to sort (see {@link
   *     xyz.bannach.bnnch_sort.server.SortHandler})
   */
  public SortButton(AbstractContainerScreen<?> parentScreen, int sortRegion) {
    super(0, 0, SIZE, SIZE, Component.empty(), b -> {}, DEFAULT_NARRATION);
    this.parentScreen = parentScreen;
    this.sortRegion = sortRegion;
  }

  /**
   * Handles button press by sending a sort request to the server.
   *
   * <p>Also displays visual feedback to the player showing the current sort settings.
   */
  @Override
  public void onPress() {
    PacketDistributor.sendToServer(new SortRequestPayload(sortRegion));
    SortFeedback.showSorted(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
  }

  /**
   * Renders the button widget.
   *
   * <p>This method updates the button's position based on the parent screen, draws the background
   * and icon texture, and renders a tooltip when hovered.
   *
   * @param guiGraphics the graphics context for rendering
   * @param mouseX the current mouse X position
   * @param mouseY the current mouse Y position
   * @param partialTick the partial tick for smooth animations
   */
  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    setX(parentScreen.getGuiLeft() + parentScreen.getXSize() + 2);
    setY(parentScreen.getGuiTop());

    // Background
    guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xCC222222);

    // Texture icon
    guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

    // Tooltip on hover
    if (isHovered) {
      Component status =
          Component.translatable(
              "tooltip.bnnch_sort.sort_button",
              Component.translatable(ClientPreferenceCache.getMethod().getTranslationKey()),
              Component.translatable(ClientPreferenceCache.getOrder().getTranslationKey()));
      guiGraphics.renderTooltip(parentScreen.getMinecraft().font, status, mouseX, mouseY);
    }
  }
}
