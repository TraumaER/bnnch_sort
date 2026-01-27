package xyz.bannach.betterinventorysorter.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.betterinventorysorter.Betterinventorysorter;
import xyz.bannach.betterinventorysorter.network.CycleMethodPayload;
import xyz.bannach.betterinventorysorter.network.SortRequestPayload;

import java.util.List;

public class SortButton extends Button {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Betterinventorysorter.MODID, "textures/gui/sort_button.png"
    );

    private static final int SIZE = 16;

    private final AbstractContainerScreen<?> parentScreen;
    private final int sortRegion;

    public SortButton(AbstractContainerScreen<?> parentScreen, int sortRegion) {
        super(0, 0, SIZE, SIZE, Component.empty(), b -> {}, DEFAULT_NARRATION);
        this.parentScreen = parentScreen;
        this.sortRegion = sortRegion;
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            PacketDistributor.sendToServer(new CycleMethodPayload(true));
        } else if (Screen.hasControlDown()) {
            PacketDistributor.sendToServer(new CycleMethodPayload(false));
        } else {
            PacketDistributor.sendToServer(new SortRequestPayload(sortRegion));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setX(parentScreen.getGuiLeft() + parentScreen.getXSize() + 2);
        setY(parentScreen.getGuiTop());

        // Background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xCC222222);

        // Texture icon
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        // Tooltip on hover
        if (isHovered) {
            Component status = Component.translatable("tooltip.betterinventorysorter.sort_button",
                    Component.translatable(ClientPreferenceCache.getMethod().getTranslationKey()),
                    Component.translatable(ClientPreferenceCache.getOrder().getTranslationKey()));
            Component hintShift = Component.translatable("tooltip.betterinventorysorter.sort_button_hint_shift");
            Component hintCtrl = Component.translatable("tooltip.betterinventorysorter.sort_button_hint_ctrl");
            guiGraphics.renderTooltip(
                    parentScreen.getMinecraft().font,
                    List.of(status, hintShift, hintCtrl),
                    java.util.Optional.empty(),
                    mouseX, mouseY
            );
        }
    }
}
