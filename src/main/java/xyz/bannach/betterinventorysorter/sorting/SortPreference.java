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

    /**
     * Cycles to the next preference combination.
     * Pattern: Toggle order first, then advance method when wrapping from DESCENDING to ASCENDING.
     * This creates the sequence: Alphabetical(Asc) -> Alphabetical(Desc) -> Category(Asc) -> ...
     */
    public SortPreference next() {
        SortOrder nextOrder = this.order.toggle();
        SortMethod nextMethod = this.method;

        // When toggling from DESCENDING to ASCENDING, advance to next method
        if (this.order == SortOrder.DESCENDING) {
            nextMethod = this.method.next();
        }

        return new SortPreference(nextMethod, nextOrder);
    }
}
