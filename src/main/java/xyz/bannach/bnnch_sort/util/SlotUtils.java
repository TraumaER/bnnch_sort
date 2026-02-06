package xyz.bannach.bnnch_sort.util;

import net.minecraft.world.inventory.Slot;

/**
 * Utility methods for working with inventory slots.
 *
 * @since 1.0.0
 */
public class SlotUtils {

  /** Private constructor to prevent instantiation of this utility class. */
  private SlotUtils() {}

  /**
   * Checks if a slot class is a sortable container slot.
   *
   * <p>Sortable slots include:
   *
   * <ul>
   *   <li>{@link Slot} - Base vanilla slot class
   *   <li>{@code SlotItemHandler} - NeoForge ItemHandler capability slot (used by many mods like
   *       MetalBarrels, Sophisticated Storage, etc.)
   * </ul>
   *
   * <p>This excludes special slot subclasses like ResultSlot, FurnaceFuelSlot, ArmorSlot, etc.
   *
   * @param slot the slot to check
   * @return true if the slot is a sortable container slot
   */
  public static boolean isSortableSlotClass(Slot slot) {
    String className = slot.getClass().getName();
    // Allow base Slot class and NeoForge's SlotItemHandler (used by modded containers)
    return slot.getClass() == Slot.class
        || className.equals("net.neoforged.neoforge.items.SlotItemHandler");
  }
}
