package com.prikolz.justhelper.gui;

import com.prikolz.justhelper.gui.widgets.FrameWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public class ConfirmScreen extends Screen {
    public final String title;
    public final String description;
    public final Runnable onYes;
    public final Runnable onNo;

    private final Font font;

    public ConfirmScreen(String title, @Nullable String description, Runnable onYes, Runnable onNo) {
        super(Component.literal(title));
        this.title = title;
        this.onYes = onYes;
        this.onNo = onNo;

        font = Minecraft.getInstance().font;
        this.description = description;
    }

    @Override
    protected void init() {
        var title = new StringWidget(Component.literal(this.title).setStyle(Style.EMPTY.withColor(0xFFCC00)), font);
        title.setPosition(width / 2 - font.width(this.title) / 2, 15);
        MultiLineTextWidget desc = null;
        FrameWidget frame = null;
        if (description != null) {
            desc = new MultiLineTextWidget(Component.literal(description), font);
            desc.setPosition(width / 2 - 100, 40);
            desc.setWidth(200);
            desc.setMaxWidth(200);
            desc.setHeight(100);
            frame = new FrameWidget(208, 108).setPos(desc.getX() - 4, desc.getY() - 4);
        }
        var yes = Button.builder(Component.translatable("gui.yes"), (btn) -> onYes.run())
                .width(100)
                .pos(width / 2 + 5, 155)
                .build();
        var no = Button.builder(Component.translatable("gui.no"), (btn) -> onNo.run())
                .width(100)
                .pos(width / 2 - 105, 155)
                .build();
        addRenderableWidget(title);
        if (desc != null) {
            addRenderableWidget(frame);
            addRenderableWidget(desc);
        }
        addRenderableWidget(yes);
        addRenderableWidget(no);
    }
}
