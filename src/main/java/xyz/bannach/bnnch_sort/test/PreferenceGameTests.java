package xyz.bannach.bnnch_sort.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.sorting.ItemSorter;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * Game tests for the player preference system.
 *
 * <p>This class contains NeoForge GameTest framework tests that verify the {@link SortPreference}
 * and {@link ModAttachments#SORT_PREFERENCE} functionality. Tests cover default values,
 * persistence, preference cycling, and integration with sorting.
 *
 * <h2>Test Categories</h2>
 *
 * <ul>
 *   <li>Attachment tests - Verify preferences are stored and retrieved correctly
 *   <li>Cycling tests - Verify method and order cycling behavior
 *   <li>Integration tests - Verify sorting uses player preferences
 * </ul>
 *
 * <h2>Running Tests</h2>
 *
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * @see SortPreference
 * @see ModAttachments
 * @since 1.0.0
 */
@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class PreferenceGameTests {

  /** Private constructor to prevent instantiation of this test class. */
  private PreferenceGameTests() {}

  /**
   * Tests that default preference attachment is set correctly.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void preference_attachment_defaults_correctly(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);

    helper.assertTrue(
        pref.equals(SortPreference.DEFAULT),
        "Default preference should be ALPHABETICAL ASCENDING, got "
            + pref.method().getSerializedName()
            + " "
            + pref.order().getSerializedName());

    helper.succeed();
  }

  /**
   * Tests that preferences persist when set on a player.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void preference_attachment_persists_on_player(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);

    SortPreference custom = new SortPreference(SortMethod.QUANTITY, SortOrder.DESCENDING);
    player.setData(ModAttachments.SORT_PREFERENCE, custom);

    SortPreference read = player.getData(ModAttachments.SORT_PREFERENCE);
    helper.assertTrue(
        read.method() == SortMethod.QUANTITY,
        "Expected QUANTITY, got " + read.method().getSerializedName());
    helper.assertTrue(
        read.order() == SortOrder.DESCENDING,
        "Expected DESCENDING, got " + read.order().getSerializedName());

    helper.succeed();
  }

  /**
   * Tests that sorting uses the player's preference settings.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void sort_uses_player_preference(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    BlockPos chestPos = new BlockPos(1, 1, 1);

    helper.setBlock(chestPos, Blocks.CHEST);
    ChestBlockEntity chest = helper.getBlockEntity(chestPos);

    // Items with different quantities
    chest.setItem(0, new ItemStack(Items.APPLE, 10));
    chest.setItem(1, new ItemStack(Items.STONE, 64));
    chest.setItem(2, new ItemStack(Items.DIAMOND, 32));

    // Set player preference to QUANTITY ASCENDING (highest first by default comparator)
    SortPreference pref = new SortPreference(SortMethod.QUANTITY, SortOrder.ASCENDING);
    player.setData(ModAttachments.SORT_PREFERENCE, pref);

    // Open chest and sort
    ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

    List<Slot> containerSlots = new ArrayList<>();
    for (Slot slot : menu.slots) {
      if (!(slot.container instanceof net.minecraft.world.entity.player.Inventory)) {
        containerSlots.add(slot);
      }
    }

    List<ItemStack> stacks = new ArrayList<>();
    for (Slot slot : containerSlots) {
      stacks.add(slot.getItem().copy());
    }

    List<ItemStack> sorted =
        ItemSorter.sort(stacks, player.getData(ModAttachments.SORT_PREFERENCE));

    for (int i = 0; i < containerSlots.size(); i++) {
      containerSlots.get(i).set(sorted.get(i));
    }

    // QuantityComparator sorts highest first in ascending order
    helper.assertTrue(
        chest.getItem(0).is(Items.STONE),
        "First slot should be stone (64), got " + chest.getItem(0));
    helper.assertTrue(
        chest.getItem(1).is(Items.DIAMOND),
        "Second slot should be diamond (32), got " + chest.getItem(1));
    helper.assertTrue(
        chest.getItem(2).is(Items.APPLE),
        "Third slot should be apple (10), got " + chest.getItem(2));

    helper.succeed();
  }

  /**
   * Tests that cycling method advances through all options.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void cycle_method_advances_to_next(GameTestHelper helper) {
    // ALPHABETICAL -> CATEGORY -> QUANTITY -> MOD_ID -> ALPHABETICAL
    SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);

    pref = pref.withNextMethod();
    helper.assertTrue(
        pref.method() == SortMethod.CATEGORY,
        "Expected CATEGORY after ALPHABETICAL, got " + pref.method().getSerializedName());

    pref = pref.withNextMethod();
    helper.assertTrue(
        pref.method() == SortMethod.QUANTITY,
        "Expected QUANTITY after CATEGORY, got " + pref.method().getSerializedName());

    pref = pref.withNextMethod();
    helper.assertTrue(
        pref.method() == SortMethod.MOD_ID,
        "Expected MOD_ID after QUANTITY, got " + pref.method().getSerializedName());

    pref = pref.withNextMethod();
    helper.assertTrue(
        pref.method() == SortMethod.ALPHABETICAL,
        "Expected ALPHABETICAL after MOD_ID, got " + pref.method().getSerializedName());

    helper.succeed();
  }

  /**
   * Tests that toggling order flips between ascending and descending.
   *
   * @param helper the game test helper
   */
  @GameTest(template = "empty")
  public static void toggle_order_flips_direction(GameTestHelper helper) {
    SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);

    pref = pref.withToggledOrder();
    helper.assertTrue(
        pref.order() == SortOrder.DESCENDING,
        "Expected DESCENDING after toggling from ASCENDING, got "
            + pref.order().getSerializedName());

    pref = pref.withToggledOrder();
    helper.assertTrue(
        pref.order() == SortOrder.ASCENDING,
        "Expected ASCENDING after toggling from DESCENDING, got "
            + pref.order().getSerializedName());

    helper.succeed();
  }
}
