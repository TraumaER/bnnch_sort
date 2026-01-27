package xyz.bannach.betterinventorysorter.sorting.comparator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class ModIdComparator implements Comparator<ItemStack> {

    public static final ModIdComparator INSTANCE = new ModIdComparator();

    @Override
    public int compare(ItemStack a, ItemStack b) {
        String nsA = BuiltInRegistries.ITEM.getKey(a.getItem()).getNamespace();
        String nsB = BuiltInRegistries.ITEM.getKey(b.getItem()).getNamespace();
        int result = nsA.compareToIgnoreCase(nsB);
        if (result != 0) {
            return result;
        }
        return AlphabeticalComparator.INSTANCE.compare(a, b);
    }
}
