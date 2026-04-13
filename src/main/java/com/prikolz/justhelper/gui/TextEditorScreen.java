package com.prikolz.justhelper.gui;

import com.prikolz.justhelper.commands.JustHelperCommand;
import com.prikolz.justhelper.dev.values.Text;
import com.prikolz.justhelper.util.JustMCUtils;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class TextEditorScreen extends Screen {
    private final Text.ParsingType currentFormat;
    private String currentContent;

    protected TextEditorScreen(Text text) {
        super(Component.literal("JustHelper Text Editor"));
        this.currentFormat = text.parsingType;
        this.currentContent = text.text;
    }

    public static TextEditorScreen create(Text text) {
        return new TextEditorScreen(text);
    }

    @Override
    protected void init() {
        var minecraft = Minecraft.getInstance();
        var title = new StringWidget(TextUtils.minimessage("<white>Редактор текста"), minecraft.font);
        title.setPosition(width / 2 - title.getWidth() / 2, 5);

        var formatting = new StringWidget(currentFormat.getNameComponent(), minecraft.font);
        formatting.setPosition(20, height - 35);
        var limit = new StringWidget(TextUtils.minimessage("<gray>" + this.currentContent.length() + "/25000"), minecraft.font);
        limit.setPosition(width - 75, height - 35);

        var editBox =
                MultiLineEditBox.builder()
                .setX(20)
                .setY(20)
                .build(minecraft.font, this.width - 40, this.height - 60, Component.literal("Текст"));
        editBox.setValue(this.currentContent);
        editBox.setValueListener((newValue) -> {
            this.currentContent = newValue;
            limit.setMessage(TextUtils.minimessage("<gray>" + this.currentContent.length() + "/25000"));
        });
        editBox.setCharacterLimit(24999);

        var saveButton =
                Button.builder(Component.translatable("gui.done"), (btn) -> {
                    assert minecraft.player != null;

                    ItemStack newTextItem = JustMCUtils.setTextValue(minecraft.player.getMainHandItem(), this.currentContent);
                    minecraft.player.getInventory().setItem(minecraft.player.getInventory().getSelectedSlot(), newTextItem);
                    minecraft.getConnection().send(new ServerboundSetCreativeModeSlotPacket(36 + minecraft.player.getInventory().getSelectedSlot(), newTextItem));
                    JustHelperCommand.feedback("<green>JustHelper >> Текст обновлен");
                    minecraft.setScreen(null);
                })
                .size(100, 20)
                .pos(width / 2 + 7, height - 25)
                .build();

        var closeButton =
                Button.builder(Component.translatable("gui.cancel"), (btn) -> minecraft.setScreen(null))
                .size(100, 20)
                .pos(width / 2 - 107, height - 25)
                .build();

        addRenderableWidget(title);
        addRenderableWidget(editBox);
        addRenderableWidget(limit);
        addRenderableWidget(formatting);
        addRenderableWidget(closeButton);
        addRenderableWidget(saveButton);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
