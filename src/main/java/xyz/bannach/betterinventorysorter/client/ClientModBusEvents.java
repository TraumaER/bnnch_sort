package xyz.bannach.betterinventorysorter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

@EventBusSubscriber(modid = Betterinventorysorter.MODID, value = Dist.CLIENT)
public class ClientModBusEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        SortKeyHandler.register(event);
    }
}
