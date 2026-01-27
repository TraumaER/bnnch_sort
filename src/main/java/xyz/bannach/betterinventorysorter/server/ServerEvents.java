package xyz.bannach.betterinventorysorter.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.ModAttachments;
import xyz.bannach.betterinventorysorter.network.SyncPreferencePayload;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);
            PacketDistributor.sendToPlayer(player,
                    new SyncPreferencePayload(pref.method(), pref.order()));
        }
    }
}
