package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/**
 * Comparator that sorts item stacks alphabetically by their display name.
 *
 * <p>This comparator uses case-insensitive string comparison on the item's
 * hover name (display name). It serves as both a primary comparator for
 * alphabetical sorting and as a tiebreaker for other comparators.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * items.sort(AlphabeticalComparator.INSTANCE);
 * }</pre>
 *
 * <h2>Side: Common</h2>
 * <p>Can be used on both client and server.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.sorting.SortMethod#ALPHABETICAL
 * @see xyz.bannach.betterinventorysorter.sorting.ItemSorter
 */
public class AlphabeticalComparator implements Comparator<ItemStack> {

    /**
     * Singleton instance of this comparator.
     */
    public static final AlphabeticalComparator INSTANCE = new AlphabeticalComparator();

    /**
     * Private constructor to enforce singleton pattern.
     * Use {@link #INSTANCE} to access this comparator.
     */
    private AlphabeticalComparator() {}

    /**
     * Compares two item stacks by their display names.
     *
     * <p>Comparison is case-insensitive and based on the localized display name
     * returned by {@link ItemStack#getHoverName()}.</p>
     *
     * @param a the first item stack to compare
     * @param b the second item stack to compare
     * @return a negative integer, zero, or a positive integer if the first item's
     *         name is alphabetically less than, equal to, or greater than the second
     */
    @Override
    public int compare(ItemStack a, ItemStack b) {
        return String.CASE_INSENSITIVE_ORDER.compare(
                a.getHoverName().getString(),
                b.getHoverName().getString()
        );
    }
}
