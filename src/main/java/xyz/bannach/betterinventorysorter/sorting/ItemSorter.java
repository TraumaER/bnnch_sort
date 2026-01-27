package xyz.bannach.betterinventorysorter.sorting;

import net.minecraft.world.item.ItemStack;
import xyz.bannach.betterinventorysorter.sorting.comparator.AlphabeticalComparator;
import xyz.bannach.betterinventorysorter.sorting.comparator.CategoryComparator;
import xyz.bannach.betterinventorysorter.sorting.comparator.ModIdComparator;
import xyz.bannach.betterinventorysorter.sorting.comparator.QuantityComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ItemSorter {

    private ItemSorter() {}

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

    private static Comparator<ItemStack> getComparator(SortMethod method) {
        return switch (method) {
            case ALPHABETICAL -> AlphabeticalComparator.INSTANCE;
            case CATEGORY -> CategoryComparator.INSTANCE;
            case QUANTITY -> QuantityComparator.INSTANCE;
            case MOD_ID -> ModIdComparator.INSTANCE;
        };
    }
}
