package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.codespace.BlockCodePos;
import com.prikolz.justhelper.codespace.CodeSpace;
import com.prikolz.justhelper.commands.arguments.searching.FoundSignInfo;
import com.prikolz.justhelper.commands.arguments.searching.InfoPack;
import com.prikolz.justhelper.commands.arguments.searching.SignsSearchingArgumentType;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;

public class AdvancedFindCommand extends JustHelperCommand {

    public static HashMap<String, String> miniChars = miniChars();

    public static final int PAGE_SIZE = 12;

    public static String toMini(String str) {
        String result = str;
        for(String key : miniChars().keySet()) {
            result = result.replaceAll(key, miniChars.get(key));
        }
        return result;
    }

    public static InfoPack lastFound = new InfoPack(List.of());
    public static String lastPrompt = ":3";

    public AdvancedFindCommand() {
        super("afind");
        this.description = """
                [Параметры поиска] <gray>- Поиск блоков кода по содержанию табличек.\
                Отображает все совпадения в чате.
                Пример использования: <white>/afind событие<gray>
                Используйте в начале <white>=[</white>, чтобы включить расширенный поиск.
                Например: <white>/afind =[floor=5]функция</white> - команда \
                будет искать все функции расположенные на 5 этаже. \
                Чтобы инвертировать параметр добавьте <white>!</white> перед операндом.
                Например: <white>/afind =[text1!="событие", floor!\\<10]</white> - команда будет искать все \
                блоки кода, которые НЕ имеют текст "событие" на первой строчке и находятся НЕ ниже 10 этажа.
                <white>Параметры поиска:<gray>
                - floor, line, distance - Числовые фильтры для поиска. \
                Поддерживают операнды: =, <, >. Принимают одно или несколько чисел. floor - условие на проверку этажа, \
                line - условие на проверку строчки кода, distance - условие на проверку расстояния до блока кода.
                Например:
                  <white>/afind =[floor=[10, 11, 5]]</white> - условие, этаж должен быть 10, 11 или 5.
                  <white>/afind =[line>10]</white> - условие, строчка должна быть больше 10
                  <white>/afind =[distance<40.5]</white> - условие, расстояние до блока должно быть меньше 40.5
                - text1, text2, text3, text4 - Текстовые фильтры для поиска. \
                Поддерживают операнды: =, <, >. Проверяет текст на указанной линии таблички. = - полное соответствие, \
                < - указанный текст содержится на линии таблички, > - текст линии таблички содержится в указанном тексте.
                Например:
                  <white>/afind =[text1=событие игрока]</white> - ищет события игрока.
                  <white>/afind =[text1<"событие"]</white> - ищет любое событие.
                  <white>/afind =[text1>событие игрока функция процесс]</white> - ищет любое событие игрока, функцию или процесс.
                  <white>/afind =[text1<событие, text1!<"игрока"]</white> - ищет любое событие, кроме событий игрока.
                - nearby - Сортировка по расстоянию. По умолчанию уже включена, поэтому не обязательно указывать, \
                но можно инвертировать, тогда в начале будут самые дальние блоки кода.
                Например:
                  <white>/afind =[text1="функция", nearby!]</white> - ищет сначала самые дальние функции.
                """;
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                Commands.argument(
                        "text", new SignsSearchingArgumentType()
                ).executes((context -> {
                    var found = SignsSearchingArgumentType.getFound(context, "text");
                    execute(found, 0);
                    return 1;
                })
                )
        );
    }

    public static void execute(InfoPack found, int page) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        lastFound = found;
        lastPrompt = SignsSearchingArgumentType.lastInput;
        var unpack = found.pack();

        if (unpack.isEmpty()) {
            JustHelperCommand.feedback(
                    "\n<hover:show_text:'Параметры поиска:<yellow>\n{0}'><click:suggest_command:'{1}'><dark_gray> ♯ Ничего не найдено ♯",
                    SignsSearchingArgumentType.lastInput,
                    "/afind " + SignsSearchingArgumentType.lastInput,
                    found.pack().size()
            );
            return;
        }

        JustHelperCommand.feedback(
                "\n<hover:show_text:'Параметры поиска:<yellow>\n{0}'><click:suggest_command:'{1}'><yellow> ♯<white> Найдено {2} совпадений:\n",
                SignsSearchingArgumentType.lastInput,
                "/afind " + SignsSearchingArgumentType.lastInput,
                found.pack().size()
        );

        var startIndex = page * PAGE_SIZE;

        var hasNextPage = true;
        for (int i = startIndex; i < startIndex + PAGE_SIZE; i++) {
            if (i >= unpack.size()) {
                hasNextPage = false;
                break;
            }
            var info = unpack.get(i);
            JustHelperCommand.feedback( createSignMessage(info) );
        }

        var controllerBuilder = new StringBuilder();
        controllerBuilder.append("<gray><strikethrough:true>    <strikethrough:false> ");
        if (page <= 0) {
            controllerBuilder.append("<dark_gray>←");
        } else {
            controllerBuilder.append("<click:run_command:'/foundlist ").append(page - 1).append("'><hover:show_text:←><yellow>←");
        }
        controllerBuilder.append("<reset> <white>").append(page + 1).append("<gold>/<white>").append(unpack.size() / PAGE_SIZE + 1);
        if (hasNextPage) {
            controllerBuilder.append(" <click:run_command:'/foundlist ").append(page + 1).append("'><hover:show_text:→><yellow>→");
        } else {
            controllerBuilder.append(" <dark_gray>→");
        }
        controllerBuilder.append("<reset> <gray><strikethrough:true>    <strikethrough:false>");
        JustHelperCommand.feedback(Component.empty());
        JustHelperCommand.feedback(controllerBuilder.toString());
    }

    public static Component createSignMessage(FoundSignInfo info) {
        var sign = info.sign();
        var pos = sign.codePos;
        var lastPrompt = SignsSearchingArgumentType.lastInput;
        String miniLine = toMini("<white>" + pos.line );
        if (pos.line < 10) miniLine = miniLine + " ";
        String clickCommand = "/tp " + (0.5 + pos.blockPos.getX()) + " " + pos.blockPos.getY() + " " + (2.5 + pos.blockPos.getZ());
        String signMainLine = "<gold>● <white>" + info.lines()[0].replace(lastPrompt, "<yellow>" + lastPrompt + "<white>");
        if (info.mainLine() != 0) {
            signMainLine = "<gold>● <gray>" + info.lines()[0] + "<gold>/<white>" + info.lines()[info.mainLine()].replace(lastPrompt, "<yellow>" + lastPrompt + "<white>");
        }
        String hoverText = info.createHoverInfo(lastPrompt);
        String floor = "" + pos.floor;
        var describe = CodeSpace.getFloorDescribe(pos.floor, true);
        if (describe != null) floor = "(" + describe.minimessage + "<yellow>)";
        var result = TextUtils.minimessage(
                " {5} <click:run_command:'{3}'><hover:show_text:'{4}'><yellow>{0}{1} {2}",
                floor,
                miniLine,
                signMainLine,
                clickCommand,
                hoverText,
                sign.getMiniBlockSprite()
        );
        return Component.literal(" ").append(result);
    }

    public static void findEach(BlockCodePos target) {
        if ( !Config.get().findEach.value ) return;
        int i = 0;
        for (var entry : lastFound.pack()) {
            if ( entry.sign().codePos.equals(target) ) {
                JustHelperCommand.feedback("┌");
                JustHelperCommand.feedback("│ {0}<gray>/<white>{1}", entry.lines()[0], entry.lines()[1]);
                String prev = getInLastFound(i - 1, "←");
                String next = getInLastFound(i + 1, "→");
                JustHelperCommand.feedback(
                        "└{0} <white>{1}<reset><gray>/<white>{2} {3}",
                        prev,
                        i + 1,
                        lastFound.pack().size(),
                        next
                );
                return;
            }
            i++;
        }
    }

    private static String getInLastFound(int i, String str) {
        if (i >= 0 && i < lastFound.pack().size()) {
            var previous = lastFound.pack().get(i);
            var hover = previous.createHoverInfo(lastPrompt);
            var pos = previous.sign().pos;
            return "<click:run_command:'/tp "
                    + pos.getX() + " "
                    + pos.getY() + " "
                    + pos.getZ()
                    + "'><hover:show_text:'" + hover + "'><yellow>" + str;
        }
        return "<dark_gray>" + str;
    }

    private static HashMap<String, String> miniChars() {
        HashMap<String, String> result = new HashMap<>();

        result.put("0", "₀");
        result.put("1", "₁");
        result.put("2", "₂");
        result.put("3", "₃");
        result.put("4", "₄");
        result.put("5", "₅");
        result.put("6", "₆");
        result.put("7", "₇");
        result.put("8", "₈");
        result.put("9", "₉");
        result.put("-", "₋");
        result.put("\\(", "₍");
        result.put("\\)", "₎");

        //result.put("a", "ᴀ");
        //result.put("b", "ʙ");
        //result.put("c", "ᴄ");
        //result.put("d", "ᴅ");
        //result.put("e", "ᴇ");
        //result.put("f", "ꜰ");
        //result.put("g", "ɢ");
        //result.put("h", "ʜ");
        //result.put("i", "ɪ");
        //result.put("j", "ᴊ");
        //result.put("k", "ᴋ");
        //result.put("l", "ʟ");
        //result.put("m", "ᴍ");
        //result.put("n", "ɴ");
        //result.put("o", "ᴏ");
        //result.put("p", "ᴘ");
        //result.put("q", "ꞯ");
        //result.put("r", "ʀ");
        //result.put("s", "ꜱ");
        //result.put("t", "ᴛ");
        //result.put("u", "ᴜ");
        //result.put("v", "ᴠ");
        //result.put("w", "ᴡ");
        //result.put("x", "x");
        //result.put("y", "ʏ");
        //result.put("z", "ᴢ");

        return result;
    }
}
