package xyz.bannach.bnnch_sort.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.server.SortHandler;

/**
 * Manages keybinding registration and input handling for sort operations.
 *
 * <p>This class defines the mod's keybindings and handles keyboard/mouse input
 * on container screens. It determines which inventory region to sort based on
 * the slot under the cursor and sends the appropriate network packet.</p>
 *
 * <h2>Default Keybindings</h2>
 * <ul>
 *   <li><strong>R</strong> ({@link #SORT_KEY}) - Sort the inventory region under the cursor</li>
 *   <li><strong>P</strong> ({@link #CYCLE_PREFERENCE_KEY}) - Cycle through sort preferences</li>
 * </ul>
 *
 * <h2>Region Detection</h2>
 * <p>The sort region is determined by the slot under the mouse cursor:</p>
 * <ul>
 *   <li>Container slots → {@link SortHandler#REGION_CONTAINER}</li>
 *   <li>Player inventory slots 9-35 → {@link SortHandler#REGION_PLAYER_MAIN}</li>
 *   <li>Player inventory slots 0-8 → {@link SortHandler#REGION_PLAYER_HOTBAR}</li>
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 * <p>Keybinding handling occurs on the client; sort requests are sent to the server.</p>
 *
 * @since 1.0.0
 * @see SortRequestPayload
 * @see CyclePreferencePayload
 * @see SortHandler
 */
public class SortKeyHandler {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SortKeyHandler() {}

    /**
     * Keybinding for triggering inventory sort.
     * <p>Default: R key</p>
     */
    public static final KeyMapping SORT_KEY = new KeyMapping(
            "key.bnnch_sort.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.bnnch_sort"
    );

    /**
     * Keybinding for cycling sort preferences.
     * <p>Default: P key</p>
     */
    public static final KeyMapping CYCLE_PREFERENCE_KEY = new KeyMapping(
            "key.bnnch_sort.cycle_preference",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.bnnch_sort"
    );

    /**
     * Registers the mod's keybindings with the game.
     *
     * <p>Called during client initialization via {@link ClientModBusEvents}.</p>
     *
     * @param event the key mapping registration event
     */
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SORT_KEY);
        event.register(CYCLE_PREFERENCE_KEY);
    }

    /**
     * Handles keyboard input on container screens.
     *
     * <p>Checks if the pressed key matches either the sort or cycle preference keybind
     * and performs the appropriate action. The event is canceled if a keybind matches.</p>
     *
     * @param event the key pressed event
     */
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
        if (CYCLE_PREFERENCE_KEY.isActiveAndMatches(key)) {
            PacketDistributor.sendToServer(new CyclePreferencePayload());
            SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
            event.setCanceled(true);
            return;
        }
        if (SORT_KEY.isActiveAndMatches(key)) {
            handleSortInput(screen);
            event.setCanceled(true);
        }
    }

    /**
     * Handles mouse button input on container screens.
     *
     * <p>Checks if the clicked mouse button matches either the sort or cycle preference
     * keybind and performs the appropriate action. The event is canceled if a keybind matches.</p>
     *
     * @param event the mouse button pressed event
     */
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
        if (CYCLE_PREFERENCE_KEY.isActiveAndMatches(mouseKey)) {
            PacketDistributor.sendToServer(new CyclePreferencePayload());
            SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
            event.setCanceled(true);
            return;
        }
        if (SORT_KEY.isActiveAndMatches(mouseKey)) {
            handleSortInput(screen);
            event.setCanceled(true);
        }
    }

    /**
     * Processes a sort input action on a container screen.
     *
     * <p>Determines the region from the hovered slot, validates it's sortable,
     * and sends a sort request to the server. Also displays feedback to the player.</p>
     *
     * @param screen the container screen receiving input
     */
    private static void handleSortInput(AbstractContainerScreen<?> screen) {
        Slot hoveredSlot = screen.getSlotUnderMouse();
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

        PacketDistributor.sendToServer(new SortRequestPayload(region));
        SortFeedback.showSorted(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
    }

    /**
     * Checks if a slot is eligible for sorting.
     *
     * <p>A slot is sortable if:</p>
     * <ul>
     *   <li>It uses the base {@link Slot} class (not a special subclass)</li>
     *   <li>It's not an armor or offhand slot (inventory slots 36+)</li>
     *   <li>It's not part of a crafting grid</li>
     * </ul>
     *
     * @param slot the slot to check
     * @return true if the slot can be sorted, false otherwise
     */
    private static boolean isSlotSortable(Slot slot) {
        // Reject special slot subclasses (ResultSlot, FurnaceResultSlot, FurnaceFuelSlot, ArmorSlot, etc.)
        if (slot.getClass() != Slot.class) {
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