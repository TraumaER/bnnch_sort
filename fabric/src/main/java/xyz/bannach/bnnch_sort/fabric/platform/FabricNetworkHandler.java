package xyz.bannach.bnnch_sort.fabric.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.services.INetworkHandler;

public class FabricNetworkHandler implements INetworkHandler {

    @Override
    @Environment(EnvType.CLIENT)
    public void sendToServer(SortRequestPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sendToServer(CyclePreferencePayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sendToServer(ToggleLockPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
