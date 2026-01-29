package xyz.bannach.bnnch_sort;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

/**
 * Configuration management for the Bnnch: Sort mod.
 *
 * <p>This class defines and manages both client-side and server-side configuration options
 * using NeoForge's {@link ModConfigSpec} system. Configuration values are automatically
 * loaded and updated when config files change.</p>
 *
 * <h2>Client Configuration</h2>
 * <ul>
 *   <li>{@link #showSortButton} - Whether to display the sort button on container screens</li>
 * </ul>
 *
 * <h2>Server Configuration</h2>
 * <ul>
 *   <li>{@link #defaultSortMethod} - Default sort method for new players</li>
 *   <li>{@link #defaultSortOrder} - Default sort order for new players</li>
 * </ul>
 *
 * <h2>Side: Common</h2>
 * <p>This class is loaded on both sides, but client config only applies client-side
 * and server config only applies server-side.</p>
 *
 * @since 1.0.0
 * @see BnnchSort
 * @see SortMethod
 * @see SortOrder
 */
@EventBusSubscriber(modid = BnnchSort.MODID)
public class Config {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Config() {}

    /**
     * Builder for client-side configuration options.
     */
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    /**
     * Builder for server-side configuration options.
     */
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    /**
     * Config value for whether to show the sort button on container screens.
     */
    private static final ModConfigSpec.BooleanValue SHOW_SORT_BUTTON = CLIENT_BUILDER
            .comment("Show the sort button on supported container screens")
            .define("showSortButton", true);

    /**
     * Config value for the default sort method applied to new players.
     */
    private static final ModConfigSpec.EnumValue<SortMethod> DEFAULT_SORT_METHOD = SERVER_BUILDER
            .comment("Default sort method for new players")
            .defineEnum("defaultSortMethod", SortMethod.ALPHABETICAL);

    /**
     * Config value for the default sort order applied to new players.
     */
    private static final ModConfigSpec.EnumValue<SortOrder> DEFAULT_SORT_ORDER = SERVER_BUILDER
            .comment("Default sort order for new players")
            .defineEnum("defaultSortOrder", SortOrder.ASCENDING);

    /**
     * The built client configuration specification.
     * Registered in {@link BnnchSort}.
     */
    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    /**
     * The built server configuration specification.
     * Registered in {@link BnnchSort}.
     */
    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    /**
     * Whether to show the sort button on supported container screens.
     * <p>Default: {@code true}</p>
     * <p>Side: Client only</p>
     */
    public static boolean showSortButton = true;

    /**
     * The default sort method assigned to new players.
     * <p>Default: {@link SortMethod#ALPHABETICAL}</p>
     * <p>Side: Server only</p>
     */
    public static SortMethod defaultSortMethod = SortMethod.ALPHABETICAL;

    /**
     * The default sort order assigned to new players.
     * <p>Default: {@link SortOrder#ASCENDING}</p>
     * <p>Side: Server only</p>
     */
    public static SortOrder defaultSortOrder = SortOrder.ASCENDING;

    /**
     * Handles configuration loading events.
     *
     * <p>Updates the static configuration fields when config files are loaded or reloaded.
     * Client and server configs are handled separately based on the spec.</p>
     *
     * @param event the config loading event containing the loaded configuration
     */
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
