package com.prikolz.justhelper.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

import static net.minecraft.commands.arguments.ResourceArgument.ERROR_INVALID_RESOURCE_TYPE;

public class MojangUtils {
    public static CommandBuildContext createBuildContext() {
        var lookup = Minecraft.getInstance().level.registryAccess().listRegistries();
        return CommandBuildContext.simple(HolderLookup.Provider.create(lookup), FeatureFlagSet.of());
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Holder.Reference<T> getResource(CommandContext<?> commandContext, String string, ResourceKey<Registry<T>> resourceKey) throws CommandSyntaxException {
        Holder.Reference<T> reference = (Holder.Reference) commandContext.getArgument(string, Holder.Reference.class);
        ResourceKey<?> resourceKey2 = reference.key();
        if (resourceKey2.isFor(resourceKey)) {
            return reference;
        } else {
            throw ERROR_INVALID_RESOURCE_TYPE.create(resourceKey2.identifier(), resourceKey2.registry(), resourceKey.identifier());
        }
    }

    public static Identifier getId(CommandContext<?> commandContext, String string) {
        return commandContext.getArgument(string, Identifier.class);
    }
}
