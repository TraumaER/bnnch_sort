package xyz.bannach.bnnch_sort.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.client.ClientLockedSlotsCache;
import xyz.bannach.bnnch_sort.client.ClientPreferenceCache;
import xyz.bannach.bnnch_sort.server.LockHandler;
import xyz.bannach.bnnch_sort.server.PreferenceHandler;
import xyz.bannach.bnnch_sort.server.SortHandler;

/**
 * Registers all network payloads for the mod.
 *
 * <p>This class handles the registration of custom packet payloads during the {@link
 * RegisterPayloadHandlersEvent}. It sets up bidirectional communication between client and server
 * for sort requests and preference synchronization.
 *
 * <h2>Registered Payloads</h2>
 *
 * <ul>
 *   <li>{@link SortRequestPayload} - Client → Server: Request to sort an inventory region
 *   <li>{@link CyclePreferencePayload} - Client → Server: Request to cycle sort preferences
 *   <li>{@link SyncPreferencePayload} - Server → Client: Sync current preferences to client
 * </ul>
 *
 * <h2>Side: Common</h2>
 *
 * <p>Registration occurs on both sides during the network setup phase.
 *
 * @see SortRequestPayload
 * @see CyclePreferencePayload
 * @see SyncPreferencePayload
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID)
public class ModPayloads {

  /** Private constructor to prevent instantiation of this utility class. */
  private ModPayloads() {}

  /**
   * Registers all mod network payloads.
   *
   * <p>This method is called during network initialization and registers handlers for all custom
   * packets used by the mod. The protocol version "1" ensures compatibility checking between client
   * and server.
   *
   * @param event the payload registration event
   */
  @SubscribeEvent
  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar("1");
    registrar.playToServer(
        SortRequestPayload.TYPE, SortRequestPayload.STREAM_CODEC, SortHandler::handle);
    registrar.playToServer(
        CyclePreferencePayload.TYPE,
        CyclePreferencePayload.STREAM_CODEC,
        PreferenceHandler::handle);
    registrar.playToClient(
        SyncPreferencePayload.TYPE,
        SyncPreferencePayload.STREAM_CODEC,
        ClientPreferenceCache::handle);
    registrar.playToServer(
        ToggleLockPayload.TYPE, ToggleLockPayload.STREAM_CODEC, LockHandler::handle);
    registrar.playToClient(
        SyncLockedSlotsPayload.TYPE,
        SyncLockedSlotsPayload.STREAM_CODEC,
        ClientLockedSlotsCache::handle);
  }
}
