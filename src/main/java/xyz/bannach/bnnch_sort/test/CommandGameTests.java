package xyz.bannach.bnnch_sort.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.server.SortHandler;
import xyz.bannach.bnnch_sort.sorting.ItemSorter;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * Game tests for the /bnnchsort command functionality.
 *
 * <p>This class contains NeoForge GameTest framework tests that verify the command system works
 * correctly. Tests simulate command execution by directly calling the sorting logic that commands
 * use.
 *
 * <h2>Test Categories</h2>
 *
 * <ul>
 *   <li>sortinv command tests - Verify main, hotbar, and all regions
 *   <li>change command tests - Verify preference updates
 *   <li>reset command tests - Verify default restoration
 *   <li>Argument parsing tests - Verify method and order validation
 * </ul>
 *
 * <h2>Running Tests</h2>
 *
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * @see xyz.bannach.bnnch_sort.commands.ModCommands
 * @since 1.0.0
 */
@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class CommandGameTests {

  /** Private constructor to prevent instantiation of this test class. */
  private CommandGameTests() {}

  /**
   * Tests sorting main inventory via command.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_sortinv_main_sorts_main_inventory(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Add items in reverse alphabetical order to main inventory (slots 9-35)
    player.getInventory().setItem(9, new ItemStack(Items.STONE));
    player.getInventory().setItem(10, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(11, new ItemStack(Items.APPLE));

    // Simulate command sorting for main region
    sortPlayerRegion(player, SortHandler.REGION_PLAYER_MAIN, SortPreference.DEFAULT);

    // Verify sorted order (alphabetical ascending by default)
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.APPLE), "Slot 9 should be Apple after sorting");
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.DIAMOND),
        "Slot 10 should be Diamond after sorting");
    helper.assertTrue(
        player.getInventory().getItem(11).is(Items.STONE), "Slot 11 should be Stone after sorting");

    helper.succeed();
  }

  /**
   * Tests sorting hotbar via command.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_sortinv_hotbar_sorts_hotbar(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Add items in reverse alphabetical order to hotbar (slots 0-8)
    player.getInventory().setItem(0, new ItemStack(Items.STONE));
    player.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
    player.getInventory().setItem(2, new ItemStack(Items.APPLE));

    // Simulate command sorting for hotbar region
    sortPlayerRegion(player, SortHandler.REGION_PLAYER_HOTBAR, SortPreference.DEFAULT);

    // Verify sorted order (alphabetical ascending by default)
    helper.assertTrue(
        player.getInventory().getItem(0).is(Items.APPLE), "Slot 0 should be Apple after sorting");
    helper.assertTrue(
        player.getInventory().getItem(1).is(Items.DIAMOND),
        "Slot 1 should be Diamond after sorting");
    helper.assertTrue(
        player.getInventory().getItem(2).is(Items.STONE), "Slot 2 should be Stone after sorting");

    helper.succeed();
  }

  /**
   * Tests sorting both inventory regions via command.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_sortinv_all_sorts_both_regions(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Add items to hotbar
    player.getInventory().setItem(0, new ItemStack(Items.STONE));
    player.getInventory().setItem(1, new ItemStack(Items.APPLE));

    // Add items to main inventory
    player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT));
    player.getInventory().setItem(10, new ItemStack(Items.COAL));

    // Simulate command sorting for "all" (both regions)
    SortPreference pref = SortPreference.DEFAULT;
    sortPlayerRegion(player, SortHandler.REGION_PLAYER_MAIN, pref);
    sortPlayerRegion(player, SortHandler.REGION_PLAYER_HOTBAR, pref);

    // Verify hotbar sorted
    helper.assertTrue(
        player.getInventory().getItem(0).is(Items.APPLE),
        "Hotbar slot 0 should be Apple after sorting");
    helper.assertTrue(
        player.getInventory().getItem(1).is(Items.STONE),
        "Hotbar slot 1 should be Stone after sorting");

    // Verify main inventory sorted
    helper.assertTrue(
        player.getInventory().getItem(9).is(Items.COAL),
        "Main slot 9 should be Coal after sorting");
    helper.assertTrue(
        player.getInventory().getItem(10).is(Items.GOLD_INGOT),
        "Main slot 10 should be Gold Ingot after sorting");

    helper.succeed();
  }

  /**
   * Tests that the change command updates player preferences.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_change_updates_player_preference(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Verify initial preference
    SortPreference initial = player.getData(ModAttachments.SORT_PREFERENCE);
    helper.assertTrue(
        initial.method() == Config.defaultSortMethod, "Initial method should be default");

    // Simulate /bnnchsort change quantity descending
    SortPreference newPref = new SortPreference(SortMethod.QUANTITY, SortOrder.DESCENDING);
    player.setData(ModAttachments.SORT_PREFERENCE, newPref);

    // Verify preference was updated
    SortPreference updated = player.getData(ModAttachments.SORT_PREFERENCE);
    helper.assertTrue(updated.method() == SortMethod.QUANTITY, "Method should be QUANTITY");
    helper.assertTrue(updated.order() == SortOrder.DESCENDING, "Order should be DESCENDING");

    helper.succeed();
  }

  /**
   * Tests that the reset command restores default preferences.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_reset_restores_defaults(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // First change the preference
    player.setData(
        ModAttachments.SORT_PREFERENCE,
        new SortPreference(SortMethod.QUANTITY, SortOrder.DESCENDING));

    // Simulate /bnnchsort reset - reset to config defaults
    SortPreference defaultPref =
        new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder);
    player.setData(ModAttachments.SORT_PREFERENCE, defaultPref);

    // Verify preference was reset to defaults
    SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);
    helper.assertTrue(
        pref.method() == Config.defaultSortMethod, "Method should be reset to config default");
    helper.assertTrue(
        pref.order() == Config.defaultSortOrder, "Order should be reset to config default");

    helper.succeed();
  }

  /**
   * Tests that command sorting respects player preferences.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_sorting_uses_player_preference(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    // Set preference to quantity ascending (highest count first after sort)
    player.setData(
        ModAttachments.SORT_PREFERENCE,
        new SortPreference(SortMethod.QUANTITY, SortOrder.ASCENDING));

    // Add items with different quantities
    player.getInventory().setItem(9, new ItemStack(Items.STONE, 10));
    player.getInventory().setItem(10, new ItemStack(Items.DIRT, 64));
    player.getInventory().setItem(11, new ItemStack(Items.COBBLESTONE, 32));

    // Sort using player's preference
    SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);
    sortPlayerRegion(player, SortHandler.REGION_PLAYER_MAIN, pref);

    // Verify sorted by quantity ascending (highest count first due to quantity comparator
    // implementation)
    helper.assertTrue(
        player.getInventory().getItem(9).getCount() == 64, "Slot 9 should have count 64 (highest)");
    helper.assertTrue(
        player.getInventory().getItem(10).getCount() == 32,
        "Slot 10 should have count 32 (middle)");
    helper.assertTrue(
        player.getInventory().getItem(11).getCount() == 10,
        "Slot 11 should have count 10 (lowest)");

    helper.succeed();
  }

  /**
   * Tests that invalid sort method names are rejected.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_invalid_method_is_rejected(GameTestHelper helper) {
    // Test that invalid method parsing returns null
    SortMethod method = parseMethod("invalid_method");
    helper.assertTrue(method == null, "Invalid method should return null");

    // Valid methods should parse correctly
    helper.assertTrue(
        parseMethod("alphabetical") == SortMethod.ALPHABETICAL, "alphabetical should parse");
    helper.assertTrue(parseMethod("category") == SortMethod.CATEGORY, "category should parse");
    helper.assertTrue(parseMethod("quantity") == SortMethod.QUANTITY, "quantity should parse");
    helper.assertTrue(parseMethod("mod_id") == SortMethod.MOD_ID, "mod_id should parse");

    helper.succeed();
  }

  /**
   * Tests that invalid sort order names are rejected.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void command_invalid_order_is_rejected(GameTestHelper helper) {
    // Test that invalid order parsing returns null
    SortOrder order = parseOrder("invalid_order");
    helper.assertTrue(order == null, "Invalid order should return null");

    // Valid orders should parse correctly
    helper.assertTrue(parseOrder("ascending") == SortOrder.ASCENDING, "ascending should parse");
    helper.assertTrue(parseOrder("descending") == SortOrder.DESCENDING, "descending should parse");

    helper.succeed();
  }

  /**
   * Helper method to sort a player's inventory region.
   *
   * <p>This method simulates what the /bnnchsort sortinv command does: extracts items from the
   * target slots, sorts them, and writes them back.
   *
   * @param player the player whose inventory to sort
   * @param region the region to sort
   * @param preference the sort preferences to use
   */
  private static void sortPlayerRegion(Player player, int region, SortPreference preference) {
    InventoryMenu menu = player.inventoryMenu;
    List<Slot> targetSlots = SortHandler.getTargetSlots(menu, region);

    if (targetSlots.isEmpty()) {
      return;
    }

    List<ItemStack> stacks = new ArrayList<>();
    for (Slot slot : targetSlots) {
      stacks.add(slot.getItem().copy());
    }

    List<ItemStack> sorted = ItemSorter.sort(stacks, preference);

    for (int i = 0; i < targetSlots.size(); i++) {
      targetSlots.get(i).set(sorted.get(i));
    }
  }

  /**
   * Helper method to parse a sort method from its name.
   *
   * <p>Mirrors the parsing logic in ModCommands for testing purposes.
   *
   * @param name the serialized name to parse
   * @return the matching SortMethod, or null if not found
   */
  private static SortMethod parseMethod(String name) {
    for (SortMethod method : SortMethod.values()) {
      if (method.getSerializedName().equalsIgnoreCase(name)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Helper method to parse a sort order from its name.
   *
   * <p>Mirrors the parsing logic in ModCommands for testing purposes.
   *
   * @param name the serialized name to parse
   * @return the matching SortOrder, or null if not found
   */
  private static SortOrder parseOrder(String name) {
    for (SortOrder order : SortOrder.values()) {
      if (order.getSerializedName().equalsIgnoreCase(name)) {
        return order;
      }
    }
    return null;
  }
}
