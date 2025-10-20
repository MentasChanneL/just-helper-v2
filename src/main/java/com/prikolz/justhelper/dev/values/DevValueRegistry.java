package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.NBTUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

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

    public static <T extends DevValue> DevValueRegistry<T> create(
            String type,
            ValueResolver<T> valueResolver,
            NBTResolver<T> nbtResolver
    ) {
        return new DevValueRegistry<>(type, nbtResolver, valueResolver);
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
        var valueTag = fullPath ? NBTUtils.get(nbt, "creative_plus.value") : nbt;
        if (!(valueTag instanceof CompoundTag value)) return null;
        String type = value.getString("type").orElse(null);
        if (type == null) return null;
        var resolver = DevValueRegistry.getRegistry(type);
        if (resolver == null) return null;
        try {
            var result = (T) resolver.valueResolver.resolve(new TagReader(value));
            result.registry = (DevValueRegistry<DevValue>) resolver;
            result.unusedFields = value;
            return result;
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("Can't resolve from NBT for '{}' data type: {} | NBT: {}", type, t.getMessage(), value);
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

    public static CompoundTag toNBT(DevValue value) {
        var result = new CompoundTag();
        result.put("type", StringTag.valueOf(value.type));
        value.registry.nbtResolver.resolve(value, result);
        return result;
    }

    public static void register(DevValueRegistry<?> registry) {
        registries.put(registry.type, registry);
    }

    public static void registerAll() {
        register( Variable.registry );
        register( Text.registry );
        register( Number.registry );
        register( Location.registry );
        register( Dictionary.registry );
        register( Array.registry );
        register( GameValue.registry );
        register( Parameter.registry );
        register( Vector.registry );
        register( Potion.registry );
        register( Sound.registry );
        register( Particle.registry );
        register( Item.registry );
    }

    public interface NBTResolver<T> {
        void resolve(T value, CompoundTag nbt);
    }

    public interface ValueResolver<T> {
        T resolve(TagReader nbt);
    }

    public record TagReader(CompoundTag tag) {

        private <T> T send(T value, String member) {
            tag.remove(member);
            return value;
        }

        public Tag get(String member) {
            var result = tag.get(member);
            return send(result, member);
        }
        public Optional<String> getString(String member) {
            var result = tag.getString(member);
            return send(result, member);
        }
        public Optional<Integer> getInt(String member) {
            var result = tag.getInt(member);
            return send(result, member);
        }
        public Optional<Double> getDouble(String member) {
            var result = tag.getDouble(member);
            return send(result, member);
        }
        public Optional<CompoundTag> getCompound(String member) {
            var result = tag.getCompound(member);
            return send(result, member);
        }
        public Optional<ListTag> getList(String member) {
            var result = tag.getList(member);
            return send(result, member);
        }
    }
}
