package xyz.bannach.bnnch_sort.sorting;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable record holding the set of locked inventory slot indices for a player.
 *
 * <p>Locked slots are skipped during sorting but may receive items merged from unlocked slots when
 * the locked slot contains a non-full, stackable item.
 *
 * <h2>Persistence</h2>
 *
 * <p>Locked slots are persisted using the {@link #CODEC} for NBT serialization and attached to
 * players via {@link xyz.bannach.bnnch_sort.ModAttachments#LOCKED_SLOTS}.
 *
 * <h2>Side: Common</h2>
 *
 * <p>Used on both client and server.
 *
 * @param slots the set of locked slot indices (player inventory 0-35)
 * @since 1.1.0
 */
public record LockedSlots(Set<Integer> slots) {

  /** Empty locked slots instance (no slots locked). */
  public static final LockedSlots EMPTY = new LockedSlots(Set.of());

  /**
   * Codec for serializing and deserializing LockedSlots to/from NBT.
   *
   * <p>Stores the locked slot indices as an integer list.
   */
  public static final Codec<LockedSlots> CODEC =
      Codec.INT
          .listOf()
          .xmap(list -> new LockedSlots(new HashSet<>(list)), ls -> List.copyOf(ls.slots()));

  /**
   * Returns whether the given slot index is locked.
   *
   * @param slotIndex the inventory slot index to check
   * @return true if the slot is locked
   */
  public boolean isLocked(int slotIndex) {
    return slots.contains(slotIndex);
  }

  /**
   * Returns a new instance with the given slot toggled.
   *
   * <p>If the slot is currently locked it will be unlocked, and vice versa.
   *
   * @param slotIndex the inventory slot index to toggle
   * @return a new LockedSlots with the slot toggled
   */
  public LockedSlots toggle(int slotIndex) {
    Set<Integer> newSlots = new HashSet<>(slots);
    if (newSlots.contains(slotIndex)) {
      newSlots.remove(slotIndex);
    } else {
      newSlots.add(slotIndex);
    }
    return new LockedSlots(newSlots);
  }

  /**
   * Counts the number of locked slots in the given range (inclusive).
   *
   * @param from the start of the range (inclusive)
   * @param to the end of the range (inclusive)
   * @return the count of locked slots in the range
   */
  public int countInRange(int from, int to) {
    int count = 0;
    for (int slot : slots) {
      if (slot >= from && slot <= to) {
        count++;
      }
    }
    return count;
  }
}
