package xyz.bannach.bnnch_sort.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.fabric.server.FabricServerEvents;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.server.LockHandler;
import xyz.bannach.bnnch_sort.server.PreferenceHandler;
import xyz.bannach.bnnch_sort.server.SortHandler;

public class BnnchSortFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register payload types (both directions)
        PayloadTypeRegistry.playC2S().register(
            SortRequestPayload.TYPE, SortRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(
            CyclePreferencePayload.TYPE, CyclePreferencePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(
            ToggleLockPayload.TYPE, ToggleLockPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            SyncPreferencePayload.TYPE, SyncPreferencePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            SyncLockedSlotsPayload.TYPE, SyncLockedSlotsPayload.STREAM_CODEC);

        // Register server-side packet handlers
        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> SortHandler.handle(payload, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CyclePreferencePayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> PreferenceHandler.handle(payload, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ToggleLockPayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> LockHandler.handle(payload, context.player())));

        FabricServerEvents.register();
    }
}
