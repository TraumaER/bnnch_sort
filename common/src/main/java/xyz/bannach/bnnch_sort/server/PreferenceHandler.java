package xyz.bannach.bnnch_sort.server;

import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.services.Services;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * Server-side handler for preference cycling requests.
 *
 * <p>This class processes incoming cycle preference requests from clients, advances the player's
 * sort preferences to the next combination, persists the change, and synchronizes the new
 * preferences back to the client.
 *
 * <h2>Cycling Pattern</h2>
 *
 * <p>Preferences cycle through all combinations of method and order:
 *
 * <pre>
 * Alphabetical(Asc) → Alphabetical(Desc) → Category(Asc) → Category(Desc) → ...
 * </pre>
 *
 * <h2>Side: Server-only</h2>
 *
 * <p>Preferences are stored server-side and synced to clients.
 *
 * @see CyclePreferencePayload
 * @see SyncPreferencePayload
 * @see SortPreference#next()
 * @since 1.0.0
 */
public class PreferenceHandler {

  /** Private constructor to prevent instantiation of this utility class. */
  private PreferenceHandler() {}

  /**
   * Handles an incoming cycle preference request from a client.
   *
   * <p>This method retrieves the player's current preferences, advances to the next combination,
   * saves the new preferences, and sends a sync packet to update the client's cache.
   *
   * @param payload the cycle preference payload (contains no data)
   * @param player the server player sending the request
   */
  public static void handle(CyclePreferencePayload payload, ServerPlayer player) {
    SortPreference current = Services.PLAYER_DATA.getPreference(player);
    SortPreference updated = current.next();
    Services.PLAYER_DATA.setPreference(player, updated);
    Services.NETWORK.sendToPlayer(player, new SyncPreferencePayload(updated.method(), updated.order()));
  }
}
