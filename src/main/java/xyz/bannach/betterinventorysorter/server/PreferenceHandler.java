package xyz.bannach.betterinventorysorter.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.betterinventorysorter.ModAttachments;
import xyz.bannach.betterinventorysorter.network.CyclePreferencePayload;
import xyz.bannach.betterinventorysorter.network.SyncPreferencePayload;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

public class PreferenceHandler {

    public static void handle(CyclePreferencePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            SortPreference current = player.getData(ModAttachments.SORT_PREFERENCE);

            SortPreference updated = current.next();

            player.setData(ModAttachments.SORT_PREFERENCE, updated);

            PacketDistributor.sendToPlayer(player,
                    new SyncPreferencePayload(updated.method(), updated.order()));
        });
    }
}
