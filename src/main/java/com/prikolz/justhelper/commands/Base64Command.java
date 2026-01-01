package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class Base64Command extends JustHelperCommand {

    public Base64Command() {
        super("base64");
        this.description = "encode/decode [Текст] <gray>- Кодирует/Декодирует текст в base64.";

    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {

        var encode = new LineCommand("encode")
                .arg("text", StringArgumentType.greedyString())
                .run(context -> {
                    var text = StringArgumentType.getString(context, "text");
                    var base64 = TextUtils.encodeBase64(text);
                    return JustHelperCommand.feedback(
                            "<green>Закодированный текст:<white>\n{0}",
                            TextUtils.copyValue(base64)
                    );
                })
                .build();

        var decode = new LineCommand("decode")
                .arg("base64", StringArgumentType.greedyString())
                .run(context -> {
                    var base64 = StringArgumentType.getString(context, "base64");
                    var text = TextUtils.decodeBase64(base64);
                    return JustHelperCommand.feedback(
                            "<green>Декодированный текст:<white>\n{0}",
                            TextUtils.copyValue(text)
                    );
                })
                .build();

        return main.then(encode).then(decode);
    }
}
