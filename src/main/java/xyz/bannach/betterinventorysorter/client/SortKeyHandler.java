package xyz.bannach.betterinventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
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
            handleSortInput(screen);
            event.setCanceled(true);
        }
    }

    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (SORT_KEY.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) {
            handleSortInput(screen);
            event.setCanceled(true);
        }
    }

    private static void handleSortInput(AbstractContainerScreen<?> screen) {
        Slot hoveredSlot = screen.getSlotUnderMouse();
        if (hoveredSlot == null) {
            return;
        }

        int region = determineRegion(hoveredSlot);
        PacketDistributor.sendToServer(new SortRequestPayload(region));
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
        return SortHandler.REGION_CONTAINER;
    }
}