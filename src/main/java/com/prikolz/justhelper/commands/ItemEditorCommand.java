package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.commands.arguments.ColorArgumentType;
import com.prikolz.justhelper.commands.arguments.GreedyArgumentType;
import com.prikolz.justhelper.commands.arguments.ReferenceArgumentType;
import com.prikolz.justhelper.commands.arguments.ValidStringArgumentType;
import com.prikolz.justhelper.util.MojangUtils;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;

import java.util.*;

public class ItemEditorCommand extends JustHelperCommand {
    private static final String TAG_NAMESPACE = "justcreativeplus:";
    private static final HashMap<String, String> tagsClipboard = new HashMap<>();
    private static final ReferenceArgumentType<String> tagArgumentResolver = new ReferenceArgumentType<>(() -> {
        var player = Minecraft.getInstance().player;
        if (player == null) return Map.of();
        var item = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (item.isEmpty()) return Map.of();
        var result = new HashMap<String, String>();
        var tags = getBukkitTags(item);
        for (String keyRaw : tags.keySet()) {
            if (!keyRaw.startsWith(TAG_NAMESPACE)) continue;
            var tag = keyRaw.substring(TAG_NAMESPACE.length());
            result.put(tag, tag);
        }
        return result;
    });

    public ItemEditorCommand() {
        super("item+");
        this.description = "<gray>- Редактирование предмета(только в креативе), расширение возможностей /item(от Star).";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then( tagBranch() )
                .then( modifierBranch() )
                .then( profileBranch() )
                .then( JustHelperCommands.literal("display").executes(context -> itemResolver(item -> {
                    Minecraft.getInstance().schedule(() ->
                            Minecraft.getInstance().setScreen( new ItemDisplayEditorScreen(item) )
                    );
                    return 0;
                })))
                .then( equipmentBranch() )
                .then( colorBranch() );
    }

    private LiteralArgumentBuilder<ClientSuggestionProvider> colorBranch() {
        return new LineCommand("color")
                .run(context -> itemResolver(item -> {
                    var color = item.get(DataComponents.DYED_COLOR);
                    if (color == null) return JustHelperCommand.feedback("<yellow>Компонент цвета не установлен");
                    var rgb = color.rgb();
                    var hex = String.format("%06x", rgb);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    String rgbStr = r + " " + g + " " + b;
                    return JustHelperCommand.feedback(
                            "Установленный цвет предмета:\n<reset>♯ HEX: <underlined><#{1}>{0}<reset>\n☰ RGB: <underlined><#{1}> {2}",
                            TextUtils.copyValue('#' + hex),
                            hex,
                            TextUtils.copyValue(rgbStr)
                    );
                }))
                .arg("color", new ColorArgumentType())
                .run(context -> itemResolver(item -> {
                    var color = IntegerArgumentType.getInteger(context, "color");
                    item.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
                    var hex = String.format("%06x", color);
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    String rgbStr = r + " " + g + " " + b;
                    return JustHelperCommand.feedback(
                            "<green>Новый цвет предмета:\n<reset>♯ HEX: <underlined><#{1}>{0}<reset>\n☰ RGB: <underlined><#{1}> {2}",
                            TextUtils.copyValue('#' + hex),
                            hex,
                            TextUtils.copyValue(rgbStr)
                    );
                }))
                .build();
    }

    private LiteralArgumentBuilder<ClientSuggestionProvider> equipmentBranch() {

        var buildContext = MojangUtils.createBuildContext();

        var slotBranch = new LineCommand("slot")
                .arg("slot", ReferenceArgumentType.ofEnums(true, EquipmentSlot.values()))
                .run(context -> itemResolver(item -> {
                    var slot = ReferenceArgumentType.<EquipmentSlot>getReference(context, "slot");
                    var data = equippableCopy(item, slot);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Слот для экипировки: <white>{0}", slot);
                })).build();

        var overlayBranch = new LineCommand("overlay")
                .arg("overlay", IdentifierArgument.id())
                .run(context -> itemResolver(item -> {
                    var overlay = MojangUtils.getId(context, "overlay");
                    var data = equippableCopy(item, null).setCameraOverlay(overlay);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Оверлей камеры: <white>{0}", overlay);
                })).build();

        var entitiesBranch = new LineCommand("entities")
                .arg("entities", new GreedyArgumentType<>(ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE), " "))
                .run(context -> itemResolver(item -> {
                    var entities = GreedyArgumentType.<Holder.Reference<EntityType<?>>>getArgument(context, "entities");
                    var data = equippableCopy(item, null).setAllowedEntities( HolderSet.direct(entities) );
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Разрешенные сущности: <white>{0}", entities);
                })).build();

        var dispensableBranch = new LineCommand("dispensable")
                .arg("dispensable", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "dispensable");
                    var data = equippableCopy(item, null).setDispensable(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>'Необязательность': <white>{0}", value);
                })).build();

        var swappableBranch = new LineCommand("swappable")
                .arg("swappable", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "swappable");
                    var data = equippableCopy(item, null).setSwappable(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Свап брони: <white>{0}", value);
                })).build();

        var damageOnHurtBranch = new LineCommand("damageOnHurt")
                .arg("damageOnHurt", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "damageOnHurt");
                    var data = equippableCopy(item, null).setDamageOnHurt(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Разрушение от урона: <white>{0}", value);
                })).build();

        var equipOnInteract = new LineCommand("equipOnInteract")
                .arg("equipOnInteract", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "equipOnInteract");
                    var data = equippableCopy(item, null).setEquipOnInteract(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Экипировка от взаимодействия: <white>{0}", value);
                })).build();

        var equipSoundBranch = new LineCommand("equipSound")
                .arg("sound", ResourceArgument.resource(buildContext, Registries.SOUND_EVENT))
                .run(context -> itemResolver(item -> {
                    var value = MojangUtils.getResource(context, "sound", Registries.SOUND_EVENT);
                    var data = equippableCopy(item, null).setEquipSound(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Звук экипировки: <white>{0}", value.key().identifier());
                })).build();

        var gliderBranch = new LineCommand("glider")
                .arg("glider", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "glider");
                    if (value) item.set(DataComponents.GLIDER, Unit.INSTANCE); else item.set(DataComponents.GLIDER, null);
                    return JustHelperCommand.feedback(1, "<green>Режим планирования: <white>{0}", value);
                })).build();

        var canShearingBranch = new LineCommand("canBeSheared")
                .arg("canBeSheared", BoolArgumentType.bool())
                .run(context -> itemResolver(item -> {
                    var value = BoolArgumentType.getBool(context, "canBeSheared");
                    var data = equippableCopy(item, null).setCanBeSheared(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Можно срезать: <white>{0}", value);
                })).build();

        var shearingSoundBranch = new LineCommand("shearingSound")
                .arg("sound", ResourceArgument.resource(buildContext, Registries.SOUND_EVENT))
                .run(context -> itemResolver(item -> {
                    var value = MojangUtils.getResource(context, "sound", Registries.SOUND_EVENT);
                    var data = equippableCopy(item, null).setShearingSound(value);
                    item.set(DataComponents.EQUIPPABLE, data.build());
                    return JustHelperCommand.feedback(1, "<green>Звук срезания брони: <white>{0}", value.key().identifier());
                })).build();

        var removeBranch = new LineCommand("remove")
                .add(JustHelperCommands.literal("confirm"))
                .run(context -> itemResolver(item -> {
                    item.set(DataComponents.EQUIPPABLE, null);
                    return JustHelperCommand.feedback(1, "<yellow>Компонент брони был удален");
                })).build();

        return JustHelperCommands.literal("equipment")
                .executes(context -> itemResolver(item -> {
                    var data = item.get(DataComponents.EQUIPPABLE);
                    if (data == null) return JustHelperCommand.feedback("<yellow>Компонент экипировки не установлен");
                    return JustHelperCommand.feedback(
                            """
                            <green>Заданные параметры <white>экипировки<green>:
                            <white>- Слот: <green>{0}
                            <white>- Звук экипировки: <green>{1}
                            <white>- Ассет: <green>{2}
                            <white>- Оверлей: <green>{3}
                            <white>- Сущности: <green>{4}
                            <white>- Необязательный: <green>{5}
                            <white>- Свап: <green>{6}
                            <white>- Повреждения от урона: <green>{7}
                            <white>- Экипировка от взаимодействия: <green>{8}
                            <white>- Можно срезать: <green>{9}
                            <white>- Звук срезки: <green>{10}
                            
                            <white>- (Доп.) Глайдер: <green>{11}
                            """,
                            data.slot(),
                            data.equipSound(),
                            data.assetId().orElse(null),
                            data.cameraOverlay(),
                            data.allowedEntities().orElse(null),
                            data.dispensable(),
                            data.swappable(),
                            data.damageOnHurt(),
                            data.equipOnInteract(),
                            data.canBeSheared(),
                            data.shearingSound(),
                            item.get(DataComponents.GLIDER) != null
                    );
                }))
                .then(slotBranch).then(overlayBranch).then(entitiesBranch).then(dispensableBranch)
                .then(swappableBranch).then(damageOnHurtBranch).then(equipSoundBranch)
                .then(shearingSoundBranch).then(gliderBranch).then(removeBranch)
                .then(canShearingBranch).then(equipOnInteract);
    }

    private Equippable.Builder equippableCopy(ItemStack item, EquipmentSlot hotSlot) {
        var prev = item.get(DataComponents.EQUIPPABLE);
        var result = Equippable.builder(hotSlot == null && prev == null ? EquipmentSlot.HEAD : hotSlot == null ? prev.slot() : hotSlot);
        if (prev == null) return result;
        result.setEquipSound(prev.equipSound());
        prev.assetId().ifPresent(result::setAsset);
        prev.cameraOverlay().ifPresent(result::setCameraOverlay);
        prev.allowedEntities().ifPresent(result::setAllowedEntities);
        result.setDispensable(prev.dispensable());
        result.setSwappable(prev.swappable());
        result.setDamageOnHurt(prev.damageOnHurt());
        result.setShearingSound(prev.shearingSound());

        return result;
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
                .arg("key", tagArgumentResolver)
                .run(context -> itemResolver(item -> {
                    var key = ReferenceArgumentType.<String>getReference(context, "key");
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
                        if (shortValue.length() > 15) shortValue = shortValue.substring(0, 15) + "...";
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
                    for (String key : new HashSet<>(tags.keySet())) {
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
            player.swing(InteractionHand.MAIN_HAND, false);
            player.connection.send(
                    new ServerboundSetCreativeModeSlotPacket(36 + player.getInventory().getSelectedSlot(), item)
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

    public static class ItemDisplayEditorScreen extends Screen {

        private String nameField;
        private String loreField;
        private EditBox nameEditBox = null;
        private MultiLineEditBox loreEditBox = null;

        protected ItemDisplayEditorScreen(ItemStack item) {
            super(Component.literal("Редактирование отображения предмета"));
            var lore = item.get(DataComponents.LORE);

            var lines = lore == null ? List.<Component>of() : lore.lines();
            StringBuilder minimessage = new StringBuilder();
            for (Component line : lines) {
                minimessage.append( TextUtils.toMiniMessage(line) ).append('\n');
            }
            loreField = minimessage.toString();
            nameField = TextUtils.toMiniMessage( item.getOrDefault(DataComponents.CUSTOM_NAME, item.getItemName()) );
        }

        @Override
        protected void init() {
            if (loreEditBox != null) loreField = loreEditBox.getValue();
            if (nameEditBox != null) nameField = nameEditBox.getValue();

            var font = Minecraft.getInstance().font;

            var title = new StringWidget(this.title, font);
            title.setPosition( width / 2 - font.width(this.title) / 2, 10 );

            nameEditBox = new EditBox(font, 60, 30, Component.literal("Название"));
            nameEditBox.setX(60);
            nameEditBox.setY(30);
            nameEditBox.setWidth(width - 120);
            nameEditBox.setHeight(20);
            nameEditBox.setMaxLength(Integer.MAX_VALUE);
            nameEditBox.setValue(nameField);
            var nameTitle = new StringWidget(Component.literal("Название"), font);
            nameTitle.setPosition( nameEditBox.getX(), nameEditBox.getY() - 10 );

            loreEditBox = new MultiLineEditBox.Builder().setX(60).setY(nameEditBox.getY() + nameEditBox.getHeight() + 20)
                    .build(font, width - 120, height - 120, Component.literal("Описание"));
            loreEditBox.setValue(loreField);
            var loreTitle = new StringWidget(Component.literal("Описание"), font);
            loreTitle.setPosition( loreEditBox.getX(), loreEditBox.getY() - 10 );

            var ok = Button.builder(Component.literal("Применить"), button -> itemResolver(item -> {
                var list = new ArrayList<Component>();
                var content = loreEditBox.getValue();
                for (String line : content.split("\n")) list.add( TextUtils.minimessage(line) );
                item.set(DataComponents.LORE, new ItemLore(list));
                item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage(nameEditBox.getValue()) );
                Minecraft.getInstance().setScreen(null);
                return 1;
            })).pos(width / 2 + 5, loreEditBox.getHeight() + loreEditBox.getY() + 10).width(100).build();

            var cancel = Button.builder(Component.literal("Отмена"), button -> {
                Minecraft.getInstance().setScreen(null);
            }).pos(width / 2 - 105, loreEditBox.getHeight() + loreEditBox.getY() + 10).width(100).build();

            addRenderableWidget(title);
            addRenderableWidget(nameTitle);
            addRenderableWidget(nameEditBox);
            addRenderableWidget(loreTitle);
            addRenderableWidget(loreEditBox);
            addRenderableWidget(ok);
            addRenderableWidget(cancel);
        }
    }
}
