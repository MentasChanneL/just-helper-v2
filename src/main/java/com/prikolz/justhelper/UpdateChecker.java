package com.prikolz.justhelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prikolz.justhelper.util.ContextRunnable;
import com.prikolz.justhelper.util.JustHelperUtils;
import com.prikolz.justhelper.util.TextUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;

public class UpdateChecker {
    public static final String REPO = "MentasChanneL/just-helper-v2";
    public static final Map<String, String> DOWNLOAD_LINKS = Map.of(
            "GitHub", "https://github.com/MentasChanneL/just-helper-v2/releases/tag/{}",
            "Modrinth", "https://modrinth.com/mod/justhelper/version/{}"
    );
    private static final Gson gson = new Gson();
    private static ReleaseInfo cache = null;
    private static long cacheTimestamp = 0L;

    public static boolean requireCheck = false;

    public static void onJoinCheckMessage() {
        if (!requireCheck) return;
        requireCheck = false;
        if (!Config.get().updateChecker.value) return;
        check((info) -> {
            if (!info.isNew) return;
            JustHelperUtils.send(
                    "<green>[↓] <aqua>(JustHelper) <white>Доступно обновление: <green><underlined>{0}</underlined>",
                    "<hover:show_text:'Скачать/Посмотреть изменения'><click:run_command:'/justhelper updates'>{1}",
                    info.title
            );
        }, (reason) -> {});
    }

    public static void checkUpdates() {
        check((info) -> {
            if (!info.isNew) {
                JustHelperUtils.send(
                        " <green>✔ [JustHelper] <white>Мод имеет актуальную версию. Обновление не требуется."
                );
                return;
            }
            JustHelperUtils.send(
                    "\n<green>↓<white> Доступно обновление <green>JustHelper<white>:" +
                    "\n <green>♯ <white>Версия: <green>" + info.title +
                    "\n <green>↓ <white>Ссылки:\n{0}<reset>" +
                    "\n <green>✎ <white>Изменения: <gray>\n  " + TextUtils.markdownToMinimessage(info.description).replace("\n", "\n  "),
                    TextUtils.joinToString(DOWNLOAD_LINKS.keySet(), "\n", (key) -> {
                        var link = DOWNLOAD_LINKS.getOrDefault(key, "???").replace("{}", info.version);
                        return "<reset><aqua>    ⏵ <white>" + key + ": <underlined><aqua><click:open_url:'" + link + "'>" +
                                "<hover:show_text:'" + link + "'>[СКАЧАТЬ]";
                    })
            );
        }, (reason) -> JustHelperUtils.send(
                " <yellow>[JustHelper] Не удалось получить информацию о обновлениях. Сообщение: <white>{0}",
                reason
        ));
    }

    public static void check(ContextRunnable<ReleaseInfo> onResponse, ContextRunnable<String> onFail) {
        if (cache != null && cacheTimestamp > System.currentTimeMillis()) {
            JustHelperUtils.sync(() ->
                    onResponse.run(cache)
            );
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
                        .header("Accept", "application/vnd.github.v3+json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                var data = gson.fromJson(response.body(), JsonObject.class);
                String version = data.get("tag_name").getAsString();
                String title = data.get("name").getAsString();
                String description = data.get("body").getAsString();
                var date = LocalDate.parse(version, JustHelperClient.VERSION_DATE_FORMAT);
                JustHelperClient.LOGGER.info(
                        "Got latest release info response: version={} title={}",
                        version,
                        title
                );
                cache = new ReleaseInfo(date.isAfter(JustHelperClient.versionDate), version, title, description);
                cacheTimestamp = System.currentTimeMillis() + 5 * 60000;
                JustHelperUtils.sync(() ->
                        onResponse.run(cache)
                );
            } catch (Exception e) {
                JustHelperUtils.sync(() ->
                        onFail.run(e.getMessage())
                );
                JustHelperClient.LOGGER.warn("Check updates fail: {}", e.getMessage());
            }
        });
        thread.start();
    }

    public record ReleaseInfo(boolean isNew, String version, String title, String description) {}
}
