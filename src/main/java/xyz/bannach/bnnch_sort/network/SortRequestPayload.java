package xyz.bannach.bnnch_sort.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.server.SortHandler;

/**
 * Network payload sent from client to server to request inventory sorting.
 *
 * <p>This payload is sent when the player triggers a sort action via keybind or button click. It
 * contains the region identifier specifying which inventory area to sort.
 *
 * <h2>Direction</h2>
 *
 * <p>Client → Server
 *
 * <h2>Handling</h2>
 *
 * <p>Handled by {@link SortHandler#handle(SortRequestPayload,
 * net.neoforged.neoforge.network.handling.IPayloadContext)}
 *
 * @param region the inventory region to sort (see {@link SortHandler} for region constants)
 * @see SortHandler#REGION_CONTAINER
 * @see SortHandler#REGION_PLAYER_MAIN
 * @see SortHandler#REGION_PLAYER_HOTBAR
 * @since 1.0.0
 */
public record SortRequestPayload(int region) implements CustomPacketPayload {

  /** The payload type identifier for registration and dispatch. */
  public static final Type<SortRequestPayload> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(BnnchSort.MODID, "sort_request"));

  /** Codec for encoding and decoding this payload to/from a byte buffer. */
  public static final StreamCodec<ByteBuf, SortRequestPayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT, SortRequestPayload::region, SortRequestPayload::new);

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
