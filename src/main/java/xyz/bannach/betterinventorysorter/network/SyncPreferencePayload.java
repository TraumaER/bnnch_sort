package xyz.bannach.betterinventorysorter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

public record SyncPreferencePayload(SortMethod method, SortOrder order) implements CustomPacketPayload {

    public static final Type<SyncPreferencePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Betterinventorysorter.MODID, "sync_preference")
    );

    public static final StreamCodec<ByteBuf, SyncPreferencePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(
                    SyncPreferencePayload::methodFromName,
                    SortMethod::getSerializedName
            ), SyncPreferencePayload::method,
            ByteBufCodecs.STRING_UTF8.map(
                    SyncPreferencePayload::orderFromName,
                    SortOrder::getSerializedName
            ), SyncPreferencePayload::order,
            SyncPreferencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static SortMethod methodFromName(String name) {
        for (SortMethod m : SortMethod.values()) {
            if (m.getSerializedName().equals(name)) {
                return m;
            }
        }
        return SortMethod.ALPHABETICAL;
    }

    private static SortOrder orderFromName(String name) {
        for (SortOrder o : SortOrder.values()) {
            if (o.getSerializedName().equals(name)) {
                return o;
            }
        }
        return SortOrder.ASCENDING;
    }
}
