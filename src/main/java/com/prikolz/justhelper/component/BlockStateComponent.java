package com.prikolz.justhelper.component;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.mixin.SpriteContentsMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public abstract class BlockStateComponent {

    private static final char[] LINES = new char[]{ '0', '1', '2', '3', '4', '5', '6', '7' };

    private static final HashMap<Block, Component> processed = new HashMap<>();
    private static final FontDescription FONT = new FontDescription.Resource(Identifier.parse("just-helper:pixel"));

    public static Component create(BlockState block) {
        var bl = block.getBlock();
        var result = (MutableComponent) processed.get( bl );
        if (result != null) return addStyle(result, bl);
        result = Component.empty();
        var render = Minecraft.getInstance().getBlockRenderer();
        try {
            var sprite = (SpriteContentsMixin) render.getBlockModel(block).particleIcon().contents();
            var image = sprite.getOriginalImage();
            int w = image.getWidth();
            int h = image.getHeight();

            int[][] resizedImage = new int[LINES.length][LINES.length];
            int[][] resizedLines = new int[h][LINES.length];

            for (int y = 0; y < h; y++) {
                var imageLine = new int[w];

                for (int x = 0; x < w; x++) {
                    imageLine[x] = image.getPixel(x, y);
                }

                var resizedLine = resize(imageLine, LINES.length);
                resizedLines[y] = resizedLine;
            }

            for (int x = 0; x < LINES.length; x++) {
                var imageColumn = new int[h];

                for (int y = 0; y < h; y++) {
                    imageColumn[y] = resizedLines[y][x];
                }

                var resizeColumn = resize(imageColumn, LINES.length);

                for (int y = 0; y < LINES.length; y++) {
                    resizedImage[x][y] = resizeColumn[y];
                }
            }

            for (int y = 0; y < LINES.length; y++) {
                for (int x : resizedImage[y]) {
                    result = result.append(entry(y, x));
                }
                result = result.append(newLine());
            }
            processed.put(bl, result);
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("Impossible create block component for {}", block);
        }
        result.append(Component.literal("-").setStyle(Style.EMPTY.withFont(FONT)));
        return addStyle(result, bl);
    }

    private static Component addStyle(Component blockComponent, Block bl) {
        return Component.empty().setStyle(
                Style.EMPTY.withHoverEvent(new HoverEvent.ShowText( Config.get().codeBlockNames.value.getName(bl) ))
        ).append(blockComponent);
    }

    private static Component entry(int line, int color) {
        return Component.literal(LINES[line] + ".").setStyle(
                Style.EMPTY.withColor(argbToRgb(color)).withFont(FONT)
        );
    }

    private static Component newLine() {
        return Component.literal("+").setStyle(Style.EMPTY.withFont(FONT));
    }

    private static int argbToRgb(int argb) {
        return argb & 0xFFFFFF; // Просто обрезаем альфа-канал
    }

    public static int[] resize(int[] input, int newSize) {
        if (input == null || input.length == 0) {
            return new int[0];
        }

        if (input.length == newSize) {
            return input.clone();
        }

        int[] output = new int[newSize];

        for (int i = 0; i < newSize; i++) {
            double position = (double) i / (newSize - 1) * (input.length - 1);

            output[i] = interpolateColor(input, position);
        }

        return output;
    }

    private static int interpolateColor(int[] colors, double position) {
        int index = (int) position;
        double fraction = position - index;

        if (fraction == 0.0 || index >= colors.length - 1) {
            return colors[Math.min(index, colors.length - 1)];
        }

        int color1 = colors[index];
        int color2 = colors[index + 1];

        return interpolateARGB(color1, color2, fraction);
    }

    private static int interpolateARGB(int color1, int color2, double fraction) {

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * fraction);
        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
