package com.prikolz.justhelper.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.commands.Commands;
import com.prikolz.justhelper.gui.widgets.ChatCheckbox;
import com.prikolz.justhelper.util.JustHelperUtils;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    @Unique
    private boolean chatPatchesIsLoaded = false;

    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @Inject(
            method = "init",
            at = @At(value = "TAIL")
    )
    private void init(CallbackInfo ci) {
        if (!Config.get().chatParameters.value.enableMarkers.value) return;
        spacesCheckBox = new ChatCheckbox(
                width - (JustHelperUtils.isClassLoaded("com.aizistral.nochatreports.common.NCRCore") ? 40 : 15),
                height - 30,
                TextUtils.minimessage("<font:just-helper:icons>1"),
                allowDoubleSpaces,
                (w, v) -> allowDoubleSpaces = v
        );
        spacesCheckBox.setTooltip(Tooltip.create(Component.literal("Включить/Выключить\nдвойные пробелы в чате")));
        this.addRenderableWidget(spacesCheckBox);
        chatPatchesIsLoaded = JustHelperUtils.isClassLoaded("obro1961.chatpatches.ChatPatches");
    }

    @Inject(
            method = "extractRenderState",
            at = @At("TAIL")
    )
    private void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int limit = 256;
        var value = input.getValue();
        if (spacesCheckBox != null && spacesCheckBox.isFocused()) {
            Minecraft.getInstance().schedule(() -> this.setFocused(input));
        }
        if (Commands.isJustHelperCommand(value)) {
            limit = Integer.MAX_VALUE;
        }
        input.setMaxLength(limit);
        if (!Config.get().chatParameters.value.showLineLimit.value) return;
        guiGraphics.text(
                Minecraft.getInstance().font,
                value.length() + "/" + limit,
                chatPatchesIsLoaded ? (int) (width * 0.32) : input.getX() + 2,
                input.getY() - 16,
                0xffAAAAAA
        );
    }

    @Redirect(
            method = "normalizeChatMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtil;trimChatMessage(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String normalizeCharMessage(String string) {
        var value = input.getValue();
        if (Commands.isJustHelperCommand(value)) return string;
        return StringUtil.trimChatMessage(string);
    }

    @Redirect(
            method = "normalizeChatMessage",
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;normalizeSpace(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String normalizeSpace(String actualChar) {
        return allowDoubleSpaces ? actualChar : StringUtils.normalizeSpace(actualChar);
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(I)I"))
    private int changeChatInputBackground(int color) {
        if(Commands.isJustHelperCommand(input.getValue())) {
            return 0xAA002255;
        } else {
            return color;
        }
    }
}
