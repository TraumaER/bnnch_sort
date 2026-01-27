package xyz.bannach.betterinventorysorter;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class Config {
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHOW_SORT_BUTTON = CLIENT_BUILDER
            .comment("Show the sort button on supported container screens")
            .define("showSortButton", true);

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    public static boolean showSortButton = true;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            showSortButton = SHOW_SORT_BUTTON.get();
        }
    }
}
