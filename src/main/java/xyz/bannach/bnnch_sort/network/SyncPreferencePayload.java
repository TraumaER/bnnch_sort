package xyz.bannach.bnnch_sort.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.bannach.bnnch_sort.BnnchSort;
import xyz.bannach.bnnch_sort.client.ClientPreferenceCache;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

/**
 * Network payload sent from server to client to synchronize sort preferences.
 *
 * <p>This payload is sent when the player logs in or when their preferences change on the server.
 * It contains the current sort method and order that the client should display in the UI.
 *
 * <h2>Direction</h2>
 *
 * <p>Server → Client
 *
 * <h2>Handling</h2>
 *
 * <p>Handled by {@link ClientPreferenceCache#handle(SyncPreferencePayload,
 * net.neoforged.neoforge.network.handling.IPayloadContext)}
 *
 * @param method the player's current sort method
 * @param order the player's current sort order
 * @see SortMethod
 * @see SortOrder
 * @see ClientPreferenceCache
 * @since 1.0.0
 */
public record SyncPreferencePayload(SortMethod method, SortOrder order)
    implements CustomPacketPayload {

  /** The payload type identifier for registration and dispatch. */
  public static final Type<SyncPreferencePayload> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(BnnchSort.MODID, "sync_preference"));

  /**
   * Codec for encoding and decoding this payload to/from a byte buffer.
   *
   * <p>Encodes enums as their serialized string names for readability and stability.
   */
  public static final StreamCodec<ByteBuf, SyncPreferencePayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8.map(
              SyncPreferencePayload::methodFromName, SortMethod::getSerializedName),
          SyncPreferencePayload::method,
          ByteBufCodecs.STRING_UTF8.map(
              SyncPreferencePayload::orderFromName, SortOrder::getSerializedName),
          SyncPreferencePayload::order,
          SyncPreferencePayload::new);

  /**
   * Returns the payload type for this packet.
   *
   * @return the registered payload type
   */
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /**
   * Parses a SortMethod from its serialized name.
   *
   * @param name the serialized name to parse
   * @return the matching SortMethod, or {@link SortMethod#ALPHABETICAL} if not found
   */
  private static SortMethod methodFromName(String name) {
    for (SortMethod m : SortMethod.values()) {
      if (m.getSerializedName().equals(name)) {
        return m;
      }
    }
    return SortMethod.ALPHABETICAL;
  }

  /**
   * Parses a SortOrder from its serialized name.
   *
   * @param name the serialized name to parse
   * @return the matching SortOrder, or {@link SortOrder#ASCENDING} if not found
   */
  private static SortOrder orderFromName(String name) {
    for (SortOrder o : SortOrder.values()) {
      if (o.getSerializedName().equals(name)) {
        return o;
      }
    }
    return SortOrder.ASCENDING;
  }
}
