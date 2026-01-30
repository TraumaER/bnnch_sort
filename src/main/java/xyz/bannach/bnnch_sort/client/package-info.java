/**
 * Client-side logic for the Bnnch: Sort mod.
 *
 * <p>This package contains client-side UI components, event handlers, keybinding management, and
 * visual feedback systems. All classes in this package are only loaded on the client.
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortButton} - The sort button UI widget injected into
 *       container screens
 *   <li>{@link xyz.bannach.bnnch_sort.client.ScreenButtonInjector} - Injects sort buttons into
 *       supported container screens (chests, shulker boxes, player inventory)
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortKeyHandler} - Manages keybindings for sorting (R
 *       key) and preference cycling (P key)
 *   <li>{@link xyz.bannach.bnnch_sort.client.ClientPreferenceCache} - Caches the player's sort
 *       preferences locally for UI display
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortFeedback} - Displays overlay messages when sorting
 *       or changing preferences
 *   <li>{@link xyz.bannach.bnnch_sort.client.ClientEvents} - Handles screen events for rendering
 *       and input processing
 *   <li>{@link xyz.bannach.bnnch_sort.client.ClientModBusEvents} - Registers keybindings on the mod
 *       event bus
 * </ul>
 *
 * <h2>Default Keybindings</h2>
 *
 * <ul>
 *   <li><strong>R</strong> - Sort the inventory region under the cursor
 *   <li><strong>P</strong> - Cycle through sort preferences
 * </ul>
 *
 * <h2>Side: Client-only</h2>
 *
 * <p>All classes use {@link net.neoforged.api.distmarker.Dist#CLIENT} to ensure they are only
 * loaded on the client side.
 *
 * @see xyz.bannach.bnnch_sort.client.SortKeyHandler
 * @see xyz.bannach.bnnch_sort.client.SortButton
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.client;
