package xyz.bannach.betterinventorysorter.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xyz.bannach.betterinventorysorter.Config;
import xyz.bannach.betterinventorysorter.server.SortHandler;

public class ScreenButtonInjector {

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!Config.showSortButton) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        var menu = screen.getMenu();
        int sortRegion;

        if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) {
            sortRegion = SortHandler.REGION_CONTAINER;
        } else if (menu instanceof InventoryMenu) {
            sortRegion = SortHandler.REGION_PLAYER_MAIN;
        } else {
            return;
        }

        SortButton sortButton = new SortButton(screen, sortRegion);
        event.addListener(sortButton);
    }
}
