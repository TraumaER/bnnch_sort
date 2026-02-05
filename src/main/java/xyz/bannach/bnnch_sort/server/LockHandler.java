package xyz.bannach.bnnch_sort.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;

/**
 * Server-side handler for slot lock toggle requests.
 *
 * <p>Processes incoming toggle lock requests from clients, updates the player's locked slots
 * attachment, and synchronizes the full state back to the client.
 *
 * <h2>Side: Server-only</h2>
 *
 * @see ToggleLockPayload
 * @see SyncLockedSlotsPayload
 * @since 1.1.0
 */
public class LockHandler {

  private LockHandler() {}

  /**
   * Handles an incoming toggle lock request from a client.
   *
   * @param payload the toggle lock payload containing the slot index
   * @param context the network context containing the sending player
   */
  public static void handle(ToggleLockPayload payload, IPayloadContext context) {
    context.enqueueWork(
        () -> {
          ServerPlayer player = (ServerPlayer) context.player();
          int slotIndex = payload.slotIndex();

          if (slotIndex < 0 || slotIndex > 35) {
            return;
          }

          LockedSlots current = player.getData(ModAttachments.LOCKED_SLOTS);
          LockedSlots updated = current.toggle(slotIndex);
          player.setData(ModAttachments.LOCKED_SLOTS, updated);

          PacketDistributor.sendToPlayer(player, new SyncLockedSlotsPayload(updated.slots()));
        });
  }
}
