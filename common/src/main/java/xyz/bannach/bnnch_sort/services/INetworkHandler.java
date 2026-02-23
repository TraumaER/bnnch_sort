package xyz.bannach.bnnch_sort.services;

import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;

public interface INetworkHandler {
    void sendToServer(SortRequestPayload payload);
    void sendToServer(CyclePreferencePayload payload);
    void sendToServer(ToggleLockPayload payload);
    void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload);
    void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload);
}
