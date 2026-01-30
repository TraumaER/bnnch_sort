package xyz.bannach.bnnch_sort.sorting.comparator;

import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

/**
 * Comparator that sorts item stacks by their creative mode tab category.
 *
 * <p>Items are grouped by the creative mode tab they belong to, with items
 * in earlier tabs sorting before items in later tabs. Items within the same
 * tab are sorted alphabetically. Uncategorized items (not in any tab) sort
 * last, ordered by their registry ID.</p>
 *
 * <h2>Sort Order</h2>
 * <ol>
 *   <li>Items are grouped by creative tab index (registration order)</li>
 *   <li>Within each tab, items are sorted alphabetically by name</li>
 *   <li>Items not in any tab sort last, by registry ID</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * items.sort(CategoryComparator.INSTANCE);
 * }</pre>
 *
 * <h2>Side: Common</h2>
 * <p>Can be used on both client and server.</p>
 *
 * @see xyz.bannach.bnnch_sort.sorting.SortMethod#CATEGORY
 * @see xyz.bannach.bnnch_sort.sorting.ItemSorter
 * @since 1.0.0
 */
public class CategoryComparator implements Comparator<ItemStack> {

    /**
     * Singleton instance of this comparator.
     */
    public static final CategoryComparator INSTANCE = new CategoryComparator();

    /**
     * Offset added to registry IDs for uncategorized items to ensure they sort last.
     */
    private static final int UNCATEGORIZED_OFFSET = 1_000_000;

    /**
     * Private constructor to enforce singleton pattern.
     * Use {@link #INSTANCE} to access this comparator.
     */
    private CategoryComparator() {
    }

    /**
     * Compares two item stacks by their creative tab category.
     *
     * <p>Items are first compared by their creative tab index. If both items
     * are in the same tab (or both uncategorized), they are compared alphabetically.</p>
     *
     * @param a the first item stack to compare
     * @param b the second item stack to compare
     * @return a negative integer, zero, or a positive integer if the first item
     * should sort before, equal to, or after the second
     */
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

    /**
     * Gets the sorting index for an item based on its creative tab.
     *
     * <p>Items in creative tabs return the tab's index (0-based). Items not in
     * any tab return {@link #UNCATEGORIZED_OFFSET} plus their registry ID to
     * ensure consistent ordering.</p>
     *
     * @param stack the item stack to get the tab index for
     * @return the tab index, or a large value for uncategorized items
     */
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
