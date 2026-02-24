package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.util.JustMCUtils;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class GzipCommand extends JustHelperCommand {

    public GzipCommand() {
        super("gzip");
        this.description = "encode/decode [Текст] <gray>- Сжимает/Распаковывает текст в gzip.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {

        var encode = new LineCommand("compress")
                .arg("text", StringArgumentType.greedyString())
                .run(context -> {
                    var text = StringArgumentType.getString(context, "text");
                    try {
                        var result = JustMCUtils.gzipCompress(text);
                        return JustHelperCommand.feedback(
                                "<green>Сжатый текст gzip:<white>\n{0}",
                                TextUtils.copyValue(result)
                        );
                    } catch (Throwable e) {
                        return JustHelperCommand.feedback("<red>Ошибка сжатия: {0}", e.getMessage());
                    }
                })
                .build();

        var decode = new LineCommand("decompress")
                .arg("gzip", StringArgumentType.greedyString())
                .run(context -> {
                    var gzip = StringArgumentType.getString(context, "gzip");
                    try {
                        var text = JustMCUtils.gzipDecompress(gzip);
                        return JustHelperCommand.feedback(
                                "<green>Распакованный текст gzip:<white>\n{0}",
                                TextUtils.copyValue(text)
                        );
                    } catch (Throwable t) {
                        return JustHelperCommand.feedback("<red>Ошибка распаковки: {0}", t.getMessage());
                    }
                })
                .build();

        return main.then(encode).then(decode);
    }
}
