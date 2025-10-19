package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.JustHelperClient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class DevValueRegistry<T extends DevValue> {

    private static final HashMap<String, DevValueRegistry<? extends DevValue>> registries = new HashMap<>();

    public final String type;
    public final NBTResolver<T> nbtResolver;
    public final ValueResolver<T> valueResolver;

    private DevValueRegistry(String type, NBTResolver<T> nbtResolver, ValueResolver<T> valueResolver) {
        this.type = type;
        this.nbtResolver = nbtResolver;
        this.valueResolver = valueResolver;
    }

    public static <T extends DevValue> DevValueRegistry<T> register(
            String type,
            ValueResolver<T> valueResolver,
            NBTResolver<T> nbtResolver
    ) {
        var result = new DevValueRegistry<>(type, nbtResolver, valueResolver);
        registries.put(type, result);
        return result;
    }

    public static DevValueRegistry<? extends DevValue> getRegistry(String key) {
        return registries.get(key);
    }

    public static Collection<DevValueRegistry<? extends DevValue>> values() {
        return registries.values();
    }

    /*
    public ItemStack toItem(T value) {
        var result = new ItemStack(value.material);
        var nbt = new CompoundTag();
        var creativePlus = new CompoundTag();
        creativePlus.put("type", StringTag.valueOf(value.type) );
        nbtResolver.resolve(value, creativePlus);
        nbt.put("creative_plus", creativePlus);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        var displays = value.buildItemDisplays();
        result.set(DataComponents.LORE, new ItemLore(displays.second));
        result.set(DataComponents.CUSTOM_NAME, displays.first);
        return result;
    }
    */

    @SuppressWarnings("unchecked")
    public static <T extends DevValue> T fromNBT(CompoundTag nbt, boolean fullPath) {
        if (nbt == null) return null;
        var creativePlusTag = fullPath ? nbt.get("creative_plus") : nbt;
        if (!(creativePlusTag instanceof CompoundTag creativePlus)) return null;
        String type = creativePlus.getString("type").orElse(null);
        if (type == null) return null;
        var resolver = DevValueRegistry.getRegistry(type);
        if (resolver == null) return null;
        try {
            return (T) resolver.valueResolver.resolve(creativePlus);
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("Can't resolve from NBT for '{}' data type: {} | NBT: {}", type, t.getMessage(), creativePlus);
            JustHelperClient.LOGGER.printStackTrace(t, JustHelperClient.JustHelperLogger.LogType.WARN);
            return null;
        }
    }

    public static <T extends DevValue> T fromItem(ItemStack item) {
        if (item == null || item.isEmpty()) return null;
        var data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var nbt = data.copyTag();
        return fromNBT(nbt, true);
    }

    public interface NBTResolver<T> {
        void resolve(T value, CompoundTag nbt);
    }

    public interface ValueResolver<T> {
        T resolve(CompoundTag nbt);
    }
}
