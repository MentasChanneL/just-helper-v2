package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.dev.values.DevValue;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    public abstract void drawString(Font font, @Nullable Component text, int i, int j, int k, boolean bl);

    @Shadow
    public abstract void fill(int i, int j, int k, int l, int m);

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    public void renderItemDecorations(Font font, ItemStack itemStack, int i, int j, @Nullable String string, CallbackInfo ci) {
        if (itemStack.isEmpty() || !JustHelperClient.CONFIG.renderValueDecorations.value) return;
        var customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        var data = customData.copyTag().getCompound(DevValue.DECORATION_TEXT_KEY).orElse(null);
        if (data == null) return;
        var text = data.getStringOr("text", "null");
        var color = data.getIntOr("color", NamedTextColor.WHITE.value());
        fill(i - 4, j - 2, i - 3 + font.width(text), j + font.lineHeight, 0x88000000);
        drawString(font, Component.literal(text).withColor(color), i - 3, j - 1, -1, true);
    }
}
