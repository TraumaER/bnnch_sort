package xyz.bannach.betterinventorysorter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

public record SortRequestPayload(int region) implements CustomPacketPayload {

    public static final Type<SortRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Betterinventorysorter.MODID, "sort_request")
    );

    public static final StreamCodec<ByteBuf, SortRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SortRequestPayload::region,
            SortRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
