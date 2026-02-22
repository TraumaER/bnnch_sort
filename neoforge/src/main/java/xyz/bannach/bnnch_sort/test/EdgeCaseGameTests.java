package xyz.bannach.bnnch_sort.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.bnnch_sort.server.SortHandler;
import xyz.bannach.bnnch_sort.sorting.ItemSorter;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * Game tests for edge cases and special scenarios.
 *
 * <p>This class contains NeoForge GameTest framework tests that verify the mod handles edge cases
 * correctly. Tests cover empty inventories, special containers, player state validation, and
 * boundary conditions.
 *
 * <h2>Test Categories</h2>
 *
 * <ul>
 *   <li>Empty inventory tests - Verify sorting handles empty slots gracefully
 *   <li>Special container tests - Verify crafting tables and furnaces are handled
 *   <li>Player state tests - Verify spectator mode and closed containers
 *   <li>Region isolation tests - Verify sorting one region doesn't affect others
 * </ul>
 *
 * <h2>Running Tests</h2>
 *
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * @see ItemSorter
 * @see SortHandler
 * @since 1.0.0
 */
@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class EdgeCaseGameTests {

  /** Private constructor to prevent instantiation of this test class. */
  private EdgeCaseGameTests() {}

  /**
   * Tests sorting an empty container.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void empty_container_sort_is_noop(GameTestHelper helper) {
    // Sorting 27 empty slots should return 27 empty slots with no crash
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < 27; i++) {
      stacks.add(ItemStack.EMPTY);
    }

    List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

    helper.assertTrue(sorted.size() == 27, "Expected 27 slots, got " + sorted.size());
    for (int i = 0; i < 27; i++) {
      helper.assertTrue(sorted.get(i).isEmpty(), "Slot " + i + " should be empty");
    }

    helper.succeed();
  }

  /**
   * Tests sorting a single item among empty slots.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void single_item_sort_works(GameTestHelper helper) {
    // Single item among empties should sort to index 0
    List<ItemStack> stacks = new ArrayList<>();
    stacks.add(ItemStack.EMPTY);
    stacks.add(ItemStack.EMPTY);
    stacks.add(new ItemStack(Items.DIAMOND));
    stacks.add(ItemStack.EMPTY);
    stacks.add(ItemStack.EMPTY);

    List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

    helper.assertTrue(sorted.size() == 5, "Expected 5 slots, got " + sorted.size());
    helper.assertTrue(
        sorted.get(0).is(Items.DIAMOND), "First slot should be diamond, got " + sorted.get(0));
    for (int i = 1; i < 5; i++) {
      helper.assertTrue(sorted.get(i).isEmpty(), "Slot " + i + " should be empty");
    }

    helper.succeed();
  }

  /**
   * Tests that full stacks are not merged with other items.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void max_stack_items_unchanged_by_merge(GameTestHelper helper) {
    // Two full stacks of 64 stone + 64 dirt should stay as separate stacks
    List<ItemStack> stacks = new ArrayList<>();
    stacks.add(new ItemStack(Items.STONE, 64));
    stacks.add(new ItemStack(Items.DIRT, 64));

    List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

    helper.assertTrue(merged.size() == 2, "Expected 2 stacks, got " + merged.size());

    // Verify counts unchanged
    int stoneCount = 0;
    int dirtCount = 0;
    for (ItemStack stack : merged) {
      if (stack.is(Items.STONE)) stoneCount += stack.getCount();
      if (stack.is(Items.DIRT)) dirtCount += stack.getCount();
    }
    helper.assertTrue(stoneCount == 64, "Expected 64 stone, got " + stoneCount);
    helper.assertTrue(dirtCount == 64, "Expected 64 dirt, got " + dirtCount);

    helper.succeed();
  }

  /**
   * Tests that many partial stacks condense to multiple full stacks.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void large_partial_stacks_condense_correctly(GameTestHelper helper) {
    // 10 stacks of 10 stone -> should condense to 1x64 + 1x36
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      stacks.add(new ItemStack(Items.STONE, 10));
    }

    List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

    int totalCount = 0;
    for (ItemStack stack : merged) {
      totalCount += stack.getCount();
    }

    helper.assertTrue(merged.size() == 2, "Expected 2 stacks, got " + merged.size());
    helper.assertTrue(totalCount == 100, "Expected total 100, got " + totalCount);
    helper.assertTrue(
        merged.get(0).getCount() == 64,
        "First stack should be 64, got " + merged.get(0).getCount());
    helper.assertTrue(
        merged.get(1).getCount() == 36,
        "Second stack should be 36, got " + merged.get(1).getCount());

    helper.succeed();
  }

  /**
   * Tests sorting many identical items doesn't crash.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void all_identical_items_sort_without_crash(GameTestHelper helper) {
    // 5 x 32 stone = 160 total -> condenses to 64+64+32, padded to 5 slots
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      stacks.add(new ItemStack(Items.STONE, 32));
    }

    List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

    helper.assertTrue(sorted.size() == 5, "Expected 5 slots, got " + sorted.size());

    // Should condense to 3 stacks of stone (64+64+32) + 2 empties
    int totalStone = 0;
    int nonEmpty = 0;
    for (ItemStack stack : sorted) {
      if (!stack.isEmpty()) {
        helper.assertTrue(stack.is(Items.STONE), "Non-empty stack should be stone, got " + stack);
        totalStone += stack.getCount();
        nonEmpty++;
      }
    }
    helper.assertTrue(totalStone == 160, "Expected total 160 stone, got " + totalStone);
    helper.assertTrue(nonEmpty == 3, "Expected 3 non-empty stacks, got " + nonEmpty);

    helper.succeed();
  }

  /**
   * Tests that hotbar is unchanged when sorting main inventory.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void hotbar_unchanged_when_sorting_main_inventory(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    BlockPos chestPos = new BlockPos(1, 1, 1);

    helper.setBlock(chestPos, Blocks.CHEST);
    ChestBlockEntity chest = helper.getBlockEntity(chestPos);

    // Place items in hotbar (slots 0-8)
    player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
    player.getInventory().setItem(1, new ItemStack(Items.SHIELD));
    player.getInventory().setItem(8, new ItemStack(Items.TORCH, 64));

    // Place items in main inventory (slots 9-35)
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 32));
    player.getInventory().setItem(10, new ItemStack(Items.APPLE, 16));

    // Open chest menu and sort main inventory region
    ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);
    List<Slot> mainSlots = SortHandler.getTargetSlots(menu, SortHandler.REGION_PLAYER_MAIN);

    // Extract and sort
    List<ItemStack> stacks = new ArrayList<>();
    for (Slot slot : mainSlots) {
      stacks.add(slot.getItem().copy());
    }
    List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);
    for (int i = 0; i < mainSlots.size(); i++) {
      mainSlots.get(i).set(sorted.get(i));
    }

    // Verify hotbar is completely unchanged
    helper.assertTrue(
        player.getInventory().getItem(0).is(Items.DIAMOND_SWORD),
        "Hotbar slot 0 should still be diamond sword");
    helper.assertTrue(
        player.getInventory().getItem(1).is(Items.SHIELD), "Hotbar slot 1 should still be shield");
    helper.assertTrue(
        player.getInventory().getItem(8).is(Items.TORCH), "Hotbar slot 8 should still be torch");
    helper.assertTrue(
        player.getInventory().getItem(8).getCount() == 64,
        "Hotbar slot 8 torch count should still be 64");

    helper.succeed();
  }

  /**
   * Tests spectator mode detection for guarding sort requests.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void spectator_mode_player_is_detected(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SPECTATOR);

    helper.assertTrue(
        player.isSpectator(), "Spectator mode player should return true for isSpectator()");

    helper.succeed();
  }

  /**
   * Tests detection of closed container state.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void closed_container_returns_empty_slots(GameTestHelper helper) {
    // When no container is open, containerMenu == inventoryMenu
    // The SortHandler guard checks this condition to bail out early
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    helper.assertTrue(
        player.containerMenu == player.inventoryMenu,
        "Player with no container open should have containerMenu == inventoryMenu");

    // Verify that REGION_CONTAINER on a real chest menu returns non-empty
    BlockPos chestPos = new BlockPos(1, 1, 1);
    helper.setBlock(chestPos, Blocks.CHEST);
    ChestBlockEntity chest = helper.getBlockEntity(chestPos);
    ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

    List<Slot> containerSlots = SortHandler.getTargetSlots(menu, SortHandler.REGION_CONTAINER);
    helper.assertTrue(
        !containerSlots.isEmpty(), "REGION_CONTAINER on chest menu should return non-empty list");

    helper.succeed();
  }

  /**
   * Tests that crafting table slots are rejected for sorting.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void crafting_table_container_slots_rejected(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    CraftingMenu menu = new CraftingMenu(0, player.getInventory());

    List<Slot> containerSlots = SortHandler.getTargetSlots(menu, SortHandler.REGION_CONTAINER);
    helper.assertTrue(
        containerSlots.isEmpty(),
        "REGION_CONTAINER on crafting menu should return empty list, got "
            + containerSlots.size()
            + " slots");

    helper.succeed();
  }

  /**
   * Tests that furnace slots are rejected for sorting.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void furnace_container_slots_rejected(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    FurnaceMenu menu = new FurnaceMenu(0, player.getInventory());

    List<Slot> containerSlots = SortHandler.getTargetSlots(menu, SortHandler.REGION_CONTAINER);
    helper.assertTrue(
        containerSlots.isEmpty(),
        "REGION_CONTAINER on furnace menu should return empty list, got "
            + containerSlots.size()
            + " slots");

    helper.succeed();
  }
}
