package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.dev.BlockCodePos;
import com.prikolz.justhelper.dev.SignInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class PosCommand extends JustHelperCommand {
    final boolean mustSave;

    static Vec3 pos = null;

    public PosCommand(String id, boolean mustSave) {
        super(id);
        this.mustSave = mustSave;
        if (mustSave)
            this.description = "<gray>- Сохраняет вашу позицию, на которую можно будет вернуться.";
        else
            this.description = "<gray>- Телепортирует вас на сохраненную позицию.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return 0;
            if (mustSave) {
                pos = player.position();
                var pos = new BlockCodePos(4, player.getBlockY(), player.getBlockZ());
                var signInfo = SignInfo.getSign(pos);
                if (signInfo == null) return JustHelperCommand.feedback(
                        "<aqua>⧈<white> Точка возврата задана "
                );
                var lines = signInfo.getLines();
                return JustHelperCommand.feedback(
                        "<aqua>⧈<white> Точка возврата задана <aqua>⊻ <reset>{0} <gray>{2}<reset><aqua> ⊻",
                        signInfo.getMiniBlockSprite(), lines[0], lines[1]
                );
            }
            if (pos == null) return JustHelperCommand.feedback(
                    "<yellow>⧈ Точка возврата не задана"
            );
            CommandBuffer.add("tp " + pos.x + " " + pos.y + " " + pos.z);
            return 1;
        });
    }
}
