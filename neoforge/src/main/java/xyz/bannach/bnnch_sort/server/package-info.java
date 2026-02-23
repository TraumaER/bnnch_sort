/**
 * Server-side logic for the Bnnch: Sort mod.
 *
 * <p>This package contains server-side event handlers and payload processors that handle sort
 * requests, preference cycling, and player login synchronization.
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.server.SortHandler} - Processes sort requests, determines
 *       target slots, and executes the sorting operation
 *   <li>{@link xyz.bannach.bnnch_sort.server.PreferenceHandler} - Handles preference cycling
 *       requests and syncs updated preferences to the client
 *   <li>{@link xyz.bannach.bnnch_sort.server.ServerEvents} - Listens for player login events to
 *       sync preferences on join
 * </ul>
 *
 * <h2>Inventory Regions</h2>
 *
 * <p>The server supports three distinct sorting regions defined in {@link
 * xyz.bannach.bnnch_sort.server.SortHandler}:
 *
 * <ul>
 *   <li>{@code REGION_CONTAINER} (0) - The container's inventory (chest, shulker box, etc.)
 *   <li>{@code REGION_PLAYER_MAIN} (1) - Player's main inventory (slots 9-35)
 *   <li>{@code REGION_PLAYER_HOTBAR} (2) - Player's hotbar (slots 0-8)
 * </ul>
 *
 * <h2>Side: Server-only</h2>
 *
 * <p>All sorting operations are performed server-side to ensure data integrity and prevent
 * client-side manipulation.
 *
 * @see xyz.bannach.bnnch_sort.server.SortHandler
 * @see xyz.bannach.bnnch_sort.sorting.ItemSorter
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.server;
