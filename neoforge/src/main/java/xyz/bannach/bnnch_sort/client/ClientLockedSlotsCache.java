package xyz.bannach.bnnch_sort.client;

import java.util.HashSet;
import java.util.Set;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;

/**
 * Client-side cache for the player's locked slots.
 *
 * <p>Maintains a local copy of the locked slot set on the client for rendering overlays and
 * tooltips. Updated when the server sends a {@link SyncLockedSlotsPayload}.
 *
 * <h2>Side: Client-only</h2>
 *
 * @see SyncLockedSlotsPayload
 * @since 1.1.0
 */
public class ClientLockedSlotsCache {

  private ClientLockedSlotsCache() {}

  private static volatile Set<Integer> lockedSlots = Set.of();

  /**
   * Handles incoming locked slots sync payloads from the server.
   *
   * @param payload the sync payload containing the locked slot set
   * @param context the network context
   */
  public static void handle(SyncLockedSlotsPayload payload, IPayloadContext context) {
    context.enqueueWork(() -> lockedSlots = Set.copyOf(payload.lockedSlots()));
  }

  /**
   * Optimistically toggles a slot locally for immediate visual feedback.
   *
   * @param slotIndex the slot index to toggle
   */
  public static void toggleLocal(int slotIndex) {
    Set<Integer> newSlots = new HashSet<>(lockedSlots);
    if (newSlots.contains(slotIndex)) {
      newSlots.remove(slotIndex);
    } else {
      newSlots.add(slotIndex);
    }
    lockedSlots = Set.copyOf(newSlots);
  }

  /**
   * Returns whether the given slot index is locked.
   *
   * @param slotIndex the slot index to check
   * @return true if the slot is locked
   */
  public static boolean isLocked(int slotIndex) {
    return lockedSlots.contains(slotIndex);
  }

  /**
   * Returns the current set of locked slots.
   *
   * @return unmodifiable set of locked slot indices
   */
  public static Set<Integer> getLockedSlots() {
    return lockedSlots;
  }

  /**
   * Counts the number of locked slots in the given range (inclusive).
   *
   * @param from the start of the range (inclusive)
   * @param to the end of the range (inclusive)
   * @return the count of locked slots in the range
   */
  public static int countInRange(int from, int to) {
    int count = 0;
    for (int slot : lockedSlots) {
      if (slot >= from && slot <= to) {
        count++;
      }
    }
    return count;
  }
}
