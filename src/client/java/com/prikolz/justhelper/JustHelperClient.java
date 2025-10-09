package com.prikolz.justhelper;

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

	public static Config CONFIG = null;

	public static User user;

	@Override
	public void onInitializeClient() {
		JustHelperCommands.initialize();
		CONFIG = new Config();
		CONFIG.read();
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