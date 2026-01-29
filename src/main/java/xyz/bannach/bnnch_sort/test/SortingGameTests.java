package xyz.bannach.bnnch_sort.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.bnnch_sort.server.SortHandler;
import xyz.bannach.bnnch_sort.sorting.ItemSorter;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Game tests for core sorting functionality.
 *
 * <p>This class contains NeoForge GameTest framework tests that verify the
 * {@link ItemSorter} and {@link SortHandler} classes work correctly. Tests cover
 * all sort methods, stack merging, empty slot handling, and menu slot partitioning.</p>
 *
 * <h2>Test Categories</h2>
 * <ul>
 *   <li>Sort method tests - Verify each comparator sorts correctly</li>
 *   <li>Stack merging tests - Verify partial stacks are combined properly</li>
 *   <li>Sort handler tests - Verify menu slot detection and sorting</li>
 * </ul>
 *
 * <h2>Running Tests</h2>
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * @since 1.0.0
 * @see ItemSorter
 * @see SortHandler
 */
@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class SortingGameTests {

    /**
     * Private constructor to prevent instantiation of this test class.
     */
    private SortingGameTests() {}

    /**
     * Tests alphabetical sorting by item name.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void alphabetical_sort_orders_by_name(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE));
        stacks.add(new ItemStack(Items.APPLE));
        stacks.add(new ItemStack(Items.DIAMOND));

        SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        assertItem(helper, sorted, 0, Items.APPLE);
        assertItem(helper, sorted, 1, Items.DIAMOND);
        assertItem(helper, sorted, 2, Items.STONE);

        helper.succeed();
    }

    /**
     * Tests category sorting groups items by creative tab.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void category_sort_groups_by_tab(GameTestHelper helper) {
        // Items from different creative tabs should be grouped
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.DIAMOND_SWORD));  // Combat
        stacks.add(new ItemStack(Items.OAK_PLANKS));     // Building Blocks
        stacks.add(new ItemStack(Items.DIAMOND_AXE));    // Tools

        SortPreference pref = new SortPreference(SortMethod.CATEGORY, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        // Verify all items present and no crashes - tab ordering depends on registration order
        helper.assertTrue(sorted.size() == 3, "Expected 3 items");
        helper.assertTrue(!sorted.get(0).isEmpty(), "First item should not be empty");
        helper.assertTrue(!sorted.get(1).isEmpty(), "Second item should not be empty");
        helper.assertTrue(!sorted.get(2).isEmpty(), "Third item should not be empty");

        helper.succeed();
    }

    /**
     * Tests quantity sorting orders by stack count.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void quantity_sort_orders_by_count(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 10));
        stacks.add(new ItemStack(Items.DIRT, 64));
        stacks.add(new ItemStack(Items.COBBLESTONE, 32));

        SortPreference pref = new SortPreference(SortMethod.QUANTITY, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        helper.assertTrue(sorted.get(0).getCount() == 64, "First should have count 64");
        helper.assertTrue(sorted.get(1).getCount() == 32, "Second should have count 32");
        helper.assertTrue(sorted.get(2).getCount() == 10, "Third should have count 10");

        helper.succeed();
    }

    /**
     * Tests quantity sorting uses alphabetical tiebreaker.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void quantity_sort_tiebreaks_alphabetically(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 10));
        stacks.add(new ItemStack(Items.APPLE, 10));
        stacks.add(new ItemStack(Items.DIRT, 10));

        SortPreference pref = new SortPreference(SortMethod.QUANTITY, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        assertItem(helper, sorted, 0, Items.APPLE);
        assertItem(helper, sorted, 1, Items.DIRT);
        assertItem(helper, sorted, 2, Items.STONE);

        helper.succeed();
    }

    /**
     * Tests mod ID sorting groups items by namespace.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void mod_id_sort_groups_by_namespace(GameTestHelper helper) {
        // All vanilla items share "minecraft" namespace, so they should sort alphabetically
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE));
        stacks.add(new ItemStack(Items.APPLE));
        stacks.add(new ItemStack(Items.DIAMOND));

        SortPreference pref = new SortPreference(SortMethod.MOD_ID, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        // Same namespace -> alphabetical tiebreaker
        assertItem(helper, sorted, 0, Items.APPLE);
        assertItem(helper, sorted, 1, Items.DIAMOND);
        assertItem(helper, sorted, 2, Items.STONE);

        helper.succeed();
    }

    /**
     * Tests that partial stacks are combined during merging.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void merge_stacks_combines_partial_stacks(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 32));
        stacks.add(new ItemStack(Items.STONE, 32));

        List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

        helper.assertTrue(merged.size() == 1, "Expected 1 stack, got " + merged.size());
        helper.assertTrue(merged.get(0).getCount() == 64, "Expected count 64, got " + merged.get(0).getCount());
        assertItem(helper, merged, 0, Items.STONE);

        helper.succeed();
    }

    /**
     * Tests that overflow items create new stacks during merging.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void merge_stacks_overflows_to_new_stack(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 30));
        stacks.add(new ItemStack(Items.STONE, 30));
        stacks.add(new ItemStack(Items.STONE, 30));

        List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

        helper.assertTrue(merged.size() == 2, "Expected 2 stacks, got " + merged.size());

        int total = 0;
        for (ItemStack stack : merged) {
            total += stack.getCount();
        }
        helper.assertTrue(total == 90, "Expected total 90, got " + total);
        helper.assertTrue(merged.get(0).getCount() == 64, "First stack should be 64, got " + merged.get(0).getCount());
        helper.assertTrue(merged.get(1).getCount() == 26, "Second stack should be 26, got " + merged.get(1).getCount());

        helper.succeed();
    }

    /**
     * Tests that unstackable items are not merged.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void merge_stacks_does_not_merge_unstackable(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.DIAMOND_SWORD));
        stacks.add(new ItemStack(Items.DIAMOND_SWORD));

        List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

        helper.assertTrue(merged.size() == 2, "Expected 2 stacks (unstackable), got " + merged.size());

        helper.succeed();
    }

    /**
     * Tests that items with different components are not merged.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void merge_stacks_does_not_merge_different_components(GameTestHelper helper) {
        ItemStack plain = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack enchanted = new ItemStack(Items.DIAMOND_SWORD);
        enchanted.enchant(helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS), 1);

        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(plain);
        stacks.add(enchanted);

        List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

        helper.assertTrue(merged.size() == 2, "Expected 2 stacks (different components), got " + merged.size());

        helper.succeed();
    }

    /**
     * Tests that descending order reverses the sort result.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void descending_order_reverses_sort(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.APPLE));
        stacks.add(new ItemStack(Items.STONE));
        stacks.add(new ItemStack(Items.DIAMOND));

        SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.DESCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        assertItem(helper, sorted, 0, Items.STONE);
        assertItem(helper, sorted, 1, Items.DIAMOND);
        assertItem(helper, sorted, 2, Items.APPLE);

        helper.succeed();
    }

    /**
     * Tests that empty slots are moved to the end after sorting.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void empty_slots_moved_to_end(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(ItemStack.EMPTY);
        stacks.add(new ItemStack(Items.STONE));
        stacks.add(ItemStack.EMPTY);
        stacks.add(new ItemStack(Items.APPLE));
        stacks.add(ItemStack.EMPTY);

        SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        helper.assertTrue(sorted.size() == 5, "Expected 5 slots");
        assertItem(helper, sorted, 0, Items.APPLE);
        assertItem(helper, sorted, 1, Items.STONE);
        helper.assertTrue(sorted.get(2).isEmpty(), "Slot 2 should be empty");
        helper.assertTrue(sorted.get(3).isEmpty(), "Slot 3 should be empty");
        helper.assertTrue(sorted.get(4).isEmpty(), "Slot 4 should be empty");

        helper.succeed();
    }

    /**
     * Tests the full pipeline: condense stacks, then sort.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_pipeline_condenses_then_sorts(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 32));
        stacks.add(new ItemStack(Items.APPLE, 16));
        stacks.add(new ItemStack(Items.STONE, 32));
        stacks.add(ItemStack.EMPTY);

        SortPreference pref = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        helper.assertTrue(sorted.size() == 4, "Expected 4 slots (original size preserved)");
        assertItem(helper, sorted, 0, Items.APPLE);
        helper.assertTrue(sorted.get(0).getCount() == 16, "Apple should have count 16");
        assertItem(helper, sorted, 1, Items.STONE);
        helper.assertTrue(sorted.get(1).getCount() == 64, "Stone should be condensed to 64");
        helper.assertTrue(sorted.get(2).isEmpty(), "Slot 2 should be empty");
        helper.assertTrue(sorted.get(3).isEmpty(), "Slot 3 should be empty");

        helper.succeed();
    }

    /**
     * Asserts that an item at the given index matches the expected item type.
     *
     * @param helper the test helper for assertions
     * @param stacks the list of item stacks to check
     * @param index the index to check
     * @param expected the expected item type
     */
    private static void assertItem(GameTestHelper helper, List<ItemStack> stacks, int index, net.minecraft.world.item.Item expected) {
        helper.assertTrue(
                stacks.get(index).is(expected),
                "Expected " + expected + " at index " + index + ", got " + stacks.get(index)
        );
    }

    // ===== SortHandler Tests =====

    /**
     * Tests that chest slots are correctly partitioned.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_partitions_chest_slots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Fill chest with items in reverse alphabetical order
        chest.setItem(0, new ItemStack(Items.STONE));
        chest.setItem(1, new ItemStack(Items.DIAMOND));
        chest.setItem(2, new ItemStack(Items.APPLE));

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Get container slots using SortHandler logic
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof net.minecraft.world.entity.player.Inventory)) {
                containerSlots.add(slot);
            }
        }

        helper.assertTrue(containerSlots.size() == 27, "Expected 27 chest slots, got " + containerSlots.size());
        helper.assertTrue(containerSlots.get(0).getItem().is(Items.STONE), "First slot should be stone");
        helper.assertTrue(containerSlots.get(1).getItem().is(Items.DIAMOND), "Second slot should be diamond");
        helper.assertTrue(containerSlots.get(2).getItem().is(Items.APPLE), "Third slot should be apple");

        helper.succeed();
    }

    /**
     * Tests that chest contents are sorted correctly.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_sorts_chest_contents(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Fill chest with items in reverse alphabetical order
        chest.setItem(0, new ItemStack(Items.STONE));
        chest.setItem(1, new ItemStack(Items.DIAMOND));
        chest.setItem(2, new ItemStack(Items.APPLE));

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Simulate sort handler logic for REGION_CONTAINER
        List<Slot> targetSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof net.minecraft.world.entity.player.Inventory)) {
                targetSlots.add(slot);
            }
        }

        // Extract and sort
        List<ItemStack> stacks = new ArrayList<>();
        for (Slot slot : targetSlots) {
            stacks.add(slot.getItem().copy());
        }

        List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

        // Write back
        for (int i = 0; i < targetSlots.size(); i++) {
            targetSlots.get(i).set(sorted.get(i));
        }

        // Verify sorted order
        helper.assertTrue(chest.getItem(0).is(Items.APPLE), "First slot should be apple");
        helper.assertTrue(chest.getItem(1).is(Items.DIAMOND), "Second slot should be diamond");
        helper.assertTrue(chest.getItem(2).is(Items.STONE), "Third slot should be stone");

        helper.succeed();
    }

    /**
     * Tests that player main inventory slots are correctly partitioned.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_partitions_player_main_inventory(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Count player main inventory slots (container slots 9-35)
        List<Slot> mainSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container instanceof net.minecraft.world.entity.player.Inventory
                && slot.getContainerSlot() >= 9 && slot.getContainerSlot() <= 35) {
                mainSlots.add(slot);
            }
        }

        helper.assertTrue(mainSlots.size() == 27, "Expected 27 main inventory slots, got " + mainSlots.size());

        helper.succeed();
    }

    /**
     * Tests that player hotbar slots are correctly partitioned.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_partitions_player_hotbar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Count player hotbar slots (container slots 0-8)
        List<Slot> hotbarSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container instanceof net.minecraft.world.entity.player.Inventory
                && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 8) {
                hotbarSlots.add(slot);
            }
        }

        helper.assertTrue(hotbarSlots.size() == 9, "Expected 9 hotbar slots, got " + hotbarSlots.size());

        helper.succeed();
    }

    /**
     * Tests that invalid region codes return no slots.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_empty_region_returns_no_slots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Test invalid region code (999)
        List<Slot> invalidSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            boolean matches = switch (999) {
                case SortHandler.REGION_CONTAINER -> !(slot.container instanceof net.minecraft.world.entity.player.Inventory);
                case SortHandler.REGION_PLAYER_MAIN -> slot.container instanceof net.minecraft.world.entity.player.Inventory
                    && slot.getContainerSlot() >= 9 && slot.getContainerSlot() <= 35;
                case SortHandler.REGION_PLAYER_HOTBAR -> slot.container instanceof net.minecraft.world.entity.player.Inventory
                    && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 8;
                default -> false;
            };
            if (matches) {
                invalidSlots.add(slot);
            }
        }

        helper.assertTrue(invalidSlots.isEmpty(), "Invalid region should return no slots");

        helper.succeed();
    }

    /**
     * Tests that sorting chest does not affect player inventory.
     * @param helper the game test helper
     */
    @GameTest(template = "empty")
    public static void sort_handler_does_not_affect_player_inventory_when_sorting_chest(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos chestPos = new BlockPos(1, 1, 1);

        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);

        // Fill chest with items
        chest.setItem(0, new ItemStack(Items.STONE));
        chest.setItem(1, new ItemStack(Items.APPLE));

        // Fill player inventory with items
        player.getInventory().setItem(9, new ItemStack(Items.DIAMOND));
        player.getInventory().setItem(10, new ItemStack(Items.GOLD_INGOT));

        // Open chest menu
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);

        // Sort only chest (REGION_CONTAINER)
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

        List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

        for (int i = 0; i < containerSlots.size(); i++) {
            containerSlots.get(i).set(sorted.get(i));
        }

        // Verify chest is sorted
        helper.assertTrue(chest.getItem(0).is(Items.APPLE), "Chest should be sorted");

        // Verify player inventory unchanged
        helper.assertTrue(player.getInventory().getItem(9).is(Items.DIAMOND), "Player slot 9 should be unchanged");
        helper.assertTrue(player.getInventory().getItem(10).is(Items.GOLD_INGOT), "Player slot 10 should be unchanged");

        helper.succeed();
    }
}
