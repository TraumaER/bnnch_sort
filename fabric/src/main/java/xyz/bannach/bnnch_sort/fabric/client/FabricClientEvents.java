package xyz.bannach.bnnch_sort.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import xyz.bannach.bnnch_sort.client.ClientLockedSlotsCache;
import xyz.bannach.bnnch_sort.client.ClientPreferenceCache;
import xyz.bannach.bnnch_sort.client.ScreenButtonInjector;
import xyz.bannach.bnnch_sort.client.SlotLockInputHandler;
import xyz.bannach.bnnch_sort.client.SlotLockRenderer;
import xyz.bannach.bnnch_sort.client.SortButton;
import xyz.bannach.bnnch_sort.client.SortFeedback;
import xyz.bannach.bnnch_sort.client.SortKeyHandler;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;

public class FabricClientEvents {

    public static void register() {
        // S2C packet handlers
        ClientPlayNetworking.registerGlobalReceiver(SyncPreferencePayload.TYPE,
            (payload, context) -> ClientPreferenceCache.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(SyncLockedSlotsPayload.TYPE,
            (payload, context) -> ClientLockedSlotsCache.handle(payload));

        // Per-screen event hooks — only for container screens
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) return;

            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;

            // Inject the sort button
            SortButton button = ScreenButtonInjector.createButton(containerScreen);
            if (button != null) {
                Screens.getButtons(screen).add(button);
            }

            // Key events — sort keybind and cycle preference keybind
            ScreenKeyboardEvents.afterKeyPress(screen).register(
                (s, key, scancode, modifiers) -> SortKeyHandler.onKeyPressed(s, key, scancode));

            // Mouse events — sort on click and lock on alt+click
            ScreenMouseEvents.beforeMouseClick(screen).register(
                (s, mouseX, mouseY, button2) -> SortKeyHandler.onMouseClicked(s, (int) button2));

            ScreenMouseEvents.beforeMouseClick(screen).register(
                (s, mouseX, mouseY, button2) ->
                    SlotLockInputHandler.onMouseClicked(s, mouseX, mouseY, (int) button2));

            // Render events — lock overlays, lock tooltip, and sort feedback overlay
            ScreenEvents.afterRender(screen).register(
                (s, drawContext, mouseX, mouseY, tickDelta) -> {
                    if (s instanceof AbstractContainerScreen<?> cs) {
                        SlotLockRenderer.renderLockOverlays(cs, drawContext);
                        SlotLockRenderer.renderLockTooltip(cs, drawContext, (int) mouseX, (int) mouseY);
                    }

                    Component message = SortFeedback.getDisplayMessage();
                    if (message == null) {
                        return;
                    }

                    var font = Minecraft.getInstance().font;
                    int screenWidth = s.width;
                    int textWidth = font.width(message);
                    int x = (screenWidth - textWidth) / 2;
                    int y = 10;

                    drawContext.fill(x - 4, y - 2, x + textWidth + 4, y + font.lineHeight + 2, 0xAA000000);
                    drawContext.drawString(font, message, x, y, 0xFFFFFF);
                });
        });
    }
}
