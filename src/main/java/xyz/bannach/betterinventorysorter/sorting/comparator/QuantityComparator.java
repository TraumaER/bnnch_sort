package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class QuantityComparator implements Comparator<ItemStack> {

    public static final QuantityComparator INSTANCE = new QuantityComparator();

    @Override
    public int compare(ItemStack a, ItemStack b) {
        int result = Integer.compare(b.getCount(), a.getCount());
        if (result != 0) {
            return result;
        }
        return AlphabeticalComparator.INSTANCE.compare(a, b);
    }
}
