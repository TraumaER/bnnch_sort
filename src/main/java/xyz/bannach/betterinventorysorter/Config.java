package xyz.bannach.betterinventorysorter;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

@EventBusSubscriber(modid = Betterinventorysorter.MODID)
public class Config {
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHOW_SORT_BUTTON = CLIENT_BUILDER
            .comment("Show the sort button on supported container screens")
            .define("showSortButton", true);

    private static final ModConfigSpec.EnumValue<SortMethod> DEFAULT_SORT_METHOD = SERVER_BUILDER
            .comment("Default sort method for new players")
            .defineEnum("defaultSortMethod", SortMethod.ALPHABETICAL);
    private static final ModConfigSpec.EnumValue<SortOrder> DEFAULT_SORT_ORDER = SERVER_BUILDER
            .comment("Default sort order for new players")
            .defineEnum("defaultSortOrder", SortOrder.ASCENDING);

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    public static boolean showSortButton = true;
    public static SortMethod defaultSortMethod = SortMethod.ALPHABETICAL;
    public static SortOrder defaultSortOrder = SortOrder.ASCENDING;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            showSortButton = SHOW_SORT_BUTTON.get();
        } else if (event.getConfig().getSpec() == SERVER_SPEC) {
            defaultSortMethod = DEFAULT_SORT_METHOD.get();
            defaultSortOrder = DEFAULT_SORT_ORDER.get();
        }
    }
}
