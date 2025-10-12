package com.prikolz.justhelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prikolz.justhelper.commands.JustHelperCommands;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class JustHelperClient implements ClientModInitializer {

	public static final String MOD_ID = "just-helper";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Config CONFIG = null;

	public static User user;

	@Override
	public void onInitializeClient() {
		JustHelperCommands.initialize();
		CONFIG = new Config();
		CONFIG.read();
		CommandBuffer.runTimer();
		LOGGER.info("hello");
		user = new User(
				"2M3V",
				UUID.randomUUID(),
				"FabricMC",
				Optional.empty(),
				Optional.empty(),
				User.Type.LEGACY
		);

	}
}