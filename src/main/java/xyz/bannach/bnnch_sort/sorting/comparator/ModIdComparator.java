package xyz.bannach.bnnch_sort.sorting.comparator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/**
 * Comparator that sorts item stacks by their mod namespace (mod ID).
 *
 * <p>Items are grouped by the mod that registered them, with vanilla Minecraft
 * items (namespace "minecraft") typically sorting first. Items from the same
 * mod are sorted alphabetically by their display name.</p>
 *
 * <h2>Sort Order</h2>
 * <ol>
 *   <li>Items are grouped by mod namespace (case-insensitive)</li>
 *   <li>Within each mod, items are sorted alphabetically by name</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * items.sort(ModIdComparator.INSTANCE);
 * }</pre>
 *
 * <h2>Side: Common</h2>
 * <p>Can be used on both client and server.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.bnnch_sort.sorting.SortMethod#MOD_ID
 * @see xyz.bannach.bnnch_sort.sorting.ItemSorter
 */
public class ModIdComparator implements Comparator<ItemStack> {

    /**
     * Singleton instance of this comparator.
     */
    public static final ModIdComparator INSTANCE = new ModIdComparator();

    /**
     * Private constructor to enforce singleton pattern.
     * Use {@link #INSTANCE} to access this comparator.
     */
    private ModIdComparator() {}

    /**
     * Compares two item stacks by their mod namespace.
     *
     * <p>Items are first compared by their registry namespace (mod ID) using
     * case-insensitive comparison. If both items are from the same mod, they
     * are compared alphabetically by display name.</p>
     *
     * @param a the first item stack to compare
     * @param b the second item stack to compare
     * @return a negative integer, zero, or a positive integer if the first item
     *         should sort before, equal to, or after the second
     */
    @Override
    public int compare(ItemStack a, ItemStack b) {
        String nsA = BuiltInRegistries.ITEM.getKey(a.getItem()).getNamespace();
        String nsB = BuiltInRegistries.ITEM.getKey(b.getItem()).getNamespace();
        int result = nsA.compareToIgnoreCase(nsB);
        if (result != 0) {
            return result;
        }
        return AlphabeticalComparator.INSTANCE.compare(a, b);
    }
}
