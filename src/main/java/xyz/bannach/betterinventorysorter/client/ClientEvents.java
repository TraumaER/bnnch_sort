package xyz.bannach.betterinventorysorter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;

@EventBusSubscriber(modid = Betterinventorysorter.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        SortKeyHandler.onKeyPressed(event);
    }

    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        SortKeyHandler.onMouseClicked(event);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        ScreenButtonInjector.onScreenInit(event);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Component message = ClientPreferenceCache.getDisplayMessage();
        if (message == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        var font = Minecraft.getInstance().font;
        int screenWidth = event.getScreen().width;
        int textWidth = font.width(message);
        int x = (screenWidth - textWidth) / 2;
        int y = 10;

        guiGraphics.fill(x - 4, y - 2, x + textWidth + 4, y + font.lineHeight + 2, 0xAA000000);
        guiGraphics.drawString(font, message, x, y, 0xFFFFFF);
    }
}