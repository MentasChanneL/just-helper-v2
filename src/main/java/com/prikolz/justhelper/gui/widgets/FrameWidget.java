package com.prikolz.justhelper.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FrameWidget extends AbstractWidget {

    public int backgroundColor = 0xff000000;
    public int frameColor1 = 0xffFFFFFF;
    public int frameColor2 = frameColor1;
    public int frameWidth = 1;
    public int fieldW;
    public int fieldH;

    public FrameWidget(int w, int h) {
        super(0, 0, 0, 0, Component.literal("frame"));
        this.fieldW = w;
        this.fieldH = h;
    }

    public FrameWidget setPos(int x, int y) {
        setX(x);
        setY(y);
        return this;
    }

    public FrameWidget setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public FrameWidget setLeftColor(int color) {
        this.frameColor1 = color;
        return this;
    }

    public FrameWidget setRightColor(int color) {
        this.frameColor2 = color;
        return this;
    }

    public FrameWidget setFrameWidth(int width) {
        this.frameWidth = width;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fillGradient(
                getX(),
                getY(),
                getX() + fieldW,
                getY() + fieldH,
                frameColor1,
                frameColor2
        );
        guiGraphics.fillGradient(
                getX() + frameWidth,
                getY() + frameWidth,
                getX() + fieldW - frameWidth,
                getY() + fieldH - frameWidth,
                backgroundColor,
                backgroundColor
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
