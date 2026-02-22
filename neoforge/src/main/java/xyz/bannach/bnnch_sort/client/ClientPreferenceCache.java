package xyz.bannach.bnnch_sort.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

/**
 * Client-side cache for the player's sort preferences.
 *
 * <p>This class maintains a local copy of the player's sort preferences on the client for use in UI
 * elements such as tooltips and feedback messages. The cache is updated when the server sends a
 * {@link SyncPreferencePayload}.
 *
 * <h2>Usage</h2>
 *
 * <p>Access the current preferences via {@link #getMethod()} and {@link #getOrder()}. These values
 * are updated automatically when sync packets arrive.
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>This cache only exists on the client. The authoritative preferences are stored server-side in
 * player data attachments.
 *
 * @see SyncPreferencePayload
 * @see SortFeedback
 * @since 1.0.0
 */
public class ClientPreferenceCache {

  /** Private constructor to prevent instantiation of this utility class. */
  private ClientPreferenceCache() {}

  /**
   * The cached sort method.
   *
   * <p>Default: {@link SortMethod#ALPHABETICAL}
   */
  private static SortMethod method = SortMethod.ALPHABETICAL;

  /**
   * The cached sort order.
   *
   * <p>Default: {@link SortOrder#ASCENDING}
   */
  private static SortOrder order = SortOrder.ASCENDING;

  /**
   * Whether the cache has been initialized with server data.
   *
   * <p>Used to suppress feedback on initial sync during login.
   */
  private static boolean initialized = false;

  /**
   * Handles incoming preference sync payloads from the server.
   *
   * <p>This method is registered as the packet handler for {@link SyncPreferencePayload} and
   * enqueues the cache update on the client thread.
   *
   * @param payload the sync payload containing the new preferences
   * @param context the network context
   */
  public static void handle(SyncPreferencePayload payload, IPayloadContext context) {
    context.enqueueWork(() -> update(payload));
  }

  /**
   * Updates the cached preferences from a sync payload.
   *
   * <p>If this is the first sync (during login), no feedback is shown. For subsequent updates,
   * feedback is displayed if the preferences changed.
   *
   * @param payload the sync payload containing the new preferences
   */
  private static void update(SyncPreferencePayload payload) {
    SortMethod oldMethod = method;
    SortOrder oldOrder = order;

    method = payload.method();
    order = payload.order();

    if (!initialized) {
      initialized = true;
      return;
    }

    // Show feedback for preference changes from server
    if (oldMethod != method || oldOrder != order) {
      SortFeedback.showPreferenceChange(method, order);
    }
  }

  /**
   * Returns the cached sort method.
   *
   * @return the player's current sort method
   */
  public static SortMethod getMethod() {
    return method;
  }

  /**
   * Returns the cached sort order.
   *
   * @return the player's current sort order
   */
  public static SortOrder getOrder() {
    return order;
  }
}
