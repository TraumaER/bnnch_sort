/**
 * Root package for the Bnnch: Sort mod.
 *
 * <p>This NeoForge mod for Minecraft 1.21.1 provides enhanced inventory sorting capabilities with
 * configurable sort methods and orders. The mod supports sorting player inventories and container
 * inventories (chests, shulker boxes, etc.) using various sorting strategies.
 *
 * <h2>Package Structure</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.sorting} - Core sorting logic and comparators
 *   <li>{@link xyz.bannach.bnnch_sort.network} - Network payloads for client-server communication
 *   <li>{@link xyz.bannach.bnnch_sort.server} - Server-side event handlers and sort processing
 *   <li>{@link xyz.bannach.bnnch_sort.client} - Client-side UI, keybindings, and feedback
 *   <li>{@link xyz.bannach.bnnch_sort.commands} - Brigadier slash commands
 *   <li>{@link xyz.bannach.bnnch_sort.test} - NeoForge GameTest framework tests
 * </ul>
 *
 * <h2>Key Classes</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.BnnchSort} - Main mod entrypoint
 *   <li>{@link xyz.bannach.bnnch_sort.Config} - Mod configuration (client and server)
 *   <li>{@link xyz.bannach.bnnch_sort.ModAttachments} - Player data attachments for sort
 *       preferences
 * </ul>
 *
 * @see xyz.bannach.bnnch_sort.BnnchSort
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort;
