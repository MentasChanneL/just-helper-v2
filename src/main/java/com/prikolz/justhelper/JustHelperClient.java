package com.prikolz.justhelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prikolz.justhelper.commands.JustHelperCommands;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JustHelperClient implements ClientModInitializer {

	public static final String MOD_ID = "just-helper";

	public static final JustHelperLogger LOGGER = new JustHelperLogger(LoggerFactory.getLogger(MOD_ID));

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static Config CONFIG = null;

	@Override
	public void onInitializeClient() {
		JustHelperCommands.initialize();
		CONFIG = new Config();
		CONFIG.read();
		CommandBuffer.runTimer();
		LOGGER.info("hello");
	}

    public static class JustHelperLogger extends OutputStream {

        public static final int CACHE_LIMIT = 50;

        public Logger logger;
        public StringBuilder outputBuffer = new StringBuilder();
        public List<String> cache = new ArrayList<>();

        public JustHelperLogger(Logger logger) {
            this.logger = logger;
        }

        public void info(String msg, Object ... placeholders) {
            log(LogType.INFO, msg, placeholders);
        }

        public void warn(String msg, Object ... placeholders) {
            log(LogType.WARN, msg, placeholders);
        }

        public void error(String msg, Object ... placeholders) {
            log(LogType.ERROR, msg, placeholders);
        }

        public void printStackTrace(Throwable throwable) { this.printStackTrace(throwable, LogType.ERROR); }

        public void printStackTrace(Throwable throwable, LogType type) {
            outputBuffer = new StringBuilder();
            var printStream = new PrintStream(this);
            throwable.printStackTrace(printStream);
            printStream.close();
            log(type, outputBuffer.toString());
        }

        public String unionCache() {
            StringBuilder result = new StringBuilder();
            for (String log : cache) result.append(log).append('\n');
            return result.toString();
        }

        private void log(LogType type, String msg, Object ... placeholders) {
            for (Object o : placeholders) msg = msg.replaceFirst("\\{}", o.toString());
            if (cache.size() > CACHE_LIMIT) cache.removeFirst();
            Date currentDate = new Date();
            SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]");
            String formattedTime = timeFormat.format(currentDate);
            cache.add(formattedTime + " " + type.prefix + " " + msg);
            switch (type) {
                case INFO -> logger.info(msg);
                case WARN -> logger.warn(msg);
                case ERROR -> logger.error(msg);
            }
        }

        @Override
        public void write(int b) throws IOException {
            outputBuffer.append((char) b);
        }

        public enum LogType {
            INFO("[INFO]"), WARN("§e[WARN]§r"), ERROR("§c[ERROR]§r");

            public final String prefix;

            LogType(String prefix) {
                this.prefix = prefix;
            }
        }
    }
}