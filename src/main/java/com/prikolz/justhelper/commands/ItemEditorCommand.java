package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.arguments.ValidStringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;

public class ItemEditorCommand extends JustHelperCommand {

    private static final String TAG_NAMESPACE = "justcreativeplus:";
    private static final HashMap<String, String> tagsClipboard = new HashMap<>();

    public ItemEditorCommand() {
        super("item+");
        this.description = "<gray>- Редактирование предмета(только в креативе), бесплатный аналог /item(от Star).";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(tagBranch());
    }

    private LiteralArgumentBuilder<ClientSuggestionProvider> tagBranch() {

        var add = new SimpleCommand("add")
                .arg("key", new ValidStringArgumentType())
                .arg("value", StringArgumentType.greedyString())
                .run(context -> itemResolver(item -> {
                    var key = StringArgumentType.getString(context, "key");
                    var value = StringArgumentType.getString(context, "value");
                    var tags = getBukkitTags(item);
                    tags.put(TAG_NAMESPACE + key, StringTag.valueOf(value));
                    setBukkitTags(tags, item);
                    return JustHelperCommand.feedback("<green>Добавлен тег <white>'{0}'", key);
                }))
                .build();

        var remove = new SimpleCommand("remove")
                .arg("key", new ValidStringArgumentType())
                .run(context -> itemResolver(item -> {
                    var key = StringArgumentType.getString(context, "key");
                    var tags = getBukkitTags(item);
                    if (!tags.contains(TAG_NAMESPACE + key)) return JustHelperCommand.feedback("<yellow>Тег <white>{0} <yellow>не найден!", key);
                    tags.remove(TAG_NAMESPACE + key);
                    setBukkitTags(tags, item);
                    return JustHelperCommand.feedback(1, "<green>Тег <white>{0}<green> удален!", key);
                }))
                .build();

        var list = new SimpleCommand("list")
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    JustHelperCommand.feedback("\n<yellow>ⓘ<white> Установленные теги предмета:\n");
                    for (String keyRaw : tags.keySet()) {
                        if (!keyRaw.startsWith(TAG_NAMESPACE)) continue;
                        var key = keyRaw.substring(TAG_NAMESPACE.length());
                        var value = tags.getString(keyRaw).orElse("?");
                        var shortValue = value;
                        if (shortValue.length() > 15) shortValue = shortValue.substring(0, 15) + "...";
                        JustHelperCommand.feedback(1,
                                " <yellow>● <white><click:copy_to_clipboard:'{0}'><hover:show_text:'Скопировать\n{0}'>{0}<reset> <yellow>= <click:copy_to_clipboard:'{2}'><hover:show_text:'Скопировать\n{2}'><white>{1}",
                                key,
                                shortValue,
                                value
                        );
                    }
                    return JustHelperCommand.feedback(1, " ");
                }))
                .build();

        var clear = new SimpleCommand("clear")
                .add(JustHelperCommands.literal("confirm"))
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    var count = 0;
                    for (String key : tags.keySet()) {
                        if (!key.startsWith(TAG_NAMESPACE)) continue;
                        tags.remove(key);
                        count++;
                    }
                    setBukkitTags(tags, item);
                    return JustHelperCommand.feedback(1,
                            "<green>Очищено {0} тегов",
                            count
                    );
                }))
                .build();

        var copy = new SimpleCommand("copy")
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    tagsClipboard.clear();
                    for (String keyRaw : tags.keySet()) {
                        if (!keyRaw.startsWith(TAG_NAMESPACE)) continue;
                        var key = keyRaw.substring(TAG_NAMESPACE.length());
                        var value = tags.getString(keyRaw).orElse("?");
                        tagsClipboard.put(key, value);
                    }
                    return JustHelperCommand.feedback(1,
                            "<green>Скопировано <white>{0}<green> тегов в буфер. Для установки в предмет используйте /{1} tag paste",
                            tagsClipboard.size(),
                            this.name
                    );
                }))
                .build();

        var paste = new SimpleCommand("paste")
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    for (String key : tagsClipboard.keySet())
                        tags.put(TAG_NAMESPACE + key, StringTag.valueOf( tagsClipboard.get(key) ));
                    setBukkitTags(tags, item);
                    return JustHelperCommand.feedback(1,
                            "<green>Установлено <white>{0}<green> тегов в предмет",
                            tagsClipboard.size()
                    );
                }))
                .build();

        return JustHelperCommands.literal("tag").then(add).then(remove).then(list).then(clear).then(copy).then(paste);
    }

    private static int itemResolver(ItemStackProvider provider) {
        var player = Minecraft.getInstance().player;

        if (player == null) return 0;
        var item = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (item.isEmpty()) return JustHelperCommand.feedback("<red>Item+ >> Для редактирования предмета вы должны держать его в ведущей руке.");
        var result = provider.provide(item);
        if (result > 0) player.connection.send(
                new ServerboundSetCreativeModeSlotPacket(player.getInventory().getSelectedSlot(), item)
        );

        return result;
    }

    private static CompoundTag getBukkitTags(ItemStack item) {
        var customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return new CompoundTag();
        try {
            var values = customData.copyTag().get("PublicBukkitValues");
            if (values == null) return new CompoundTag();
            return (CompoundTag) values;
        } catch (Throwable t) { return new CompoundTag(); }
    }

    private static void setBukkitTags(CompoundTag tags, ItemStack item) {
        var nbt = new CompoundTag();
        nbt.put("PublicBukkitValues", tags);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    public interface ItemStackProvider {
        int provide(ItemStack item);
    }
}
