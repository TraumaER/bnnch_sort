package xyz.bannach.betterinventorysorter.sorting;

import net.minecraft.util.StringRepresentable;

public enum SortMethod implements StringRepresentable {
    ALPHABETICAL("alphabetical"),
    CATEGORY("category"),
    QUANTITY("quantity"),
    MOD_ID("mod_id");

    public static final com.mojang.serialization.Codec<SortMethod> CODEC = StringRepresentable.fromEnum(SortMethod::values);

    private final String serializedName;

    SortMethod(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public SortMethod next() {
        SortMethod[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String getTranslationKey() {
        return "sort_method.betterinventorysorter." + serializedName;
    }
}
