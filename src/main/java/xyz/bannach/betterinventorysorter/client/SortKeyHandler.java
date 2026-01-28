package xyz.bannach.betterinventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import xyz.bannach.betterinventorysorter.network.CycleMethodPayload;
import xyz.bannach.betterinventorysorter.network.SortRequestPayload;
import xyz.bannach.betterinventorysorter.server.SortHandler;

public class SortKeyHandler {

    public static final KeyMapping SORT_KEY = new KeyMapping(
            "key.betterinventorysorter.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.betterinventorysorter"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SORT_KEY);
    }

    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (SORT_KEY.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
            if (Screen.hasShiftDown()) {
                PacketDistributor.sendToServer(new CycleMethodPayload(true));
                SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
                event.setCanceled(true);
                return;
            }
            if (Screen.hasControlDown()) {
                PacketDistributor.sendToServer(new CycleMethodPayload(false));
                SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
                event.setCanceled(true);
                return;
            }
            handleSortInput(screen);
            event.setCanceled(true);
        }
    }

    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (SORT_KEY.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) {
            if (Screen.hasShiftDown()) {
                PacketDistributor.sendToServer(new CycleMethodPayload(true));
                SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
                event.setCanceled(true);
                return;
            }
            if (Screen.hasControlDown()) {
                PacketDistributor.sendToServer(new CycleMethodPayload(false));
                SortFeedback.showPreferenceChange(ClientPreferenceCache.getMethod(), ClientPreferenceCache.getOrder());
                event.setCanceled(true);
                return;
            }
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