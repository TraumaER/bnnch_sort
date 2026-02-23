/**
 * Brigadier slash commands for the Bnnch: Sort mod.
 *
 * <p>This package provides the {@code /bnnchsort} command tree for controlling inventory sorting
 * through chat commands. Commands are registered using NeoForge's command registration event.
 *
 * <h2>Available Commands</h2>
 *
 * <ul>
 *   <li>{@code /bnnchsort sortinv [region]} - Sort player inventory (regions: all, main, hotbar)
 *   <li>{@code /bnnchsort change <method> <order>} - Change sort preferences
 *   <li>{@code /bnnchsort reset} - Reset preferences to server defaults
 *   <li>{@code /bnnchsort config [key]} - View current configuration values
 *   <li>{@code /bnnchsort help} - Display help information
 * </ul>
 *
 * <h2>Sort Methods</h2>
 *
 * <ul>
 *   <li>{@code alphabetical} - Sort by item name
 *   <li>{@code category} - Sort by creative tab
 *   <li>{@code quantity} - Sort by stack count
 *   <li>{@code mod_id} - Sort by mod namespace
 * </ul>
 *
 * <h2>Sort Orders</h2>
 *
 * <ul>
 *   <li>{@code ascending} - Normal sort order
 *   <li>{@code descending} - Reversed sort order
 * </ul>
 *
 * <h2>Side: Common</h2>
 *
 * <p>Commands are registered on both client and server, but execute server-side.
 *
 * @see xyz.bannach.bnnch_sort.commands.ModCommands
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.commands;
