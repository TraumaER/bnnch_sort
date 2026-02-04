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
 * <p>This class defines and manages both client-side and server-side configuration options using
 * NeoForge's {@link ModConfigSpec} system. Configuration values are automatically loaded and
 * updated when config files change.
 *
 * <h2>Client Configuration</h2>
 *
 * <ul>
 *   <li>{@link #showSortButton} - Whether to display the sort button on container screens
 * </ul>
 *
 * <h2>Server Configuration</h2>
 *
 * <ul>
 *   <li>{@link #defaultSortMethod} - Default sort method for new players
 *   <li>{@link #defaultSortOrder} - Default sort order for new players
 * </ul>
 *
 * <h2>Side: Common</h2>
 *
 * <p>This class is loaded on both sides, but client config only applies client-side and server
 * config only applies server-side.
 *
 * @see BnnchSort
 * @see SortMethod
 * @see SortOrder
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID)
public class Config {

  /** Private constructor to prevent instantiation of this utility class. */
  private Config() {}

  /** Builder for client-side configuration options. */
  private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

  /** Builder for server-side configuration options. */
  private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

  /** Config value for whether to show the sort button on container screens. */
  private static final ModConfigSpec.BooleanValue SHOW_SORT_BUTTON =
      CLIENT_BUILDER
          .comment("Show the sort button on supported container screens")
          .define("showSortButton", true);

  /** Config value for the modifier key used to lock/unlock slots. */
  private static final ModConfigSpec.EnumValue<ModifierKey> LOCK_MODIFIER_KEY =
      CLIENT_BUILDER
          .comment("Modifier key to hold while clicking a slot to toggle its lock state")
          .defineEnum("lockModifierKey", ModifierKey.ALT);

  /** Config value for the tint color applied to locked slots (hex ARGB). */
  private static final ModConfigSpec.ConfigValue<String> LOCK_TINT_COLOR =
      CLIENT_BUILDER
          .comment("Tint color for locked slots in hex ARGB format (e.g. 800000FF for semi-transparent blue)")
          .define(
              "lockTintColor",
              "800000FF",
              s -> {
                try {
                  Long.parseLong((String) s, 16);
                  return true;
                } catch (Exception e) {
                  return false;
                }
              });

  /** Config value for whether to show a tooltip on locked slots. */
  private static final ModConfigSpec.BooleanValue SHOW_LOCK_TOOLTIP =
      CLIENT_BUILDER
          .comment("Show a tooltip when hovering over an empty locked slot")
          .define("showLockTooltip", true);

  /** Config value for the default sort method applied to new players. */
  private static final ModConfigSpec.EnumValue<SortMethod> DEFAULT_SORT_METHOD =
      SERVER_BUILDER
          .comment("Default sort method for new players")
          .defineEnum("defaultSortMethod", SortMethod.ALPHABETICAL);

  /** Config value for the default sort order applied to new players. */
  private static final ModConfigSpec.EnumValue<SortOrder> DEFAULT_SORT_ORDER =
      SERVER_BUILDER
          .comment("Default sort order for new players")
          .defineEnum("defaultSortOrder", SortOrder.ASCENDING);

  /** The built client configuration specification. Registered in {@link BnnchSort}. */
  static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

  /** The built server configuration specification. Registered in {@link BnnchSort}. */
  static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

  /**
   * Whether to show the sort button on supported container screens.
   *
   * <p>Default: {@code true}
   *
   * <p>Side: Client only
   */
  public static boolean showSortButton = true;

  /**
   * The modifier key used to lock/unlock inventory slots.
   *
   * <p>Default: {@link ModifierKey#ALT}
   *
   * <p>Side: Client only
   */
  public static ModifierKey lockModifierKey = ModifierKey.ALT;

  /**
   * The tint color applied to locked slots as an ARGB integer.
   *
   * <p>Default: {@code 0x800000FF} (semi-transparent blue)
   *
   * <p>Side: Client only
   */
  public static int lockTintColor = 0x800000FF;

  /**
   * Whether to show a tooltip when hovering over an empty locked slot.
   *
   * <p>Default: {@code true}
   *
   * <p>Side: Client only
   */
  public static boolean showLockTooltip = true;

  /**
   * The default sort method assigned to new players.
   *
   * <p>Default: {@link SortMethod#ALPHABETICAL}
   *
   * <p>Side: Server only
   */
  public static SortMethod defaultSortMethod = SortMethod.ALPHABETICAL;

  /**
   * The default sort order assigned to new players.
   *
   * <p>Default: {@link SortOrder#ASCENDING}
   *
   * <p>Side: Server only
   */
  public static SortOrder defaultSortOrder = SortOrder.ASCENDING;

  /**
   * Handles configuration loading events.
   *
   * <p>Updates the static configuration fields when config files are loaded. Client and server
   * configs are handled separately based on the spec.
   *
   * @param event the config loading event containing the loaded configuration
   */
  @SubscribeEvent
  public static void onLoad(final ModConfigEvent.Loading event) {
    applyConfig(event.getConfig().getSpec());
  }

  /**
   * Handles configuration reloading events.
   *
   * <p>Updates the static configuration fields when config files are changed at runtime.
   *
   * @param event the config reloading event containing the reloaded configuration
   */
  @SubscribeEvent
  public static void onReload(final ModConfigEvent.Reloading event) {
    applyConfig(event.getConfig().getSpec());
  }

  /**
   * Applies configuration values from the given spec to the static fields.
   *
   * @param spec the config spec that was loaded or reloaded
   */
  private static void applyConfig(Object spec) {
    if (spec == CLIENT_SPEC) {
      showSortButton = SHOW_SORT_BUTTON.get();
      lockModifierKey = LOCK_MODIFIER_KEY.get();
      lockTintColor = parseColor(LOCK_TINT_COLOR.get(), 0x800000FF);
      showLockTooltip = SHOW_LOCK_TOOLTIP.get();
    } else if (spec == SERVER_SPEC) {
      defaultSortMethod = DEFAULT_SORT_METHOD.get();
      defaultSortOrder = DEFAULT_SORT_ORDER.get();
    }
  }

  /**
   * Parses a hex ARGB color string into an integer.
   *
   * @param hex the hex string (e.g. "800000FF")
   * @param fallback the fallback value if parsing fails
   * @return the parsed ARGB color as an integer
   */
  private static int parseColor(String hex, int fallback) {
    try {
      return (int) Long.parseLong(hex, 16);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }
}
