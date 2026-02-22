/**
 * Network payloads for client-server communication.
 *
 * <p>This package defines the custom packet payloads used for communication between the client and
 * server. All payloads implement {@link
 * net.minecraft.network.protocol.common.custom.CustomPacketPayload} and use NeoForge's payload
 * registration system.
 *
 * <h2>Payload Types</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.network.SortRequestPayload} - Client-to-server request to
 *       sort a specific inventory region
 *   <li>{@link xyz.bannach.bnnch_sort.network.CyclePreferencePayload} - Client-to-server request to
 *       cycle the player's sort preferences
 *   <li>{@link xyz.bannach.bnnch_sort.network.SyncPreferencePayload} - Server-to-client sync of the
 *       player's current sort preferences
 * </ul>
 *
 * <h2>Registration</h2>
 *
 * <p>All payloads are registered in {@link xyz.bannach.bnnch_sort.network.ModPayloads} during the
 * {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent}.
 *
 * <h2>Side: Common</h2>
 *
 * <p>Network payloads are loaded on both client and server sides.
 *
 * @see xyz.bannach.bnnch_sort.network.ModPayloads
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.network;
