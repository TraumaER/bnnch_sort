package xyz.bannach.betterinventorysorter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

public record CycleMethodPayload(boolean cycleMethod) implements CustomPacketPayload {

    public static final Type<CycleMethodPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Betterinventorysorter.MODID, "cycle_method")
    );

    public static final StreamCodec<ByteBuf, CycleMethodPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CycleMethodPayload::cycleMethod,
            CycleMethodPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
