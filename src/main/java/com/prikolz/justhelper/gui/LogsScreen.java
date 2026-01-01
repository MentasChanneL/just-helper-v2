package com.prikolz.justhelper.gui;

import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LogsScreen extends Screen {
    public LogsScreen() {
        super(Component.literal("JustHelper Logs"));
    }

    @Override
    protected void init() {
        this.minecraft = Minecraft.getInstance();
        var title = new StringWidget(TextUtils.minimessage("<blue>Just<red>Helper <#FFFFBB>Logs"), minecraft.font);
        title.setPosition(width / 2 - minecraft.font.width(title.getMessage().getString()) / 2, 10);
        var box = MultiLineEditBox.builder()
                .setX(width / 4).setY(30)
                .build(minecraft.font, width - (width / 4) * 2, height / 2, Component.literal("Logs"));
        var stupidButton = Button.builder(Component.literal("Понятно"), (btn) -> {
            Minecraft.getInstance().setScreen(null);
        }).pos(width / 2 - 50, height / 2 + 30).width(100).build();
        box.setValue(JustHelperClient.LOGGER.unionCache());

        addRenderableWidget(title);
        addRenderableWidget(new LogsHolder(box));
        addRenderableWidget(stupidButton);
    }

    public static class LogsHolder extends AbstractWidget {

        public final MultiLineEditBox box;

        public LogsHolder(MultiLineEditBox box) {
            super(box.getX(), box.getY(), box.getWidth(), box.getHeight(), box.getMessage());
            this.box = box;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            box.renderWidget(guiGraphics, i, j, f);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public void mouseMoved(double d, double e) {
            box.mouseMoved(d, e);
        }

        @Override
        public boolean mouseScrolled(double d, double e, double f, double g) {
            return box.mouseScrolled(d, e, f, g);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            return box.mouseClicked(d, e, i);
        }

        @Override
        public boolean mouseReleased(double d, double e, int i) {
            return box.mouseReleased(d, e, i);
        }

        @Override
        public boolean mouseDragged(double d, double e, int i, double f, double g) {
            return box.mouseDragged(d, e, i, f, g);
        }

        @Override
        public boolean isMouseOver(double d, double e) {
            return box.isMouseOver(d, e);
        }

        @Override
        public void onClick(double d, double e) {
            box.onClick(d, e);
        }
    }
}
