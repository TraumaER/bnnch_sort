package xyz.bannach.betterinventorysorter.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.betterinventorysorter.network.SortRequestPayload;
import xyz.bannach.betterinventorysorter.sorting.ItemSorter;
import xyz.bannach.betterinventorysorter.sorting.SortPreference;

import java.util.ArrayList;
import java.util.List;

public class SortHandler {

    public static final int REGION_CONTAINER = 0;
    public static final int REGION_PLAYER_MAIN = 1;
    public static final int REGION_PLAYER_HOTBAR = 2;

    public static void handle(SortRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            AbstractContainerMenu menu = player.containerMenu;

            List<Slot> targetSlots = getTargetSlots(menu, payload.region());
            if (targetSlots.isEmpty()) {
                return;
            }

            // Extract ItemStacks from slots
            List<ItemStack> stacks = new ArrayList<>();
            for (Slot slot : targetSlots) {
                stacks.add(slot.getItem().copy());
            }

            // Sort stacks
            List<ItemStack> sorted = ItemSorter.sort(stacks, SortPreference.DEFAULT);

            // Write sorted stacks back to slots
            for (int i = 0; i < targetSlots.size(); i++) {
                targetSlots.get(i).set(sorted.get(i));
            }

            // Sync to client
            menu.broadcastChanges();
        });
    }

    private static List<Slot> getTargetSlots(AbstractContainerMenu menu, int region) {
        List<Slot> slots = new ArrayList<>();

        for (Slot slot : menu.slots) {
            boolean matches = switch (region) {
                case REGION_CONTAINER -> !(slot.container instanceof Inventory);
                case REGION_PLAYER_MAIN -> slot.container instanceof Inventory && slot.getContainerSlot() >= 9 && slot.getContainerSlot() <= 35;
                case REGION_PLAYER_HOTBAR -> slot.container instanceof Inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 8;
                default -> false;
            };

            if (matches) {
                slots.add(slot);
            }
        }

        return slots;
    }
}
