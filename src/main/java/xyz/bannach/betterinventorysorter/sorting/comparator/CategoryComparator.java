package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class CategoryComparator implements Comparator<ItemStack> {

    public static final CategoryComparator INSTANCE = new CategoryComparator();

    private static final int UNCATEGORIZED_OFFSET = 1_000_000;

    @Override
    public int compare(ItemStack a, ItemStack b) {
        int indexA = getTabIndex(a);
        int indexB = getTabIndex(b);
        int result = Integer.compare(indexA, indexB);
        if (result != 0) {
            return result;
        }
        return AlphabeticalComparator.INSTANCE.compare(a, b);
    }

    private static int getTabIndex(ItemStack stack) {
        var tabs = CreativeModeTabs.allTabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).contains(stack)) {
                return i;
            }
        }
        // Uncategorized items sort last, ordered by registry ID
        return UNCATEGORIZED_OFFSET + BuiltInRegistries.ITEM.getId(stack.getItem());
    }
}
