package xyz.bannach.betterinventorysorter.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.client.ClientPreferenceCache;
import xyz.bannach.betterinventorysorter.server.PreferenceHandler;
import xyz.bannach.betterinventorysorter.server.SortHandler;

@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class ModPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SortRequestPayload.TYPE,
                SortRequestPayload.STREAM_CODEC,
                SortHandler::handle
        );
        registrar.playToServer(
                CycleMethodPayload.TYPE,
                CycleMethodPayload.STREAM_CODEC,
                PreferenceHandler::handle
        );
        registrar.playToClient(
                SyncPreferencePayload.TYPE,
                SyncPreferencePayload.STREAM_CODEC,
                ClientPreferenceCache::handle
        );
    }
}
