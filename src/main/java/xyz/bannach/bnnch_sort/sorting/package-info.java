/**
 * Core sorting logic for the Bnnch: Sort mod.
 *
 * <p>This package contains the sorting engine, preference management, and sort method/order enums.
 * The sorting system supports multiple sort strategies with ascending/descending order options.</p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.ItemSorter} - Main sorting utility that handles
 *       stack merging, sorting, and empty slot padding</li>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.SortMethod} - Enum defining available sort methods
 *       (alphabetical, category, quantity, mod ID)</li>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.SortOrder} - Enum defining sort directions
 *       (ascending, descending)</li>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting.SortPreference} - Immutable record holding a
 *       player's current sort method and order preferences</li>
 * </ul>
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
 * @since 1.0.0
 * @see xyz.bannach.bnnch_sort.sorting.ItemSorter
 * @see xyz.bannach.bnnch_sort.sorting.comparator
 */
package xyz.bannach.bnnch_sort.sorting;
