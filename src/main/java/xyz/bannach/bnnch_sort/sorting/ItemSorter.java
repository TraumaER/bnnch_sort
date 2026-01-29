package xyz.bannach.bnnch_sort.sorting;

import net.minecraft.world.item.ItemStack;
import xyz.bannach.bnnch_sort.sorting.comparator.AlphabeticalComparator;
import xyz.bannach.bnnch_sort.sorting.comparator.CategoryComparator;
import xyz.bannach.bnnch_sort.sorting.comparator.ModIdComparator;
import xyz.bannach.bnnch_sort.sorting.comparator.QuantityComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Core utility class for sorting inventory item stacks.
 *
 * <p>This class provides the main sorting pipeline that handles stack merging,
 * sorting, and slot padding. It is designed to be stateless with all methods
 * being static.</p>
 *
 * <h2>Sorting Pipeline</h2>
 * <ol>
 *   <li>Condense partial stacks into full stacks where possible</li>
 *   <li>Filter out empty slots</li>
 *   <li>Sort non-empty items using the appropriate comparator</li>
 *   <li>Reverse if descending order is selected</li>
 *   <li>Pad with empty stacks to preserve original slot count</li>
 * </ol>
 *
 * <h2>Side: Common</h2>
 * <p>This class is used primarily server-side for actual sorting, but the logic
 * is available on both sides.</p>
 *
 * @since 1.0.0
 * @see SortPreference
 * @see xyz.bannach.bnnch_sort.sorting.comparator
 */
public final class ItemSorter {

    /**
     * Private constructor to prevent instantiation.
     */
    private ItemSorter() {}

    /**
     * Sorts a list of item stacks according to the given preferences.
     *
     * <p>The sorting process:</p>
     * <ol>
     *   <li>Merges partial stacks of the same item type</li>
     *   <li>Removes empty stacks from consideration</li>
     *   <li>Sorts remaining items using the preference's method</li>
     *   <li>Reverses order if preference specifies descending</li>
     *   <li>Pads with empty stacks to match original size</li>
     * </ol>
     *
     * @param stacks the list of item stacks to sort (not modified)
     * @param preference the sorting preferences containing method and order
     * @return a new list containing the sorted stacks with preserved slot count
     */
    public static List<ItemStack> sort(List<ItemStack> stacks, SortPreference preference) {
        int originalSize = stacks.size();

        // 1. Condense partial stacks
        List<ItemStack> merged = mergeStacks(stacks);

        // 2. Filter empties
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : merged) {
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }

        // 3. Sort by comparator
        Comparator<ItemStack> comparator = getComparator(preference.method());
        items.sort(comparator);

        // 4. Reverse if descending
        if (preference.order() == SortOrder.DESCENDING) {
            Collections.reverse(items);
        }

        // 5. Pad with empties to restore original size
        while (items.size() < originalSize) {
            items.add(ItemStack.EMPTY);
        }

        return items;
    }

    /**
     * Merges partial stacks of the same item type into full stacks.
     *
     * <p>This method combines stacks that share the same item and components,
     * respecting each item's maximum stack size. Items that cannot be stacked
     * (different items, different NBT, or max stack size of 1) remain separate.</p>
     *
     * <p>Example: Two stacks of 32 Stone become one stack of 64 Stone.</p>
     *
     * @param stacks the list of item stacks to merge (not modified)
     * @return a new list containing merged stacks (empty stacks are excluded)
     */
    public static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack original : stacks) {
            if (original.isEmpty()) {
                continue;
            }

            ItemStack toMerge = original.copy();

            // Try to merge into existing stacks in result
            for (ItemStack existing : result) {
                if (toMerge.isEmpty()) {
                    break;
                }
                if (ItemStack.isSameItemSameComponents(existing, toMerge)) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space > 0) {
                        int transfer = Math.min(space, toMerge.getCount());
                        existing.grow(transfer);
                        toMerge.shrink(transfer);
                    }
                }
            }

            // If there's anything left, add as new stack
            if (!toMerge.isEmpty()) {
                result.add(toMerge);
            }
        }

        return result;
    }

    /**
     * Returns the appropriate comparator for the given sort method.
     *
     * @param method the sort method to get a comparator for
     * @return the singleton comparator instance for the specified method
     */
    private static Comparator<ItemStack> getComparator(SortMethod method) {
        return switch (method) {
            case ALPHABETICAL -> AlphabeticalComparator.INSTANCE;
            case CATEGORY -> CategoryComparator.INSTANCE;
            case QUANTITY -> QuantityComparator.INSTANCE;
            case MOD_ID -> ModIdComparator.INSTANCE;
        };
    }
}
