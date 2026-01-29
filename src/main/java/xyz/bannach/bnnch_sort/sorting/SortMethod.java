package xyz.bannach.bnnch_sort.sorting;

import net.minecraft.util.StringRepresentable;

/**
 * Enumeration of available sorting methods for inventory items.
 *
 * <p>Each sort method corresponds to a different {@link java.util.Comparator} implementation
 * in the {@link xyz.bannach.bnnch_sort.sorting.comparator} package.</p>
 *
 * <h2>Side: Common</h2>
 * <p>Used on both client and server for preference storage and sorting operations.</p>
 *
 * @since 1.0.0
 * @see SortOrder
 * @see SortPreference
 * @see ItemSorter
 */
public enum SortMethod implements StringRepresentable {

    /**
     * Sort items alphabetically by their display name (case-insensitive).
     *
     * @see xyz.bannach.bnnch_sort.sorting.comparator.AlphabeticalComparator
     */
    ALPHABETICAL("alphabetical"),

    /**
     * Sort items by their creative mode tab category, then alphabetically within each category.
     *
     * @see xyz.bannach.bnnch_sort.sorting.comparator.CategoryComparator
     */
    CATEGORY("category"),

    /**
     * Sort items by stack quantity (highest first), then alphabetically for equal quantities.
     *
     * @see xyz.bannach.bnnch_sort.sorting.comparator.QuantityComparator
     */
    QUANTITY("quantity"),

    /**
     * Sort items by their mod namespace (mod ID), then alphabetically within each mod.
     *
     * @see xyz.bannach.bnnch_sort.sorting.comparator.ModIdComparator
     */
    MOD_ID("mod_id");

    /**
     * Codec for serializing and deserializing SortMethod values.
     * <p>Used for NBT persistence and network communication.</p>
     */
    public static final com.mojang.serialization.Codec<SortMethod> CODEC = StringRepresentable.fromEnum(SortMethod::values);

    /**
     * The string representation used for serialization and translation keys.
     */
    private final String serializedName;

    /**
     * Constructs a SortMethod with the given serialized name.
     *
     * @param serializedName the string identifier for this sort method
     */
    SortMethod(String serializedName) {
        this.serializedName = serializedName;
    }

    /**
     * Returns the serialized name of this sort method.
     *
     * @return the string identifier for this sort method
     */
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Returns the next sort method in the cycle.
     *
     * <p>The cycle order is: ALPHABETICAL → CATEGORY → QUANTITY → MOD_ID → ALPHABETICAL</p>
     *
     * @return the next sort method in the enumeration order, wrapping to the first after the last
     */
    public SortMethod next() {
        SortMethod[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    /**
     * Returns the translation key for this sort method's display name.
     *
     * @return the translation key in the format "sort_method.bnnch_sort.{name}"
     */
    public String getTranslationKey() {
        return "sort_method.bnnch_sort." + serializedName;
    }
}
