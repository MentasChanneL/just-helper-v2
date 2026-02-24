package com.prikolz.justhelper.mixin;

import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiLineEditBox.class)
public interface MultiLineEditBoxMixin {
    @Mutable
    @Accessor
    void setTextColor(int value);

    @Final
    @Accessor
    MultilineTextField getTextField();

    @Invoker("onDrag")
    void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e);
}
