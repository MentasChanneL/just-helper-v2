package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.commands.arguments.ReferenceArgumentType;
import com.prikolz.justhelper.commands.arguments.ValidStringArgumentType;
import com.prikolz.justhelper.util.TextUtils;
import com.prikolz.justhelper.util.MojangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemEditorCommand extends JustHelperCommand {

    private static final String TAG_NAMESPACE = "justcreativeplus:";
    private static final HashMap<String, String> tagsClipboard = new HashMap<>();

    public ItemEditorCommand() {
        super("item+");
        this.description = "<gray>- Редактирование предмета(только в креативе), расширение возможностей /item(от Star).";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then( tagBranch() ).then( modifierBranch() ).then( profileBranch() );
    }

    private LiteralArgumentBuilder<ClientSuggestionProvider> profileBranch() {
        return new LineCommand("profile")
                .run(context -> itemResolver(item -> {
                    var profile = item.get(DataComponents.PROFILE);
                    if (profile == null) return JustHelperCommand.feedback("<yellow>Профиль предмета не задан!");
                    JustHelperCommand.feedback("<aqua>ⓘ<white> Профиль предмета:");
                    JustHelperCommand.feedback("");
                    profile.partialProfile().properties().forEach((k, v) -> JustHelperCommand.feedback(
                            " • <aqua>{0}<white> = <aqua>{1}",
                            TextUtils.copyValue(k),
                            TextUtils.copyValue(v == null ? "null" : v.value())
                    ));
                    return 0;
                }))
                .build();
    }


    private LiteralArgumentBuilder<ClientSuggestionProvider> modifierBranch() {

        var buildContext = MojangUtils.createBuildContext();

        var operationArg = new ReferenceArgumentType<>(
                List.of("add", "multiple", "percent"),
                List.of(
                        AttributeModifier.Operation.ADD_VALUE,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
        );

        var add = new LineCommand("add")
                .arg("attribute", ResourceArgument.resource(buildContext, Registries.ATTRIBUTE))
                .arg("name", IdentifierArgument.id())
                .arg("amount", DoubleArgumentType.doubleArg())
                .arg("operation", operationArg)
                .arg("slot", ReferenceArgumentType.ofEnums( true, EquipmentSlotGroup.values() ))
                .run(context -> itemResolver(item -> {
                    var attribute = MojangUtils.getResource(context, "attribute", Registries.ATTRIBUTE);
                    var name = MojangUtils.getId(context, "name");
                    var amount = DoubleArgumentType.getDouble(context, "amount");
                    var operation = ReferenceArgumentType.<AttributeModifier.Operation>getReference(context, "operation");
                    var slot = ReferenceArgumentType.<EquipmentSlotGroup>getReference(context, "slot");
                    var modifiers = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    modifiers = modifiers == null ? ItemAttributeModifiers.EMPTY : modifiers;
                    modifiers = modifiers.withModifierAdded(
                            attribute,
                            new AttributeModifier(name, amount, operation),
                            slot
                    );
                    item.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
                    return JustHelperCommand.feedback(1,
                            "<green>Добавлен атрибут <white><tr:'{0}'>/{1}",
                            attribute.value().getDescriptionId(),
                            TextUtils.copyValue(name)
                    );
                }))
                .build();

        var remove = new LineCommand("remove")
                .arg("attribute", ResourceArgument.resource(buildContext, Registries.ATTRIBUTE))
                .run(context -> itemResolver(item -> {
                    var attribute = MojangUtils.getResource(context, "attribute", Registries.ATTRIBUTE);
                    var modifiers = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    modifiers = modifiers == null ? ItemAttributeModifiers.EMPTY : modifiers;
                    int removed = 0;
                    var newModificators = ItemAttributeModifiers.EMPTY;
                    for (var entry : modifiers.modifiers()) {
                        var modifier = entry.modifier();
                        if ( !entry.attribute().is(attribute.key()) ) {
                            newModificators = newModificators.withModifierAdded(
                                    entry.attribute(), modifier, entry.slot()
                            );
                            continue;
                        }
                        removed++;
                    }
                    if (removed == 0) return JustHelperCommand.feedback(
                            "<yellow>Модификаторы атрибута <white><tr:'{0}'><yellow> не найдены!",
                            attribute.value().getDescriptionId()
                    );
                    item.set(DataComponents.ATTRIBUTE_MODIFIERS, newModificators);
                    return JustHelperCommand.feedback(1,
                            "<green>Удалено <white>{0}<green> модификаторов атрибута <white><tr:'{1}'>",
                            removed,
                            attribute.value().getDescriptionId()
                    );
                }))
                .arg("name", IdentifierArgument.id())
                .run(context -> itemResolver(item -> {
                    var attribute = MojangUtils.getResource(context, "attribute", Registries.ATTRIBUTE);
                    var name = MojangUtils.getId(context, "name");
                    var modifiers = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    modifiers = modifiers == null ? ItemAttributeModifiers.EMPTY : modifiers;
                    boolean removed = false;
                    var newModificators = ItemAttributeModifiers.EMPTY;
                    for (var entry : modifiers.modifiers()) {
                        var modifier = entry.modifier();
                        if ( !entry.attribute().is(attribute.key()) || !modifier.id().equals(name)) {
                            newModificators = newModificators.withModifierAdded(
                                    entry.attribute(), modifier, entry.slot()
                            );
                            continue;
                        }
                        removed = true;
                        break;
                    }
                    if (!removed) return JustHelperCommand.feedback(
                            "<yellow>Атрибут <white><tr:'{0}'>/{1}<yellow> не найден!",
                            attribute.value().getDescriptionId(), name
                    );
                    item.set(DataComponents.ATTRIBUTE_MODIFIERS, newModificators);
                    return JustHelperCommand.feedback(1,
                            "<green>Удален атрибут <white><tr:'{0}'>/{1}",
                            attribute.value().getDescriptionId(),
                            name
                    );
                }))
                .build();

        var list = new LineCommand("list")
                .run(context -> itemResolver(item -> {
                    var modifiers = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    if (modifiers == null) return JustHelperCommand.feedback("<yellow>Модификаторы не найдены!");
                    JustHelperCommand.feedback("<yellow>⏷ <white>Список модификаторов:");
                    final var messagesMap = new HashMap<Holder<Attribute>, List<Component>>();
                    modifiers.modifiers().forEach(entry -> {
                        var modifier = entry.modifier();
                        var messages = messagesMap.computeIfAbsent(entry.attribute(), k -> new ArrayList<>());
                        var message = TextUtils.minimessage(
                                "    - <yellow>{0} <white>{1} <gold>{2} {3}",
                                TextUtils.copyValue(modifier.id()),
                                modifier.amount(),
                                operationArg.getKeyOrDefault(modifier.operation(), "?"),
                                entry.slot().name().toLowerCase()
                        );
                        messages.add(message);
                    });
                    messagesMap.forEach((k, v) -> {
                        JustHelperCommand.feedback(
                                "  • <hover:show_text:'<tr:chat.copy> {1}'><click:copy_to_clipboard:'{1}'><aqua><tr:'{0}'>",
                                k.value().getDescriptionId(),
                                k.unwrapKey().orElseThrow().identifier()
                        );
                        v.forEach(JustHelperCommand::feedback);
                    });
                    return JustHelperCommand.feedback("<yellow>⏶");
                }))
                .build();

        return JustHelperCommands.literal("modifier").then(add).then(remove).then(list);
    }

    private LiteralArgumentBuilder<ClientSuggestionProvider> tagBranch() {

        var add = new LineCommand("add")
                .arg("key", new ValidStringArgumentType())
                .arg("value", StringArgumentType.greedyString())
                .run(context -> itemResolver(item -> {
                    var key = StringArgumentType.getString(context, "key");
                    var value = StringArgumentType.getString(context, "value");
                    var tags = getBukkitTags(item);
                    tags.put(TAG_NAMESPACE + key, StringTag.valueOf(value));
                    setBukkitTags(tags, item);
                    return JustHelperCommand.feedback(1, "<green>Добавлен тег <white>'{0}'", key);
                }))
                .build();

        var remove = new LineCommand("remove")
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

        var list = new LineCommand("list")
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    JustHelperCommand.feedback("\n<yellow>ⓘ<white> Установленные теги предмета:\n");
                    for (String keyRaw : tags.keySet()) {
                        if (!keyRaw.startsWith(TAG_NAMESPACE)) continue;
                        var key = keyRaw.substring(TAG_NAMESPACE.length());
                        var value = tags.getString(keyRaw).orElse("?");
                        var shortValue = value;
                        if (shortValue.length() > 15) shortValue = shortValue.substring(0, 25) + "...";
                        JustHelperCommand.feedback(1,
                                " <yellow>● <white>{0}<reset> <yellow>= <click:copy_to_clipboard:'{2}'><hover:show_text:'<tr:chat.copy>\n{2}'><white>{1}",
                                TextUtils.copyValue(key),
                                shortValue,
                                value
                        );
                    }
                    return JustHelperCommand.feedback(" ");
                }))
                .build();

        var clear = new LineCommand("clear")
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

        var copy = new LineCommand("copy")
                .run(context -> itemResolver(item -> {
                    var tags = getBukkitTags(item);
                    tagsClipboard.clear();
                    for (String keyRaw : tags.keySet()) {
                        if (!keyRaw.startsWith(TAG_NAMESPACE)) continue;
                        var key = keyRaw.substring(TAG_NAMESPACE.length());
                        var value = tags.getString(keyRaw).orElse("?");
                        tagsClipboard.put(key, value);
                    }
                    return JustHelperCommand.feedback(
                            "<green>Скопировано <white>{0}<green> тегов в буфер. Для установки в предмет используйте /{1} tag paste",
                            tagsClipboard.size(),
                            this.name
                    );
                }))
                .build();

        var paste = new LineCommand("paste")
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
        int result;
        try {
            result = provider.provide(item);
        } catch (Throwable t) {
            JustHelperClient.LOGGER.printStackTrace(t, JustHelperClient.JustHelperLogger.LogType.ERROR);
            return JustHelperCommand.feedback("<red>Item+ >> Ошибка выполнения: " + t.getMessage());
        }
        if (result > 0) {
            player.connection.send(
                    new ServerboundSetCreativeModeSlotPacket(player.getInventory().getSelectedSlot(), item)
            );
        }

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
        int provide(ItemStack item) throws CommandSyntaxException;
    }
}
