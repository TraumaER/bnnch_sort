package xyz.bannach.bnnch_sort.platform;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.services.INetworkHandler;

public class NeoForgeNetworkHandler implements INetworkHandler {

    @Override
    public void sendToServer(SortRequestPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToServer(CyclePreferencePayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToServer(ToggleLockPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
