package xyz.bannach.betterinventorysorter.client;

import net.minecraft.network.chat.Component;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

public class SortFeedback {

    private static final long DISPLAY_DURATION_MS = 2000;

    private static Component displayMessage = null;
    private static long displayExpiryMs = 0;

    public static void showSorted(SortMethod method, SortOrder order) {
        Component message = Component.translatable("message.betterinventorysorter.sorted",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey()));
        displayOverlay(message);
    }

    public static void showPreferenceChange(SortMethod method, SortOrder order) {
        Component message = Component.translatable("message.betterinventorysorter.preference_changed",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey()));
        displayOverlay(message);
    }

    private static void displayOverlay(Component message) {
        displayMessage = message;
        displayExpiryMs = System.currentTimeMillis() + DISPLAY_DURATION_MS;
    }

    public static Component getDisplayMessage() {
        if (displayMessage != null && System.currentTimeMillis() < displayExpiryMs) {
            return displayMessage;
        }
        return null;
    }
}
