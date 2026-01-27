package xyz.bannach.betterinventorysorter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

@EventBusSubscriber(modid = Betterinventorysorter.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        SortKeyHandler.onKeyPressed(event);
    }

    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        SortKeyHandler.onMouseClicked(event);
    }
}