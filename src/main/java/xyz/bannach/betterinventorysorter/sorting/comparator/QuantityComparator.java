package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/**
 * Comparator that sorts item stacks by their quantity (stack count).
 *
 * <p>Items with higher quantities sort first (descending by count). Items
 * with equal quantities are sorted alphabetically by their display name.</p>
 *
 * <h2>Sort Order</h2>
 * <ol>
 *   <li>Items are sorted by count in descending order (highest first)</li>
 *   <li>Items with equal counts are sorted alphabetically by name</li>
 * </ol>
 *
 * <h2>Note</h2>
 * <p>The default ascending order for this comparator places highest quantities
 * first. Using descending order will place lowest quantities first.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * items.sort(QuantityComparator.INSTANCE);
 * }</pre>
 *
 * <h2>Side: Common</h2>
 * <p>Can be used on both client and server.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.sorting.SortMethod#QUANTITY
 * @see xyz.bannach.betterinventorysorter.sorting.ItemSorter
 */
public class QuantityComparator implements Comparator<ItemStack> {

    /**
     * Singleton instance of this comparator.
     */
    public static final QuantityComparator INSTANCE = new QuantityComparator();

    /**
     * Private constructor to enforce singleton pattern.
     * Use {@link #INSTANCE} to access this comparator.
     */
    private QuantityComparator() {}

    /**
     * Compares two item stacks by their quantity.
     *
     * <p>Items are compared by count in descending order (higher counts first).
     * If both items have the same count, they are compared alphabetically.</p>
     *
     * @param a the first item stack to compare
     * @param b the second item stack to compare
     * @return a negative integer, zero, or a positive integer if the first item
     *         should sort before, equal to, or after the second
     */
    @Override
    public int compare(ItemStack a, ItemStack b) {
        int result = Integer.compare(b.getCount(), a.getCount());
        if (result != 0) {
            return result;
        }
        return AlphabeticalComparator.INSTANCE.compare(a, b);
    }
}
