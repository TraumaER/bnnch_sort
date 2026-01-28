/**
 * Root package for the Better Inventory Sorter mod.
 *
 * <p>This NeoForge mod for Minecraft 1.21.1 provides enhanced inventory sorting capabilities
 * with configurable sort methods and orders. The mod supports sorting player inventories
 * and container inventories (chests, shulker boxes, etc.) using various sorting strategies.</p>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@link xyz.bannach.betterinventorysorter.sorting} - Core sorting logic and comparators</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.network} - Network payloads for client-server communication</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.server} - Server-side event handlers and sort processing</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.client} - Client-side UI, keybindings, and feedback</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.commands} - Brigadier slash commands</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.test} - NeoForge GameTest framework tests</li>
 * </ul>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link xyz.bannach.betterinventorysorter.Betterinventorysorter} - Main mod entrypoint</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.Config} - Mod configuration (client and server)</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.ModAttachments} - Player data attachments for sort preferences</li>
 * </ul>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.Betterinventorysorter
 */
package xyz.bannach.betterinventorysorter;
