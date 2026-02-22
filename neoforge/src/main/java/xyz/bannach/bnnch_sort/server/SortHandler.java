package xyz.bannach.bnnch_sort.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.sorting.ItemSorter;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;
import xyz.bannach.bnnch_sort.util.SlotUtils;

/**
 * Server-side handler for sort request payloads.
 *
 * <p>This class processes incoming sort requests from clients, determines which inventory slots to
 * sort based on the requested region, and performs the sorting operation using the player's stored
 * preferences.
 *
 * <h2>Inventory Regions</h2>
 *
 * <ul>
 *   <li>{@link #REGION_CONTAINER} (0) - Container inventory (chest, shulker box, etc.)
 *   <li>{@link #REGION_PLAYER_MAIN} (1) - Player's main inventory (slots 9-35)
 *   <li>{@link #REGION_PLAYER_HOTBAR} (2) - Player's hotbar (slots 0-8)
 * </ul>
 *
 * <h2>Slot Filtering</h2>
 *
 * <p>The handler intelligently filters out non-sortable slots:
 *
 * <ul>
 *   <li>Special slot subclasses (result slots, fuel slots, armor slots)
 *   <li>Crafting grid slots
 *   <li>Containers that contain any special slots (e.g., furnace)
 * </ul>
 *
 * <h2>Side: Server-only</h2>
 *
 * <p>All sorting operations execute on the server to prevent cheating.
 *
 * @see SortRequestPayload
 * @see ItemSorter
 * @since 1.0.0
 */
public class SortHandler {

  /** Private constructor to prevent instantiation of this utility class. */
  private SortHandler() {}

  /**
   * Region code for the container's inventory (chest, shulker box, etc.).
   *
   * <p>Value: {@value}
   */
  public static final int REGION_CONTAINER = 0;

  /**
   * Region code for the player's main inventory (slots 9-35, excluding hotbar).
   *
   * <p>Value: {@value}
   */
  public static final int REGION_PLAYER_MAIN = 1;

  /**
   * Region code for the player's hotbar (slots 0-8).
   *
   * <p>Value: {@value}
   */
  public static final int REGION_PLAYER_HOTBAR = 2;

  /**
   * Handles an incoming sort request from a client.
   *
   * <p>This method validates the request, determines target slots, extracts items, sorts them using
   * the player's preferences, and writes the sorted items back to the slots. The operation is
   * enqueued on the main server thread.
   *
   * <p><strong>Guard Conditions:</strong>
   *
   * <ul>
   *   <li>Spectator mode players are ignored
   *   <li>Container region requests are ignored when no container is open
   *   <li>Empty target slot lists result in no-op
   * </ul>
   *
   * @param payload the sort request containing the target region
   * @param context the network context containing the sending player
   */
  public static void handle(SortRequestPayload payload, IPayloadContext context) {
    context.enqueueWork(
        () -> {
          ServerPlayer player = (ServerPlayer) context.player();
          if (player.isSpectator()) return;

          AbstractContainerMenu menu = player.containerMenu;
          if (payload.region() == REGION_CONTAINER && menu == player.inventoryMenu) return;

          int region = payload.region();
          if (region == REGION_PLAYER_MAIN || region == REGION_PLAYER_HOTBAR) {
            sortRegion(player, menu, region);
          } else {
            sortContainerRegion(menu, player);
          }
        });
  }

  /**
   * Sorts a container region (chest, shulker, etc.) without lock awareness.
   *
   * @param menu the container menu
   * @param player the player performing the sort
   */
  private static void sortContainerRegion(AbstractContainerMenu menu, ServerPlayer player) {
    List<Slot> targetSlots = getTargetSlots(menu, REGION_CONTAINER);
    if (targetSlots.isEmpty()) {
      return;
    }

    List<ItemStack> stacks = new ArrayList<>();
    for (Slot slot : targetSlots) {
      stacks.add(slot.getItem().copy());
    }

    List<ItemStack> sorted =
        ItemSorter.sort(stacks, player.getData(ModAttachments.SORT_PREFERENCE));

    for (int i = 0; i < targetSlots.size(); i++) {
      targetSlots.get(i).set(sorted.get(i));
    }

    menu.broadcastChanges();
  }

  /**
   * Sorts a player inventory region with lock awareness.
   *
   * <p>Locked slots are excluded from sorting. Non-full stackable items in locked slots may receive
   * matching items merged from unlocked slots during the sort.
   *
   * @param player the player whose inventory is being sorted
   * @param menu the container menu
   * @param region the region code ({@link #REGION_PLAYER_MAIN} or {@link #REGION_PLAYER_HOTBAR})
   */
  public static void sortRegion(Player player, AbstractContainerMenu menu, int region) {
    List<Slot> targetSlots = getTargetSlots(menu, region);
    if (targetSlots.isEmpty()) {
      return;
    }

    LockedSlots lockedSlots = player.getData(ModAttachments.LOCKED_SLOTS);
    SortPreference preference = player.getData(ModAttachments.SORT_PREFERENCE);

    // Partition into locked and unlocked
    List<Slot> locked = new ArrayList<>();
    List<Slot> unlocked = new ArrayList<>();
    for (Slot slot : targetSlots) {
      if (lockedSlots.isLocked(slot.getContainerSlot())) {
        locked.add(slot);
      } else {
        unlocked.add(slot);
      }
    }

    // Extract unlocked items
    List<ItemStack> unlockedItems = new ArrayList<>();
    for (Slot slot : unlocked) {
      unlockedItems.add(slot.getItem().copy());
    }

    // Pre-sort merge: fill locked non-full stacks from unlocked items
    for (Slot lockedSlot : locked) {
      ItemStack lockedItem = lockedSlot.getItem();
      if (lockedItem.isEmpty() || lockedItem.getCount() >= lockedItem.getMaxStackSize()) {
        continue;
      }
      if (lockedItem.getMaxStackSize() <= 1) {
        continue;
      }

      for (int i = 0; i < unlockedItems.size(); i++) {
        ItemStack unlockedItem = unlockedItems.get(i);
        if (unlockedItem.isEmpty()) {
          continue;
        }
        if (ItemStack.isSameItemSameComponents(lockedItem, unlockedItem)) {
          int space = lockedItem.getMaxStackSize() - lockedItem.getCount();
          if (space > 0) {
            int transfer = Math.min(space, unlockedItem.getCount());
            lockedItem.grow(transfer);
            unlockedItem.shrink(transfer);
            if (unlockedItem.isEmpty()) {
              unlockedItems.set(i, ItemStack.EMPTY);
            }
          }
        }
        if (lockedItem.getCount() >= lockedItem.getMaxStackSize()) {
          break;
        }
      }

      lockedSlot.set(lockedItem);
    }

    // Sort unlocked items
    List<ItemStack> sorted = ItemSorter.sort(unlockedItems, preference);

    // Write sorted items back to unlocked slots
    for (int i = 0; i < unlocked.size(); i++) {
      unlocked.get(i).set(sorted.get(i));
    }

    menu.broadcastChanges();
  }

  /**
   * Determines which slots in a menu belong to the specified region.
   *
   * <p>This method filters menu slots based on the region code and excludes non-sortable slots such
   * as:
   *
   * <ul>
   *   <li>Special slot subclasses (ResultSlot, FurnaceFuelSlot, etc.)
   *   <li>Crafting container slots
   *   <li>Any container that contains special slots (to avoid sorting furnace inputs)
   *   <li>Armor and offhand slots (outside slots 0-35)
   * </ul>
   *
   * @param menu the container menu to analyze
   * @param region the region code ({@link #REGION_CONTAINER}, {@link #REGION_PLAYER_MAIN}, or
   *     {@link #REGION_PLAYER_HOTBAR})
   * @return a list of sortable slots for the specified region, may be empty
   */
  public static List<Slot> getTargetSlots(AbstractContainerMenu menu, int region) {
    // For REGION_CONTAINER, build a set of containers that have any special slot subclass.
    // A base Slot sharing a container with a subclass (e.g. furnace ingredient slot) is not
    // sortable.
    Set<Container> specialContainers = Set.of();
    if (region == REGION_CONTAINER) {
      specialContainers = new HashSet<>();
      for (Slot slot : menu.slots) {
        if (!(slot.container instanceof Inventory) && !SlotUtils.isSortableSlotClass(slot)) {
          specialContainers.add(slot.container);
        }
      }
    }

    List<Slot> slots = new ArrayList<>();

    for (Slot slot : menu.slots) {
      boolean matches =
          switch (region) {
            case REGION_CONTAINER ->
                !(slot.container instanceof Inventory)
                    && SlotUtils.isSortableSlotClass(slot)
                    && !(slot.container instanceof CraftingContainer)
                    && !specialContainers.contains(slot.container);
            case REGION_PLAYER_MAIN ->
                slot.container instanceof Inventory
                    && slot.getContainerSlot() >= 9
                    && slot.getContainerSlot() <= 35;
            case REGION_PLAYER_HOTBAR ->
                slot.container instanceof Inventory
                    && slot.getContainerSlot() >= 0
                    && slot.getContainerSlot() <= 8;
            default -> false;
          };

      if (matches) {
        slots.add(slot);
      }
    }

    return slots;
  }
}
