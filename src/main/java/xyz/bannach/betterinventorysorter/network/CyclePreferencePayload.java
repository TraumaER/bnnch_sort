package xyz.bannach.betterinventorysorter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

public record CyclePreferencePayload() implements CustomPacketPayload {

    public static final Type<CyclePreferencePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Betterinventorysorter.MODID, "cycle_preference")
    );

    public static final StreamCodec<ByteBuf, CyclePreferencePayload> STREAM_CODEC = StreamCodec.unit(
            new CyclePreferencePayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
