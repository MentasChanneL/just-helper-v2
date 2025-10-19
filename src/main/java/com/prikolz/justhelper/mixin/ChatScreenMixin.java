package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.commands.JustHelperCommands;
import com.prikolz.justhelper.gui.widgets.ChatCheckbox;
import com.prikolz.justhelper.util.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin<T extends ChatScreen> extends Screen {

    @Shadow
    protected EditBox input;

    @Unique
    private static boolean allowDoubleSpaces = false;
    @Unique
    private ChatCheckbox spacesCheckBox = null;

    protected ChatScreenMixin(Component component) { super(component); }

    @Inject(
            method = "init",
            at = @At(value = "TAIL")
    )
    private void init(CallbackInfo ci) {
        if (!Config.get().chatParameters.value.enableMarkers.value) return;
        spacesCheckBox = new ChatCheckbox(
                width - 15,
                height - 25,
                ComponentUtils.minimessage("<font:just-helper:icons>1"),
                allowDoubleSpaces,
                (w, v) -> allowDoubleSpaces = v
        );
        spacesCheckBox.setTooltip( Tooltip.create(Component.literal("Включить/Выключить\nдвойные пробелы в чате")) );
        this.addRenderableWidget(spacesCheckBox);
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V")
    )
    private void render(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int limit = 256;
        var value = input.getValue();
        if (spacesCheckBox != null && spacesCheckBox.isFocused()) Minecraft.getInstance().schedule(() -> this.setFocused(input));
        if ( JustHelperCommands.isJustHelperCommand(value) ) {
            guiGraphics.fill(x1, y1, x2, y2,0x3300AAFF);
            limit = Integer.MAX_VALUE;
        } else {
            guiGraphics.fill(x1, y1, x2, y2, color);
        }
        input.setMaxLength(limit);
        if (!Config.get().chatParameters.value.showLineLimit.value) return;
        guiGraphics.drawString(Minecraft.getInstance().font, value.length() + "/" + limit, x1 + 2, y1 - 10, 0xffAAAAAA);
    }

    @Redirect(
            method = "normalizeChatMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtil;trimChatMessage(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String normalizeCharMessage(String string) {
        var value = input.getValue();
        if (JustHelperCommands.isJustHelperCommand(value)) return string;
        return StringUtil.trimChatMessage(string);
    }

    @Redirect(
            method = "normalizeChatMessage",
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;normalizeSpace(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String normalizeSpace(String actualChar) {
        return allowDoubleSpaces ? actualChar : StringUtils.normalizeSpace(actualChar);
    }
}
