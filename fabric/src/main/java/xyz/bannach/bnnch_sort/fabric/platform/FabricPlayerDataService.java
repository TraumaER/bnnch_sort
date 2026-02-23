package xyz.bannach.bnnch_sort.fabric.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.CommonConfig;
import xyz.bannach.bnnch_sort.services.IPlayerDataService;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class FabricPlayerDataService implements IPlayerDataService {

    @Override
    public SortPreference getPreference(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return new SortPreference(CommonConfig.defaultSortMethod, CommonConfig.defaultSortOrder);
        }
        return FabricPlayerDataManager.get(sp.serverLevel()).getPreference(player.getUUID());
    }

    @Override
    public void setPreference(Player player, SortPreference preference) {
        if (!(player instanceof ServerPlayer sp)) return;
        FabricPlayerDataManager.get(sp.serverLevel()).setPreference(player.getUUID(), preference);
    }

    @Override
    public LockedSlots getLockedSlots(Player player) {
        if (!(player instanceof ServerPlayer sp)) return LockedSlots.EMPTY;
        return FabricPlayerDataManager.get(sp.serverLevel()).getLockedSlots(player.getUUID());
    }

    @Override
    public void setLockedSlots(Player player, LockedSlots slots) {
        if (!(player instanceof ServerPlayer sp)) return;
        FabricPlayerDataManager.get(sp.serverLevel()).setLockedSlots(player.getUUID(), slots);
    }
}
