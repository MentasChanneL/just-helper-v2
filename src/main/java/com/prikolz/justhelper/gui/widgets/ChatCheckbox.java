package com.prikolz.justhelper.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class ChatCheckbox extends AbstractWidget {
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.parse("just-helper:checkbox_selected");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.parse("just-helper:checkbox");
    private static final ResourceLocation CHECKBOX_SWAG = ResourceLocation.parse("just-helper:ludi");
    private final Font font;

    private ResourceLocation resource;
    private boolean isSelected;
    private final OnChange onChange;

    public ChatCheckbox(int i, int j, Component component, boolean initial, OnChange onChange) {
        super(i, j, 10, 10, component);
        this.font = Minecraft.getInstance().font;
        this.onChange = onChange;
        this.isSelected = initial;
        resource = isSelected ? CHECKBOX_SELECTED_SPRITE : CHECKBOX_SPRITE;
        if (this.isSelected) makeSwag();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resource, this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        guiGraphics.drawString(font, this.getMessage(), this.getX() - 3 - font.width(this.getMessage()), this.getY(), 0xffFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void onClick(double d, double e) {
        isSelected = !isSelected;
        resource = isSelected ? CHECKBOX_SELECTED_SPRITE : CHECKBOX_SPRITE;
        onChange.onChange(this, isSelected);
        if (this.isSelected) makeSwag();
    }

    private void makeSwag() {
        if (Math.random() > 0.05) return;
        resource = CHECKBOX_SWAG;
    }

    public interface OnChange {
        void onChange(ChatCheckbox widget, boolean value);
    }
}
