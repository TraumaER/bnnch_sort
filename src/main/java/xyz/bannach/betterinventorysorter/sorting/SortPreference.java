package xyz.bannach.betterinventorysorter.sorting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SortPreference(SortMethod method, SortOrder order) {

    public static final SortPreference DEFAULT = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);

    public static final Codec<SortPreference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SortMethod.CODEC.fieldOf("method").forGetter(SortPreference::method),
            SortOrder.CODEC.fieldOf("order").forGetter(SortPreference::order)
    ).apply(instance, SortPreference::new));

    public SortPreference withNextMethod() {
        return new SortPreference(method.next(), order);
    }

    public SortPreference withToggledOrder() {
        return new SortPreference(method, order.toggle());
    }
}
