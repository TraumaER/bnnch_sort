package xyz.bannach.bnnch_sort.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import xyz.bannach.bnnch_sort.client.SortKeyHandler;
import xyz.bannach.bnnch_sort.fabric.client.FabricClientEvents;

public class BnnchSortFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SortKeyHandler.SORT_KEY);
        KeyBindingHelper.registerKeyBinding(SortKeyHandler.CYCLE_PREFERENCE_KEY);
        FabricClientEvents.register();
    }
}
