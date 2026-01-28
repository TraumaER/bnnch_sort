package xyz.bannach.betterinventorysorter.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.bannach.betterinventorysorter.network.SyncPreferencePayload;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

public class ClientPreferenceCache {

    private static SortMethod method = SortMethod.ALPHABETICAL;
    private static SortOrder order = SortOrder.ASCENDING;
    private static boolean initialized = false;

    public static void handle(SyncPreferencePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> update(payload));
    }

    private static void update(SyncPreferencePayload payload) {
        SortMethod oldMethod = method;
        SortOrder oldOrder = order;

        method = payload.method();
        order = payload.order();

        if (!initialized) {
            initialized = true;
            return;
        }

        // Show feedback for preference changes from server
        if (oldMethod != method || oldOrder != order) {
            SortFeedback.showPreferenceChange(method, order);
        }
    }

    public static SortMethod getMethod() {
        return method;
    }

    public static SortOrder getOrder() {
        return order;
    }
}
