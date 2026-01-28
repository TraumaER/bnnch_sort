package xyz.bannach.betterinventorysorter.client;

import net.minecraft.network.chat.Component;
import xyz.bannach.betterinventorysorter.sorting.SortMethod;
import xyz.bannach.betterinventorysorter.sorting.SortOrder;

/**
 * Manages visual feedback messages displayed to the player.
 *
 * <p>This class provides a simple overlay message system that displays temporary
 * feedback when sorting is performed or preferences are changed. Messages are
 * displayed for a fixed duration before automatically expiring.</p>
 *
 * <h2>Message Types</h2>
 * <ul>
 *   <li>{@link #showSorted(SortMethod, SortOrder)} - Displayed after a sort operation</li>
 *   <li>{@link #showPreferenceChange(SortMethod, SortOrder)} - Displayed when preferences change</li>
 * </ul>
 *
 * <h2>Rendering</h2>
 * <p>Messages are rendered by {@link ClientEvents#onScreenRender(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post)}
 * as a centered overlay at the top of the screen.</p>
 *
 * <h2>Side: Client-only</h2>
 * <p>Feedback is purely visual and only exists on the client.</p>
 *
 * @since 1.0.0
 * @see ClientEvents
 */
public class SortFeedback {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SortFeedback() {}

    /**
     * Duration in milliseconds that feedback messages are displayed.
     * <p>Value: {@value} ms (2 seconds)</p>
     */
    private static final long DISPLAY_DURATION_MS = 2000;

    /**
     * The current message being displayed, or null if none.
     */
    private static Component displayMessage = null;

    /**
     * Timestamp (in system milliseconds) when the current message expires.
     */
    private static long displayExpiryMs = 0;

    /**
     * Displays a feedback message indicating that sorting was performed.
     *
     * <p>The message uses the translation key "message.betterinventorysorter.sorted"
     * with the method and order as parameters.</p>
     *
     * @param method the sort method that was used
     * @param order the sort order that was used
     */
    public static void showSorted(SortMethod method, SortOrder order) {
        Component message = Component.translatable("message.betterinventorysorter.sorted",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey()));
        displayOverlay(message);
    }

    /**
     * Displays a feedback message indicating that preferences were changed.
     *
     * <p>The message uses the translation key "message.betterinventorysorter.preference_changed"
     * with the new method and order as parameters.</p>
     *
     * @param method the new sort method
     * @param order the new sort order
     */
    public static void showPreferenceChange(SortMethod method, SortOrder order) {
        Component message = Component.translatable("message.betterinventorysorter.preference_changed",
                Component.translatable(method.getTranslationKey()),
                Component.translatable(order.getTranslationKey()));
        displayOverlay(message);
    }

    /**
     * Sets the overlay message and its expiry time.
     *
     * @param message the message to display
     */
    private static void displayOverlay(Component message) {
        displayMessage = message;
        displayExpiryMs = System.currentTimeMillis() + DISPLAY_DURATION_MS;
    }

    /**
     * Returns the current display message if it hasn't expired.
     *
     * @return the message to display, or null if no message or the message has expired
     */
    public static Component getDisplayMessage() {
        if (displayMessage != null && System.currentTimeMillis() < displayExpiryMs) {
            return displayMessage;
        }
        return null;
    }
}
