package com.prikolz.justhelper.mixin;

import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiLineEditBox.class)
public interface MultiLineEditBoxMixin {
    @Mutable
    @Accessor
    void setTextColor(int value);

    @Final
    @Accessor
    MultilineTextField getTextField();
}
