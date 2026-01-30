/**
 * Brigadier slash commands for the Bnnch: Sort mod.
 *
 * <p>This package provides the {@code /bnnchsort} command tree for controlling inventory sorting
 * through chat commands. Commands are registered using NeoForge's command registration event.</p>
 *
 * <h2>Available Commands</h2>
 * <ul>
 *   <li>{@code /bnnchsort sortinv [region]} - Sort player inventory (regions: all, main, hotbar)</li>
 *   <li>{@code /bnnchsort change <method> <order>} - Change sort preferences</li>
 *   <li>{@code /bnnchsort reset} - Reset preferences to server defaults</li>
 *   <li>{@code /bnnchsort config [key]} - View current configuration values</li>
 *   <li>{@code /bnnchsort help} - Display help information</li>
 * </ul>
 *
 * <h2>Sort Methods</h2>
 * <ul>
 *   <li>{@code alphabetical} - Sort by item name</li>
 *   <li>{@code category} - Sort by creative tab</li>
 *   <li>{@code quantity} - Sort by stack count</li>
 *   <li>{@code mod_id} - Sort by mod namespace</li>
 * </ul>
 *
 * <h2>Sort Orders</h2>
 * <ul>
 *   <li>{@code ascending} - Normal sort order</li>
 *   <li>{@code descending} - Reversed sort order</li>
 * </ul>
 *
 * <h2>Side: Common</h2>
 * <p>Commands are registered on both client and server, but execute server-side.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.bnnch_sort.commands.ModCommands
 */
package xyz.bannach.bnnch_sort.commands;
