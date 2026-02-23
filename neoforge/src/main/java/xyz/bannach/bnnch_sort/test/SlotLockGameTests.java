package xyz.bannach.bnnch_sort.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.server.SortHandler;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;

/**
 * Game tests for the slot locking feature.
 *
 * <p>This class contains NeoForge GameTest framework tests that verify slot locking works
 * correctly. Tests cover the LockedSlots record, lock-aware sorting, pre-sort merging into locked
 * slots, the unlock command, and attachment persistence.
 *
 * <h2>Test Categories</h2>
 *
 * <ul>
 *   <li>LockedSlots record tests - Toggle, isLocked, countInRange, EMPTY
 *   <li>Lock-aware sorting tests - Locked slots stay in place during sorts
 *   <li>Pre-sort merge tests - Non-full locked stacks receive matching items
 *   <li>Attachment tests - Persistence, defaults, unlock command
 *   <li>Edge case tests - All locked, all unlocked, empty locked slots
 * </ul>
 *
 * <h2>Running Tests</h2>
 *
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * @see LockedSlots
 * @see SortHandler
 * @since 1.1.0
 */
@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class SlotLockGameTests {

  private SlotLockGameTests() {}

  // ===== LockedSlots Record Tests =====

  /**
   * Tests that EMPTY locked slots has no slots locked.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_empty_has_no_locked(GameTestHelper helper) {
    LockedSlots empty = LockedSlots.EMPTY;

    helper.assertTrue(empty.slots().isEmpty(), "EMPTY should have no slots");
    helper.assertTrue(!empty.isLocked(0), "Slot 0 should not be locked in EMPTY");
    helper.assertTrue(!empty.isLocked(9), "Slot 9 should not be locked in EMPTY");
    helper.assertTrue(!empty.isLocked(35), "Slot 35 should not be locked in EMPTY");

    helper.succeed();
  }

  /**
   * Tests that toggling a slot locks it, and toggling again unlocks it.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_toggle_locks_and_unlocks(GameTestHelper helper) {
    LockedSlots initial = LockedSlots.EMPTY;

    // Toggle slot 5 on
    LockedSlots locked = initial.toggle(5);
    helper.assertTrue(locked.isLocked(5), "Slot 5 should be locked after toggle on");
    helper.assertTrue(!locked.isLocked(0), "Slot 0 should still be unlocked");

    // Toggle slot 5 off
    LockedSlots unlocked = locked.toggle(5);
    helper.assertTrue(!unlocked.isLocked(5), "Slot 5 should be unlocked after toggle off");

    helper.succeed();
  }

  /**
   * Tests that toggling returns a new instance (immutability).
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_toggle_is_immutable(GameTestHelper helper) {
    LockedSlots initial = LockedSlots.EMPTY;
    LockedSlots toggled = initial.toggle(5);

    helper.assertTrue(!initial.isLocked(5), "Original should be unchanged after toggle");
    helper.assertTrue(toggled.isLocked(5), "New instance should have slot 5 locked");

    helper.succeed();
  }

  /**
   * Tests countInRange correctly counts locked slots within a range.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_count_in_range_works(GameTestHelper helper) {
    LockedSlots locked =
        LockedSlots.EMPTY.toggle(0).toggle(3).toggle(8).toggle(9).toggle(15).toggle(35);

    // Hotbar range (0-8): slots 0, 3, 8 = 3
    int hotbarCount = locked.countInRange(0, 8);
    helper.assertTrue(hotbarCount == 3, "Expected 3 locked in hotbar, got " + hotbarCount);

    // Main range (9-35): slots 9, 15, 35 = 3
    int mainCount = locked.countInRange(9, 35);
    helper.assertTrue(mainCount == 3, "Expected 3 locked in main, got " + mainCount);

    // Empty range
    int emptyCount = LockedSlots.EMPTY.countInRange(0, 35);
    helper.assertTrue(emptyCount == 0, "Expected 0 locked in EMPTY, got " + emptyCount);

    helper.succeed();
  }

  // ===== Attachment Tests =====

  /**
   * Tests that the default locked slots attachment is empty.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_attachment_defaults_to_empty(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    LockedSlots locked = player.getData(ModAttachments.LOCKED_SLOTS);

    helper.assertTrue(
        locked.slots().isEmpty(),
        "Default locked slots should be empty, got " + locked.slots().size() + " slots");

    helper.succeed();
  }

  /**
   * Tests that locked slots persist when set on a player.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_slots_attachment_persists_on_player(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    LockedSlots locked = LockedSlots.EMPTY.toggle(5).toggle(10).toggle(35);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    LockedSlots read = player.getData(ModAttachments.LOCKED_SLOTS);
    helper.assertTrue(read.isLocked(5), "Slot 5 should be locked after read-back");
    helper.assertTrue(read.isLocked(10), "Slot 10 should be locked after read-back");
    helper.assertTrue(read.isLocked(35), "Slot 35 should be locked after read-back");
    helper.assertTrue(!read.isLocked(0), "Slot 0 should not be locked after read-back");

    helper.succeed();
  }

  // ===== Lock-Aware Sorting Tests =====

  /**
   * Tests that locked slots are skipped during sorting and their items remain in place.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void sort_skips_locked_slots(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Place items in main inventory (slots 9-35)
    player.getInventory().setItem(9, new ItemStack(Items.STONE));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));

    // Lock slot 9 (Stone) - it should stay in place
    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    // Sort main inventory with lock awareness
    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Slot 9 should still be Stone (locked)
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.STONE),
        "Locked slot 9 should still be Stone, got " + player.getInventory().getItem(9));

    // Remaining unlocked slots should be sorted (Apple before Diamond)
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.APPLE),
        "Unlocked slot 10 should be Apple (sorted), got " + player.getInventory().getItem(10));
    helper.assertTrue(
        player.getInventory().getItem(11).is(Items.DIAMOND),
        "Unlocked slot 11 should be Diamond (sorted), got " + player.getInventory().getItem(11));

    helper.succeed();
  }

  /**
   * Tests that an empty locked slot stays empty after sorting.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_empty_slot_stays_empty(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Place items in main inventory
    player.getInventory().setItem(10, new ItemStack(Items.STONE));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));
    // Slot 9 is empty

    // Lock slot 9 (empty)
    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Slot 9 should still be empty (locked)
    helper.assertTrue(
        player.getInventory().getItem(9).isEmpty(), "Locked empty slot 9 should remain empty");

    // Unlocked slots should be sorted
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.APPLE),
        "Unlocked slot 10 should be Apple (sorted)");
    helper.assertTrue(
        player.getInventory().getItem(11).is(Items.STONE),
        "Unlocked slot 11 should be Stone (sorted)");

    helper.succeed();
  }

  /**
   * Tests that locked hotbar slots are skipped when sorting hotbar.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_hotbar_slot_stays_in_place(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Place items in hotbar (slots 0-8)
    player.getInventory().setItem(0, new ItemStack(Items.STONE));
    player.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(2, new ItemStack(Items.APPLE));

    // Lock slot 0 (Stone)
    LockedSlots locked = LockedSlots.EMPTY.toggle(0);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_HOTBAR);

    // Slot 0 should still be Stone (locked)
    helper.assertTrue(
        player.getInventory().getItem(0).is(Items.STONE),
        "Locked hotbar slot 0 should still be Stone");

    // Remaining unlocked slots should be sorted
    helper.assertTrue(
        player.getInventory().getItem(1).is(Items.APPLE),
        "Unlocked hotbar slot 1 should be Apple (sorted)");
    helper.assertTrue(
        player.getInventory().getItem(2).is(Items.DIAMOND),
        "Unlocked hotbar slot 2 should be Diamond (sorted)");

    helper.succeed();
  }

  // ===== Pre-Sort Merge Tests =====

  /**
   * Tests that a non-full locked stack receives matching items from unlocked slots.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_partial_stack_merges_from_unlocked(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Locked slot has 32 stone, unlocked slot has 16 stone
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 32));
    player.getInventory().setItem(10, new ItemStack(Items.STONE, 16));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE, 5));

    // Lock slot 9
    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Locked slot 9 should have 48 stone (32 + 16 merged)
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.STONE), "Locked slot 9 should still be Stone");
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 48,
        "Locked slot 9 should have 48 stone, got " + player.getInventory().getItem(9).getCount());

    helper.succeed();
  }

  /**
   * Tests that a full locked stack does not receive additional items.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_full_stack_does_not_merge(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Locked slot has 64 stone (full), unlocked slot also has stone
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 64));
    player.getInventory().setItem(10, new ItemStack(Items.STONE, 16));

    // Lock slot 9
    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Locked slot 9 should still have 64 stone (no merge needed)
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 64,
        "Locked full slot should stay at 64, got " + player.getInventory().getItem(9).getCount());

    // Unlocked stone should still exist
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.STONE), "Unlocked stone should be in slot 10");
    helper.assertTrue(
        player.getInventory().getItem(10).getCount() == 16,
        "Unlocked stone should have count 16, got " + player.getInventory().getItem(10).getCount());

    helper.succeed();
  }

  /**
   * Tests that unstackable locked items (like swords) don't merge.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_unstackable_item_does_not_merge(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    player.getInventory().setItem(9, new ItemStack(Items.DIAMOND_SWORD));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND_SWORD));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));

    // Lock slot 9 (sword)
    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Locked sword should still be count 1
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.DIAMOND_SWORD),
        "Locked slot 9 should still be Diamond Sword");
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 1, "Locked sword should still have count 1");

    helper.succeed();
  }

  /**
   * Tests that pre-sort merge only transfers up to max stack size.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_partial_stack_caps_at_max(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Locked slot has 50 stone, unlocked has 32 stone
    // Should merge 14 into locked (to 64), leaving 18 unlocked
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 50));
    player.getInventory().setItem(10, new ItemStack(Items.STONE, 32));

    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 64,
        "Locked slot should cap at 64, got " + player.getInventory().getItem(9).getCount());

    // Remaining unlocked stone should have 18
    int remainingStone = 0;
    for (int i = 10; i <= 35; i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (stack.is(Items.STONE)) {
        remainingStone += stack.getCount();
      }
    }
    helper.assertTrue(
        remainingStone == 18, "Remaining unlocked stone should be 18, got " + remainingStone);

    helper.succeed();
  }

  // ===== Edge Cases =====

  /**
   * Tests that sorting with no locked slots behaves exactly like normal sorting.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void sort_with_no_locks_behaves_normally(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    player.getInventory().setItem(9, new ItemStack(Items.STONE));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));

    // No locks set (default EMPTY)
    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    helper.assertTrue(player.getInventory().getItem(9).is(Items.APPLE), "Slot 9 should be Apple");
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.DIAMOND), "Slot 10 should be Diamond");
    helper.assertTrue(player.getInventory().getItem(11).is(Items.STONE), "Slot 11 should be Stone");

    helper.succeed();
  }

  /**
   * Tests that sorting when all slots are locked is a no-op (items don't move).
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void sort_with_all_slots_locked_is_noop(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    player.getInventory().setItem(9, new ItemStack(Items.STONE));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));

    // Lock all three slots
    LockedSlots locked = LockedSlots.EMPTY.toggle(9).toggle(10).toggle(11);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // All items should remain exactly where they were
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.STONE),
        "Slot 9 should still be Stone (all locked)");
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.DIAMOND),
        "Slot 10 should still be Diamond (all locked)");
    helper.assertTrue(
        player.getInventory().getItem(11).is(Items.APPLE),
        "Slot 11 should still be Apple (all locked)");

    helper.succeed();
  }

  /**
   * Tests that locking main inventory slots does not affect hotbar sorting.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void main_locks_do_not_affect_hotbar_sort(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Lock main inventory slot
    LockedSlots locked = LockedSlots.EMPTY.toggle(9).toggle(10);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    // Place items in hotbar
    player.getInventory().setItem(0, new ItemStack(Items.STONE));
    player.getInventory().setItem(1, new ItemStack(Items.APPLE));

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_HOTBAR);

    // Hotbar should sort normally (no hotbar slots are locked)
    helper.assertTrue(
        player.getInventory().getItem(0).is(Items.APPLE), "Hotbar slot 0 should be Apple (sorted)");
    helper.assertTrue(
        player.getInventory().getItem(1).is(Items.STONE), "Hotbar slot 1 should be Stone (sorted)");

    helper.succeed();
  }

  /**
   * Tests the unlock command simulation: clearing all locks.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void unlock_all_clears_locked_slots(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Lock several slots
    LockedSlots locked = LockedSlots.EMPTY.toggle(0).toggle(5).toggle(9).toggle(20).toggle(35);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    // Verify they're locked
    LockedSlots before = player.getData(ModAttachments.LOCKED_SLOTS);
    helper.assertTrue(before.slots().size() == 5, "Expected 5 locked slots before unlock");

    // Simulate /bnnchsort unlock
    player.setData(ModAttachments.LOCKED_SLOTS, LockedSlots.EMPTY);

    LockedSlots after = player.getData(ModAttachments.LOCKED_SLOTS);
    helper.assertTrue(after.slots().isEmpty(), "All slots should be unlocked after unlock command");

    helper.succeed();
  }

  /**
   * Tests that multiple locked slots across both regions work correctly when sorting main.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void multiple_locked_slots_sort_correctly(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Place items: slots 9-13
    player.getInventory().setItem(9, new ItemStack(Items.STONE));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));
    player.getInventory().setItem(12, new ItemStack(Items.GOLD_INGOT));
    player.getInventory().setItem(13, new ItemStack(Items.COAL));

    // Lock slots 9 (Stone) and 12 (Gold Ingot)
    LockedSlots locked = LockedSlots.EMPTY.toggle(9).toggle(12);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Locked slots should be unchanged
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.STONE), "Locked slot 9 should still be Stone");
    helper.assertTrue(
        player.getInventory().getItem(12).is(Items.GOLD_INGOT),
        "Locked slot 12 should still be Gold Ingot");

    // Unlocked slots (10, 11, 13) should be sorted: Apple, Coal, Diamond
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.APPLE),
        "Unlocked slot 10 should be Apple, got " + player.getInventory().getItem(10));
    helper.assertTrue(
        player.getInventory().getItem(11).is(Items.COAL),
        "Unlocked slot 11 should be Coal, got " + player.getInventory().getItem(11));
    helper.assertTrue(
        player.getInventory().getItem(13).is(Items.DIAMOND),
        "Unlocked slot 13 should be Diamond, got " + player.getInventory().getItem(13));

    helper.succeed();
  }

  /**
   * Tests pre-sort merge with mismatched items does not merge.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void locked_partial_stack_does_not_merge_different_item(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Locked slot has 32 stone, unlocked has dirt (different item)
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 32));
    player.getInventory().setItem(10, new ItemStack(Items.DIRT, 16));

    LockedSlots locked = LockedSlots.EMPTY.toggle(9);
    player.setData(ModAttachments.LOCKED_SLOTS, locked);

    SortHandler.sortRegion(player, player.inventoryMenu, SortHandler.REGION_PLAYER_MAIN);

    // Stone should not have gained any dirt
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 32,
        "Locked stone should remain at 32 (no merge with dirt), got "
            + player.getInventory().getItem(9).getCount());

    helper.succeed();
  }
}
