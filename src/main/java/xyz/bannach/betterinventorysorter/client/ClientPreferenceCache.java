package xyz.bannach.betterinventorysorter.client;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.betterinventorysorter.network.SyncPreferencePayload;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

public class ClientPreferenceCache {

    private static final long DISPLAY_DURATION_MS = 2000;

    private static SortMethod method = SortMethod.ALPHABETICAL;
    private static SortOrder order = SortOrder.ASCENDING;
    private static boolean initialized = false;

    private static Component displayMessage = null;
    private static long displayExpiryMs = 0;

    public static void handle(SyncPreferencePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> update(payload));
    }

    private static void update(SyncPreferencePayload payload) {
        method = payload.method();
        order = payload.order();

        if (!initialized) {
            initialized = true;
            return;
        }

        displayMessage = Component.translatable("message.betterinventorysorter.preference_changed",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey()));
        displayExpiryMs = System.currentTimeMillis() + DISPLAY_DURATION_MS;
    }

    public static Component getDisplayMessage() {
        if (displayMessage != null && System.currentTimeMillis() < displayExpiryMs) {
            return displayMessage;
        }
        return null;
    }

    public static SortMethod getMethod() {
        return method;
    }

    public static SortOrder getOrder() {
        return order;
    }
}
