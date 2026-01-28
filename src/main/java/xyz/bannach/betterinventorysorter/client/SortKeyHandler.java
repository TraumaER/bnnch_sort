package xyz.bannach.betterinventorysorter.client;

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
import xyz.bannach.betterinventorysorter.network.CyclePreferencePayload;
import xyz.bannach.betterinventorysorter.network.SortRequestPayload;
import xyz.bannach.betterinventorysorter.server.SortHandler;

public class SortKeyHandler {

    public static final KeyMapping SORT_KEY = new KeyMapping(
            "key.betterinventorysorter.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.betterinventorysorter"
    );

    public static final KeyMapping CYCLE_PREFERENCE_KEY = new KeyMapping(
            "key.betterinventorysorter.cycle_preference",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.betterinventorysorter"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SORT_KEY);
        event.register(CYCLE_PREFERENCE_KEY);
    }

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
        if (slot.container instanceof CraftingContainer) {
            return false;
        }
        return true;
    }

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