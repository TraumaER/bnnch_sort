package xyz.bannach.bnnch_sort.services;

import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public interface IPlayerDataService {
    SortPreference getPreference(Player player);
    void setPreference(Player player, SortPreference preference);
    LockedSlots getLockedSlots(Player player);
    void setLockedSlots(Player player, LockedSlots slots);
}
