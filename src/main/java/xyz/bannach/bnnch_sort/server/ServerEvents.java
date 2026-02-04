package xyz.bannach.bnnch_sort.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

/**
 * Server-side event listeners for the mod.
 *
 * <p>This class handles server-side events that require mod interaction, such as synchronizing
 * player preferences when they log in.
 *
 * <h2>Handled Events</h2>
 *
 * <ul>
 *   <li>{@link PlayerEvent.PlayerLoggedInEvent} - Sync preferences on player login
 * </ul>
 *
 * <h2>Side: Server-only</h2>
 *
 * <p>These events only fire on the server (or integrated server for singleplayer).
 *
 * @see SyncPreferencePayload
 * @see ModAttachments#SORT_PREFERENCE
 * @since 1.0.0
 */
@EventBusSubscriber(modid = BnnchSort.MODID)
public class ServerEvents {

  /** Private constructor to prevent instantiation of this utility class. */
  private ServerEvents() {}

  /**
   * Handles player login events by synchronizing their sort preferences.
   *
   * <p>When a player logs in, their stored sort preferences are read from the player data
   * attachment and sent to the client via a {@link SyncPreferencePayload}. This ensures the
   * client's preference cache is up-to-date for UI display.
   *
   * @param event the player login event
   */
  @SubscribeEvent
  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);
      PacketDistributor.sendToPlayer(
          player, new SyncPreferencePayload(pref.method(), pref.order()));

      LockedSlots locked = player.getData(ModAttachments.LOCKED_SLOTS);
      PacketDistributor.sendToPlayer(player, new SyncLockedSlotsPayload(locked.slots()));
    }
  }
}
