package xyz.bannach.betterinventorysorter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.server.PreferenceHandler;

/**
 * Network payload sent from client to server to cycle sort preferences.
 *
 * <p>This payload is sent when the player presses the cycle preference keybind.
 * It has no data content; the server advances the player's preferences to the
 * next combination and responds with a {@link SyncPreferencePayload}.</p>
 *
 * <h2>Direction</h2>
 * <p>Client → Server</p>
 *
 * <h2>Handling</h2>
 * <p>Handled by {@link PreferenceHandler#handle(CyclePreferencePayload, net.neoforged.neoforge.network.handling.IPayloadContext)}</p>
 *
 * @since 1.0.0
 * @see PreferenceHandler
 * @see SyncPreferencePayload
 * @see xyz.bannach.betterinventorysorter.sorting.SortPreference#next()
 */
public record CyclePreferencePayload() implements CustomPacketPayload {

    /**
     * The payload type identifier for registration and dispatch.
     */
    public static final Type<CyclePreferencePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Betterinventorysorter.MODID, "cycle_preference")
    );

    /**
     * Codec for encoding and decoding this payload.
     * <p>Uses a unit codec since this payload carries no data.</p>
     */
    public static final StreamCodec<ByteBuf, CyclePreferencePayload> STREAM_CODEC = StreamCodec.unit(
            new CyclePreferencePayload()
    );

    /**
     * Returns the payload type for this packet.
     *
     * @return the registered payload type
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
