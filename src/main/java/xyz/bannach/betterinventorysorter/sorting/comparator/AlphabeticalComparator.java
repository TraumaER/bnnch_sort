package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class AlphabeticalComparator implements Comparator<ItemStack> {

    public static final AlphabeticalComparator INSTANCE = new AlphabeticalComparator();

    @Override
    public int compare(ItemStack a, ItemStack b) {
        return String.CASE_INSENSITIVE_ORDER.compare(
                a.getHoverName().getString(),
                b.getHoverName().getString()
        );
    }
}
