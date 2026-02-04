package xyz.bannach.bnnch_sort.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.bannach.bnnch_sort.BnnchSort;

/**
 * Network payload sent from client to server to toggle a slot's lock state.
 *
 * <h2>Direction</h2>
 *
 * <p>Client → Server
 *
 * @param slotIndex the player inventory slot index to toggle (0-35)
 * @since 1.1.0
 */
public record ToggleLockPayload(int slotIndex) implements CustomPacketPayload {

  /** The payload type identifier for registration and dispatch. */
  public static final Type<ToggleLockPayload> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(BnnchSort.MODID, "toggle_lock"));

  /** Codec for encoding and decoding this payload to/from a byte buffer. */
  public static final StreamCodec<ByteBuf, ToggleLockPayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT, ToggleLockPayload::slotIndex, ToggleLockPayload::new);

  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
