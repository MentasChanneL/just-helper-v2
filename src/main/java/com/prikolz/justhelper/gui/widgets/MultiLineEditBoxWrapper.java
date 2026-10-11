package com.prikolz.justhelper.gui.widgets;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class MultiLineEditBoxWrapper extends AbstractTextAreaWidget {
    public final MultiLineEditBox hold;

    public MultiLineEditBoxWrapper(MultiLineEditBox hold, boolean showBackground, boolean showDecorations) {
        super(
                hold.getX(),
                hold.getY(),
                hold.getWidth(),
                hold.getHeight(),
                hold.getMessage(),
                defaultSettings(1),
                showBackground,
                showDecorations
        );
        this.hold = hold;
    }

    @Override
    public void setFocused(boolean bl) {
        hold.setFocused(bl);
    }

    @Override
    protected int getInnerHeight() {
        return hold.getInnerHeight();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        hold.extractRenderState(guiGraphics, i, j, f);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        hold.extractRenderState(guiGraphics, i, j, f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void mouseMoved(double d, double e) {
        hold.mouseMoved(d, e);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return hold.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return hold.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        return hold.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return hold.isMouseOver(d, e);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return hold.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return hold.keyReleased(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        return hold.charTyped(characterEvent);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return hold.shouldTakeFocusAfterInteraction();
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return hold.getCurrentFocusPath();
    }

    @Override
    public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
        return hold.getBorderForArrowNavigation(screenDirection);
    }

    @Override
    public void setPosition(int i, int j) {
        super.setPosition(i, j);
        hold.setPosition(i, j);
    }

    @Override
    public Collection<? extends NarratableEntry> getNarratables() {
        return hold.getNarratables();
    }
}
