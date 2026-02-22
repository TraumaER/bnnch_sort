/**
 * Client-side logic for the Bnnch: Sort mod.
 *
 * <p>This package contains client-side UI components, keybinding management, and visual feedback
 * systems. All classes in this package are only loaded on the client.
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortButton} - The sort button UI widget injected into
 *       container screens
 *   <li>{@link xyz.bannach.bnnch_sort.client.ScreenButtonInjector} - Creates sort buttons for
 *       supported container screens (chests, shulker boxes, player inventory)
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortKeyHandler} - Manages keybindings for sorting (R
 *       key) and preference cycling (P key)
 *   <li>{@link xyz.bannach.bnnch_sort.client.ClientPreferenceCache} - Caches the player's sort
 *       preferences locally for UI display
 *   <li>{@link xyz.bannach.bnnch_sort.client.SortFeedback} - Displays overlay messages when sorting
 *       or changing preferences
 *   <li>{@link xyz.bannach.bnnch_sort.client.SlotLockRenderer} - Renders lock overlays on locked
 *       inventory slots
 *   <li>{@link xyz.bannach.bnnch_sort.client.SlotLockInputHandler} - Handles modifier+click input
 *       for toggling slot locks
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
 * <p>All classes in this package are intended to be used only on the client side.
 *
 * @see xyz.bannach.bnnch_sort.client.SortKeyHandler
 * @see xyz.bannach.bnnch_sort.client.SortButton
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.client;
