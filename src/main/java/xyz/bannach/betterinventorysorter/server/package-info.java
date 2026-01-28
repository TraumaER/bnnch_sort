/**
 * Server-side logic for the Better Inventory Sorter mod.
 *
 * <p>This package contains server-side event handlers and payload processors that
 * handle sort requests, preference cycling, and player login synchronization.</p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link xyz.bannach.betterinventorysorter.server.SortHandler} - Processes sort requests,
 *       determines target slots, and executes the sorting operation</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.server.PreferenceHandler} - Handles preference
 *       cycling requests and syncs updated preferences to the client</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.server.ServerEvents} - Listens for player login
 *       events to sync preferences on join</li>
 * </ul>
 *
 * <h2>Inventory Regions</h2>
 * <p>The server supports three distinct sorting regions defined in {@link xyz.bannach.betterinventorysorter.server.SortHandler}:</p>
 * <ul>
 *   <li>{@code REGION_CONTAINER} (0) - The container's inventory (chest, shulker box, etc.)</li>
 *   <li>{@code REGION_PLAYER_MAIN} (1) - Player's main inventory (slots 9-35)</li>
 *   <li>{@code REGION_PLAYER_HOTBAR} (2) - Player's hotbar (slots 0-8)</li>
 * </ul>
 *
 * <h2>Side: Server-only</h2>
 * <p>All sorting operations are performed server-side to ensure data integrity and
 * prevent client-side manipulation.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.server.SortHandler
 * @see xyz.bannach.betterinventorysorter.sorting.ItemSorter
 */
package xyz.bannach.betterinventorysorter.server;
