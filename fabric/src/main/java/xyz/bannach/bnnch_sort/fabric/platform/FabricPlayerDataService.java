package xyz.bannach.bnnch_sort.fabric.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.services.IPlayerDataService;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class FabricPlayerDataService implements IPlayerDataService {

    @Override
    public SortPreference getPreference(Player player) {
        return FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .getPreference(player.getUUID());
    }

    @Override
    public void setPreference(Player player, SortPreference preference) {
        FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .setPreference(player.getUUID(), preference);
    }

    @Override
    public LockedSlots getLockedSlots(Player player) {
        return FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .getLockedSlots(player.getUUID());
    }

    @Override
    public void setLockedSlots(Player player, LockedSlots slots) {
        FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .setLockedSlots(player.getUUID(), slots);
    }
}
