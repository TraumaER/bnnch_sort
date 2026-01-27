package xyz.bannach.betterinventorysorter.sorting;

import net.minecraft.util.StringRepresentable;

public enum SortOrder implements StringRepresentable {
    ASCENDING("ascending"),
    DESCENDING("descending");

    public static final com.mojang.serialization.Codec<SortOrder> CODEC = StringRepresentable.fromEnum(SortOrder::values);

    private final String serializedName;

    SortOrder(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public SortOrder toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }

    public String getTranslationKey() {
        return "sort_order.betterinventorysorter." + serializedName;
    }
}
