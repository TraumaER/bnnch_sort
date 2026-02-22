package xyz.bannach.bnnch_sort.fabric.server;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.CommonConfig;
import xyz.bannach.bnnch_sort.commands.ModCommands;
import xyz.bannach.bnnch_sort.fabric.platform.FabricPlayerDataManager;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.services.Services;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortOrder;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class FabricServerEvents {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> ModCommands.register(
                dispatcher,
                () -> new SortPreference(CommonConfig.defaultSortMethod, CommonConfig.defaultSortOrder),
                () -> CommonConfig.showSortButton));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            SortPreference pref = Services.PLAYER_DATA.getPreference(player);
            Services.NETWORK.sendToPlayer(player,
                new SyncPreferencePayload(pref.method(), pref.order()));
            LockedSlots locked = Services.PLAYER_DATA.getLockedSlots(player);
            Services.NETWORK.sendToPlayer(player, new SyncLockedSlotsPayload(locked.slots()));
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            FabricPlayerDataManager manager =
                FabricPlayerDataManager.get(newPlayer.serverLevel());
            manager.setPreference(newPlayer.getUUID(),
                manager.getPreference(oldPlayer.getUUID()));
            manager.setLockedSlots(newPlayer.getUUID(),
                manager.getLockedSlots(oldPlayer.getUUID()));
        });
    }
}
