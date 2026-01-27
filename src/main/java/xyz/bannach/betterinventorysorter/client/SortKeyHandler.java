package xyz.bannach.betterinventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class SortKeyHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        LOGGER.info("Sort key pressed on screen: {}", screen.getClass().getSimpleName());
        LOGGER.info("Menu type: {}", screen.getMenu().getClass().getSimpleName());
        LOGGER.info("Total slots: {}", screen.getMenu().slots.size());

        Slot hoveredSlot = screen.getSlotUnderMouse();
        if (hoveredSlot != null) {
            LOGGER.info("Hovered slot index: {}, container slot: {}, container: {}",
                    hoveredSlot.index, hoveredSlot.getContainerSlot(), hoveredSlot.container.getClass().getSimpleName());
        } else {
            LOGGER.info("No slot hovered");
        }
    }
}