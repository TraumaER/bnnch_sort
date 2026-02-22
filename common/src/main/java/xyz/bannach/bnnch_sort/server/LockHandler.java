package xyz.bannach.bnnch_sort.server;

import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.services.Services;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;

/**
 * Server-side handler for slot lock toggle requests.
 *
 * <p>Processes incoming toggle lock requests from clients, updates the player's locked slots
 * attachment, and synchronizes the full state back to the client.
 *
 * <h2>Side: Server-only</h2>
 *
 * @see ToggleLockPayload
 * @see SyncLockedSlotsPayload
 * @since 1.1.0
 */
public class LockHandler {

  private LockHandler() {}

  /**
   * Handles an incoming toggle lock request from a client.
   *
   * @param payload the toggle lock payload containing the slot index
   * @param player the server player sending the request
   */
  public static void handle(ToggleLockPayload payload, ServerPlayer player) {
    int slotIndex = payload.slotIndex();
    if (slotIndex < 0 || slotIndex > 35) return;
    LockedSlots current = Services.PLAYER_DATA.getLockedSlots(player);
    LockedSlots updated = current.toggle(slotIndex);
    Services.PLAYER_DATA.setLockedSlots(player, updated);
    Services.NETWORK.sendToPlayer(player, new SyncLockedSlotsPayload(updated.slots()));
  }
}
