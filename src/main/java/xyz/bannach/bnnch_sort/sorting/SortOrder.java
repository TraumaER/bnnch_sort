package xyz.bannach.bnnch_sort.sorting;

import net.minecraft.util.StringRepresentable;

/**
 * Enumeration of available sort directions.
 *
 * <p>Sort order determines whether items are arranged in normal order (ascending)
 * or reversed order (descending) after sorting.</p>
 *
 * <h2>Side: Common</h2>
 * <p>Used on both client and server for preference storage and sorting operations.</p>
 *
 * @since 1.0.0
 * @see SortMethod
 * @see SortPreference
 * @see ItemSorter
 */
public enum SortOrder implements StringRepresentable {

    /**
     * Normal sort order (A-Z, lowest to highest, etc.).
     */
    ASCENDING("ascending"),

    /**
     * Reversed sort order (Z-A, highest to lowest, etc.).
     */
    DESCENDING("descending");

    /**
     * Codec for serializing and deserializing SortOrder values.
     * <p>Used for NBT persistence and network communication.</p>
     */
    public static final com.mojang.serialization.Codec<SortOrder> CODEC = StringRepresentable.fromEnum(SortOrder::values);

    /**
     * The string representation used for serialization and translation keys.
     */
    private final String serializedName;

    /**
     * Constructs a SortOrder with the given serialized name.
     *
     * @param serializedName the string identifier for this sort order
     */
    SortOrder(String serializedName) {
        this.serializedName = serializedName;
    }

    /**
     * Returns the serialized name of this sort order.
     *
     * @return the string identifier for this sort order
     */
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Returns the opposite sort order.
     *
     * @return {@link #DESCENDING} if this is {@link #ASCENDING}, or {@link #ASCENDING} if this is {@link #DESCENDING}
     */
    public SortOrder toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }

    /**
     * Returns the translation key for this sort order's display name.
     *
     * @return the translation key in the format "sort_order.bnnch_sort.{name}"
     */
    public String getTranslationKey() {
        return "sort_order.bnnch_sort." + serializedName;
    }
}
