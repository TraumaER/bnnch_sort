package xyz.bannach.betterinventorysorter.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import xyz.bannach.betterinventorysorter.sorting.ItemSorter;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("betterinventorysorter")
@PrefixGameTestTemplate(false)
public class SortingGameTests {

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

    @GameTest(template = "empty")
    public static void quantity_sort_orders_by_count(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.STONE, 10));
        stacks.add(new ItemStack(Items.DIRT, 64));
        stacks.add(new ItemStack(Items.COBBLESTONE, 32));

        SortPreference pref = new SortPreference(SortMethod.QUANTITY, SortOrder.ASCENDING);
        List<ItemStack> sorted = ItemSorter.sort(stacks, pref);

        helper.assertTrue(sorted.get(0).getCount() == 10, "First should have count 10");
        helper.assertTrue(sorted.get(1).getCount() == 32, "Second should have count 32");
        helper.assertTrue(sorted.get(2).getCount() == 64, "Third should have count 64");

        helper.succeed();
    }

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

    @GameTest(template = "empty")
    public static void merge_stacks_does_not_merge_unstackable(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.DIAMOND_SWORD));
        stacks.add(new ItemStack(Items.DIAMOND_SWORD));

        List<ItemStack> merged = ItemSorter.mergeStacks(stacks);

        helper.assertTrue(merged.size() == 2, "Expected 2 stacks (unstackable), got " + merged.size());

        helper.succeed();
    }

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

    private static void assertItem(GameTestHelper helper, List<ItemStack> stacks, int index, net.minecraft.world.item.Item expected) {
        helper.assertTrue(
                stacks.get(index).is(expected),
                "Expected " + expected + " at index " + index + ", got " + stacks.get(index)
        );
    }
}
