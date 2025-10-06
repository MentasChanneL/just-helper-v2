package com.prikolz.justhelper;

import com.prikolz.justhelper.commands.JustHelperCommands;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class JustHelperClient implements ClientModInitializer {

	public static final String MOD_ID = "just-helper";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static User user;

	@Override
	public void onInitializeClient() {
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