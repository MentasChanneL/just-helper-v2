package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.arguments.ReferenceArgumentType;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Base64Command extends JustHelperCommand {

    public Base64Command() {
        super("base64");
        this.description = "encode/decode [Кодировка] [Текст] <gray>- Кодирует/Декодирует текст в base64. UTF-8 и UTF-16 символы таблицы Unicode(включая например русские буквы), а ASCII стандартные 256 символов, где есть только знаки препинания, цифры и английские буквы.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {

        var encode = new LineCommand("encode")
                .arg("charset", new ReferenceArgumentType<>(
                        List.of("UTF-8", "UTF-16", "ASCII"),
                        List.of(StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.US_ASCII)
                ))
                .arg("text", StringArgumentType.greedyString())
                .run(context -> {
                    var text = StringArgumentType.getString(context, "text");
                    var charset = (Charset) ReferenceArgumentType.getReference(context, "charset");
                    var base64 = TextUtils.encodeBase64(text, charset);
                    return JustHelperCommand.feedback(
                            "<green>Закодированный текст base64:<white>\n{0}",
                            TextUtils.copyValue(base64)
                    );
                })
                .build();

        var decode = new LineCommand("decode")
                .arg("charset", new ReferenceArgumentType<>(
                        List.of("UTF-8", "UTF-16", "ASCII"),
                        List.of(StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.US_ASCII)
                ))
                .arg("base64", StringArgumentType.greedyString())
                .run(context -> {
                    var base64 = StringArgumentType.getString(context, "base64");
                    var charset = (Charset) ReferenceArgumentType.getReference(context, "charset");
                    var text = TextUtils.decodeBase64(base64, charset);
                    return JustHelperCommand.feedback(
                            "<green>Декодированный текст base64:<white>\n{0}",
                            TextUtils.copyValue(text)
                    );
                })
                .build();

        return main.then(encode).then(decode);
    }
}
