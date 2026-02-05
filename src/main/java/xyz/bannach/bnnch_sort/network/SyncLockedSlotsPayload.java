package xyz.bannach.bnnch_sort.network;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.bannach.bnnch_sort.BnnchSort;

/**
 * Network payload sent from server to client to synchronize locked slot state.
 *
 * <h2>Direction</h2>
 *
 * <p>Server → Client
 *
 * @param lockedSlots the set of locked slot indices
 * @since 1.1.0
 */
public record SyncLockedSlotsPayload(Set<Integer> lockedSlots) implements CustomPacketPayload {

  /** The payload type identifier for registration and dispatch. */
  public static final Type<SyncLockedSlotsPayload> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(BnnchSort.MODID, "sync_locked_slots"));

  /** Stream codec for encoding a set of integers over the network. */
  private static final StreamCodec<ByteBuf, Set<Integer>> INT_SET_CODEC =
      ByteBufCodecs.VAR_INT
          .apply(ByteBufCodecs.list())
          .map(HashSet::new, set -> set.stream().sorted().toList());

  /** Codec for encoding and decoding this payload to/from a byte buffer. */
  public static final StreamCodec<ByteBuf, SyncLockedSlotsPayload> STREAM_CODEC =
      StreamCodec.composite(
          INT_SET_CODEC, SyncLockedSlotsPayload::lockedSlots, SyncLockedSlotsPayload::new);

  /**
   * Returns the payload type for this packet.
   *
   * @return the registered payload type
   */
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
