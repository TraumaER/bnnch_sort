package xyz.bannach.bnnch_sort.platform;

import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.services.IPlayerDataService;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class NeoForgePlayerDataService implements IPlayerDataService {

    @Override
    public SortPreference getPreference(Player player) {
        return player.getData(ModAttachments.SORT_PREFERENCE);
    }

    @Override
    public void setPreference(Player player, SortPreference preference) {
        player.setData(ModAttachments.SORT_PREFERENCE, preference);
    }

    @Override
    public LockedSlots getLockedSlots(Player player) {
        return player.getData(ModAttachments.LOCKED_SLOTS);
    }

    @Override
    public void setLockedSlots(Player player, LockedSlots slots) {
        player.setData(ModAttachments.LOCKED_SLOTS, slots);
    }
}
