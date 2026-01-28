/**
 * Client-side logic for the Better Inventory Sorter mod.
 *
 * <p>This package contains client-side UI components, event handlers, keybinding management,
 * and visual feedback systems. All classes in this package are only loaded on the client.</p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.SortButton} - The sort button UI widget
 *       injected into container screens</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.ScreenButtonInjector} - Injects sort buttons
 *       into supported container screens (chests, shulker boxes, player inventory)</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.SortKeyHandler} - Manages keybindings for
 *       sorting (R key) and preference cycling (P key)</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.ClientPreferenceCache} - Caches the player's
 *       sort preferences locally for UI display</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.SortFeedback} - Displays overlay messages
 *       when sorting or changing preferences</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.ClientEvents} - Handles screen events for
 *       rendering and input processing</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client.ClientModBusEvents} - Registers keybindings
 *       on the mod event bus</li>
 * </ul>
 *
 * <h2>Default Keybindings</h2>
 * <ul>
 *   <li><strong>R</strong> - Sort the inventory region under the cursor</li>
 *   <li><strong>P</strong> - Cycle through sort preferences</li>
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 * <p>All classes use {@link net.neoforged.api.distmarker.Dist#CLIENT} to ensure they are
 * only loaded on the client side.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.client.SortKeyHandler
 * @see xyz.bannach.betterinventorysorter.client.SortButton
 */
package xyz.bannach.betterinventorysorter.client;
