package xyz.bannach.bnnch_sort.fabric.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import xyz.bannach.bnnch_sort.CommonConfig;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricPlayerDataManager extends SavedData {

    private static final String ID = "bnnch_sort_player_data";

    private static final Factory<FabricPlayerDataManager> FACTORY = new Factory<>(
        FabricPlayerDataManager::new,
        FabricPlayerDataManager::load,
        null
    );

    private final Map<UUID, SortPreference> preferences = new HashMap<>();
    private final Map<UUID, LockedSlots> lockedSlots = new HashMap<>();

    public static FabricPlayerDataManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public SortPreference getPreference(UUID uuid) {
        return preferences.getOrDefault(uuid,
            new SortPreference(CommonConfig.defaultSortMethod, CommonConfig.defaultSortOrder));
    }

    public void setPreference(UUID uuid, SortPreference pref) {
        preferences.put(uuid, pref);
        setDirty();
    }

    public LockedSlots getLockedSlots(UUID uuid) {
        return lockedSlots.getOrDefault(uuid, LockedSlots.EMPTY);
    }

    public void setLockedSlots(UUID uuid, LockedSlots slots) {
        lockedSlots.put(uuid, slots);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag prefsTag = new CompoundTag();
        preferences.forEach((uuid, pref) ->
            SortPreference.CODEC.encodeStart(NbtOps.INSTANCE, pref)
                .ifSuccess(t -> prefsTag.put(uuid.toString(), t)));
        tag.put("preferences", prefsTag);

        CompoundTag locksTag = new CompoundTag();
        lockedSlots.forEach((uuid, slots) ->
            LockedSlots.CODEC.encodeStart(NbtOps.INSTANCE, slots)
                .ifSuccess(t -> locksTag.put(uuid.toString(), t)));
        tag.put("lockedSlots", locksTag);

        return tag;
    }

    public static FabricPlayerDataManager load(CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider provider) {
        FabricPlayerDataManager manager = new FabricPlayerDataManager();

        CompoundTag prefsTag = tag.getCompound("preferences");
        for (String key : prefsTag.getAllKeys()) {
            SortPreference.CODEC.parse(NbtOps.INSTANCE, prefsTag.get(key))
                .ifSuccess(pref -> manager.preferences.put(UUID.fromString(key), pref));
        }

        CompoundTag locksTag = tag.getCompound("lockedSlots");
        for (String key : locksTag.getAllKeys()) {
            LockedSlots.CODEC.parse(NbtOps.INSTANCE, locksTag.get(key))
                .ifSuccess(slots -> manager.lockedSlots.put(UUID.fromString(key), slots));
        }

        return manager;
    }
}
