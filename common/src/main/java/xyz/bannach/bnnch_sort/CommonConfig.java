package xyz.bannach.bnnch_sort;

import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

/**
 * Platform-agnostic configuration values for the Bnnch: Sort mod.
 *
 * <p>This class holds the runtime configuration values that are populated by the platform-specific
 * config loader (e.g., NeoForge's {@code NeoForgeConfig.java} or Fabric's JSON config loader). Client-side
 * code in common should read values from this class rather than from the platform config directly.
 *
 * <h2>Side: Common</h2>
 *
 * <p>Readable on both client and server, but client-only values are only meaningful on the client.
 *
 * @since 1.0.0
 */
public final class CommonConfig {

  /** Private constructor to prevent instantiation of this utility class. */
  private CommonConfig() {}

  /** The mod identifier. */
  public static final String MODID = "bnnch_sort";

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
   * <p>Default: {@code 0x80FFD700} (semi-transparent gold)
   *
   * <p>Side: Client only
   */
  public static int lockTintColor = 0x80FFD700;

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
}
