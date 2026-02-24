package com.prikolz.justhelper.gui;

import com.google.gson.JsonObject;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.commands.JustHelperCommand;
import com.prikolz.justhelper.mixin.MultiLineEditBoxMixin;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ConfigScreen extends Screen {

    private JSONHolder editBox;
    private Button saveButton;
    private Button closeButton;

    protected ConfigScreen() {
        super(Component.literal("JustHelper configuration"));
    }

    public static ConfigScreen create() {
        return new ConfigScreen();
    }

    @Override
    protected void init() {
        var json = editBox == null ? Config.getJSON() : editBox.box.getValue();
        var minecraft = Minecraft.getInstance();
        var title = new StringWidget(TextUtils.minimessage("<blue>Just<red>Helper <#FFFFBB>JSON Config"), minecraft.font);
        title.setPosition(width / 2 - title.getWidth() / 2, 5);
        saveButton = Button.builder(Component.translatable("gui.done"), (btn) -> {
            Config.saveConfig(this.editBox.box.getValue());
            Config.get().read();
            Minecraft.getInstance().setScreen(null);
            JustHelperCommand.feedback("<green>JustHelper >> Конфиг обновлен");
        }).size(100, 20).pos(width / 2 + 7, height - 25).build();

        var editBox = net.minecraft.client.gui.components.MultiLineEditBox.builder()
                .setX(20)
                .setY(20)
                .build(minecraft.font, this.width - 40, this.height - 60, Component.literal("JSON"));
        editBox.setValue( json );
        this.editBox = new JSONHolder(editBox, saveButton);

        closeButton = Button.builder(Component.translatable("gui.cancel"), (btn) -> {
            minecraft.setScreen(null);
        }).size(100, 20).pos(width / 2 - 107, height - 25).build();

        var resetButton = Button.builder(Component.translatable("controls.reset"), (btn) -> {
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    "Сбросить конфиг?",
                    "Конфиг JustHelper будет сброшен до значений по умолчанию.",
                    () -> {
                        Config.printLogs( Config.get().reset() );
                        Config.get().read();
                        JustHelperCommand.feedback("<green>JustHelper >> Конфиг обновлен");
                        Minecraft.getInstance().setScreen(null);
                    },
                    () -> Minecraft.getInstance().setScreen(this)
            ));
        }).pos(width - 70, height - 40).width(50).build();

        var folderButton = ImageButton.builder(TextUtils.minimessage("<white><font:just-helper:icons>0"), (btn) -> {
            Config.openConfigFolder();
        }).tooltip(Tooltip.create(Component.literal("Открыть папку"))).pos(20, height - 40).width(20).build();

        this.editBox.checkValid();
        addRenderableWidget(this.editBox);
        addRenderableWidget(closeButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(title);
        addRenderableWidget(resetButton);
        addRenderableWidget(folderButton);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    static class JSONHolder extends AbstractWidget {

        private final MultiLineEditBox box;
        private final Button saveButton;
        private ExceptionInfo error = new ExceptionInfo(0, false, "");
        private final Font font;

        public JSONHolder(MultiLineEditBox box, Button saveButton) {
            super(box.getX(), box.getY(), box.getWidth(), box.getHeight(), Component.literal("JSON"));
            this.box = box;
            this.saveButton = saveButton;
            font = Minecraft.getInstance().font;
            box.setScrollAmount(0.0);
        }

        public void checkValid() {
            var json = box.getValue();
            error = new ExceptionInfo(0, false, "");
            saveButton.active = true;
            saveButton.setTooltip(null);
            try {
                var obj = JustHelperClient.GSON.fromJson(json, JsonObject.class);
                JustHelperClient.GSON.toJson(obj);
            } catch (Throwable t) {
                var lineError = 0;
                var errorMessage = t.getMessage() == null ? "line 0" : t.getMessage();
                var i = errorMessage.indexOf("line ");
                if (i != -1) try {
                    var sub = errorMessage.substring(i + 5);
                    i = sub.indexOf(" ");
                    if (i != -1) lineError = Integer.parseInt(sub.substring(0, i));
                } catch (Throwable ignore) {}
                error = new ExceptionInfo(lineError, true, t.getMessage());
                saveButton.active = false;
                saveButton.setTooltip(Tooltip.create(
                        TextUtils.minimessage("<red>Ошибка JSON\n" + TextUtils.splitByWord(t.getMessage(), 40)),
                        Component.literal( t.getMessage() )
                ));
            }
            var abox = (MultiLineEditBoxMixin) box;
            abox.setTextColor( error.error ? 0xffFFAA55 : 0xffFFFFBB);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            box.renderWidget(guiGraphics, i, j, f);
            if (error.error) {
                var lineCount = ((MultiLineEditBoxMixin) box).getTextField().getLineCount();
                var pos = ((double) error.line / lineCount) * (box.maxScrollAmount() + box.getHeight() - 4);
                if (box.scrollAmount() > pos) return;
                pos = (pos * 0.99 - 5 - box.scrollAmount());
                if (pos > box.getHeight()) return;
                int markerX = box.getX() - 12;
                guiGraphics.drawString(font, "⚠", markerX,(int) (pos) + box.getY(), 0xffFFAA00);
            }
        }

        @Override
        public void setFocused(boolean bl) {
            box.setFocused(bl);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            box.updateWidgetNarration(narrationElementOutput);
        }

        @Override
        public void mouseMoved(double d, double e) {
            box.mouseMoved(d, e);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
            return box.mouseClicked(event, bl);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
            return box.mouseReleased(mouseButtonEvent);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
            return box.mouseDragged(mouseButtonEvent, d, e);
        }

        @Override
        protected void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e) {
            ((MultiLineEditBoxMixin) box).onDrag(mouseButtonEvent, d, e);
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            boolean result = box.keyPressed(keyEvent);
            checkValid();
            return result;
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            boolean result = box.charTyped(event);
            checkValid();
            return result;
        }

        @Override
        public boolean mouseScrolled(double d, double e, double f, double g) {
            return box.mouseScrolled(d, e, f ,g);
        }

        @Override
        public boolean isMouseOver(double d, double e) {
            return box.isMouseOver(d, e);
        }

        @Override
        public @Nullable ComponentPath getCurrentFocusPath() {
            return box.getCurrentFocusPath();
        }

        @Override
        public @NotNull ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
            return box.getBorderForArrowNavigation(screenDirection);
        }

        @Override
        public void setPosition(int i, int j) {
            box.setPosition(i, j);
        }

        @Override
        public @NotNull Collection<? extends NarratableEntry> getNarratables() {
            return box.getNarratables();
        }

        public record ExceptionInfo(int line, boolean error, String message) {}
    }

}
