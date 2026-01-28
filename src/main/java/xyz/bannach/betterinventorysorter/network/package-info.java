/**
 * Network payloads for client-server communication.
 *
 * <p>This package defines the custom packet payloads used for communication between
 * the client and server. All payloads implement {@link net.minecraft.network.protocol.common.custom.CustomPacketPayload}
 * and use NeoForge's payload registration system.</p>
 *
 * <h2>Payload Types</h2>
 * <ul>
 *   <li>{@link xyz.bannach.betterinventorysorter.network.SortRequestPayload} - Client-to-server request
 *       to sort a specific inventory region</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.network.CyclePreferencePayload} - Client-to-server request
 *       to cycle the player's sort preferences</li>
 *   <li>{@link xyz.bannach.betterinventorysorter.network.SyncPreferencePayload} - Server-to-client sync
 *       of the player's current sort preferences</li>
 * </ul>
 *
 * <h2>Registration</h2>
 * <p>All payloads are registered in {@link xyz.bannach.betterinventorysorter.network.ModPayloads}
 * during the {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent}.</p>
 *
 * <h2>Side: Common</h2>
 * <p>Network payloads are loaded on both client and server sides.</p>
 *
 * @since 1.0.0
 * @see xyz.bannach.betterinventorysorter.network.ModPayloads
 */
package xyz.bannach.betterinventorysorter.network;
