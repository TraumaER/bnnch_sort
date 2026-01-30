/**
 * ItemStack comparators for different sorting strategies.
 *
 * <p>This package provides singleton comparator implementations for each sort method supported by
 * the mod. All comparators use alphabetical comparison as a tiebreaker to ensure consistent, stable
 * sort results.
 *
 * <h2>Available Comparators</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.comparator.AlphabeticalComparator} - Sorts items by
 *       their display name (case-insensitive)
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.comparator.CategoryComparator} - Groups items by
 *       creative mode tab, then alphabetically within each group
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.comparator.QuantityComparator} - Sorts by stack count
 *       (highest first), then alphabetically
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.comparator.ModIdComparator} - Groups items by mod
 *       namespace, then alphabetically within each group
 * </ul>
 *
 * <h2>Usage Pattern</h2>
 *
 * <p>All comparators are singletons accessed via their {@code INSTANCE} field:
 *
 * <pre>{@code
 * Comparator<ItemStack> comparator = AlphabeticalComparator.INSTANCE;
 * items.sort(comparator);
 * }</pre>
 *
 * @see xyz.bannach.bnnch_sort.sorting.ItemSorter
 * @see xyz.bannach.bnnch_sort.sorting.SortMethod
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.sorting.comparator;
