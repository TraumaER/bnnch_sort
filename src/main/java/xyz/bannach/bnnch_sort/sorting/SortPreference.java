package xyz.bannach.bnnch_sort.sorting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Immutable record holding a player's sort preferences.
 *
 * <p>A SortPreference combines a {@link SortMethod} and {@link SortOrder} to define
 * how items should be sorted. Instances are immutable; modification methods return
 * new instances with the updated values.</p>
 *
 * <h2>Persistence</h2>
 * <p>Sort preferences are persisted using the {@link #CODEC} for NBT serialization
 * and attached to players via {@link xyz.bannach.bnnch_sort.ModAttachments#SORT_PREFERENCE}.</p>
 *
 * <h2>Side: Common</h2>
 * <p>Used on both client and server for preference storage and sorting operations.</p>
 *
 * @param method the sort method to use (alphabetical, category, quantity, or mod_id)
 * @param order the sort direction (ascending or descending)
 * @since 1.0.0
 * @see SortMethod
 * @see SortOrder
 * @see ItemSorter
 */
public record SortPreference(SortMethod method, SortOrder order) {

    /**
     * The default sort preference: {@link SortMethod#ALPHABETICAL} with {@link SortOrder#ASCENDING}.
     */
    public static final SortPreference DEFAULT = new SortPreference(SortMethod.ALPHABETICAL, SortOrder.ASCENDING);

    /**
     * Codec for serializing and deserializing SortPreference to/from NBT.
     * <p>Used by {@link xyz.bannach.bnnch_sort.ModAttachments} for player data persistence.</p>
     */
    public static final Codec<SortPreference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SortMethod.CODEC.fieldOf("method").forGetter(SortPreference::method),
            SortOrder.CODEC.fieldOf("order").forGetter(SortPreference::order)
    ).apply(instance, SortPreference::new));

    /**
     * Returns a new preference with the next sort method, keeping the current order.
     *
     * @return a new SortPreference with the next method in the cycle
     * @see SortMethod#next()
     */
    public SortPreference withNextMethod() {
        return new SortPreference(method.next(), order);
    }

    /**
     * Returns a new preference with the toggled sort order, keeping the current method.
     *
     * @return a new SortPreference with the opposite order
     * @see SortOrder#toggle()
     */
    public SortPreference withToggledOrder() {
        return new SortPreference(method, order.toggle());
    }

    /**
     * Cycles to the next preference combination.
     *
     * <p>The cycling pattern toggles order first, then advances method when wrapping
     * from DESCENDING to ASCENDING. This creates the sequence:</p>
     * <pre>
     * Alphabetical(Asc) → Alphabetical(Desc) → Category(Asc) → Category(Desc) → ...
     * </pre>
     *
     * @return a new SortPreference representing the next combination in the cycle
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
