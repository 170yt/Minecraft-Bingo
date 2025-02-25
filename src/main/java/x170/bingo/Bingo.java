package x170.bingo;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x170.bingo.command.BackpackCommand;
import x170.bingo.command.BingoCommand;
import x170.bingo.command.TopCommand;
import x170.bingo.event.*;
import x170.bingo.game.GameManager;
import x170.bingo.team.TeamManager;
import x170.bingo.setting.SettingsManager;

import java.io.File;

public class Bingo implements ModInitializer {
	public static final String MOD_ID = "bingo";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		LOGGER.info("Bingo mod loaded!");

		CommandRegistrationCallback.EVENT.register(BackpackCommand::register);
		CommandRegistrationCallback.EVENT.register(BingoCommand::register);
		CommandRegistrationCallback.EVENT.register(TopCommand::register);
		PlayerInventoryChangedCallback.EVENT.register(TeamManager::checkGoal);
		PlayerAdvancementAchievedCallback.EVENT.register(TeamManager::checkGoal);
		ServerTickEvents.START_SERVER_TICK.register(GameManager::onServerTick);

		PlayerBlockBreakEvents.BEFORE.register(new PlayerBlockBreakHandler());
		UseBlockCallback.EVENT.register(new UseBlockCallbackHandler());
		AttackEntityCallback.EVENT.register(new AttackEntityCallbackHandler());
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(new ServerEntityCombatEventsHandler());
		ServerPlayConnectionEvents.JOIN.register(new PlayerJoinHandler());
		ServerPlayerEvents.AFTER_RESPAWN.register(new PlayerRespawnHandler());

		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
	}

	private void onServerStarted(MinecraftServer server) {
		SERVER = server;
		GameManager.loadItemIcons();
		SettingsManager.loadSettings();
		TeamManager.createTeams();
		SettingsManager.applySettingsToServer();
	}

	private void onServerStopped(MinecraftServer server) {
		SettingsManager.saveSettings();

		if (GameManager.resetWorldOnStop) {
			String levelName = server.getSaveProperties().getLevelName();
			LOGGER.info("Resetting the world: {}", levelName);
			File directory = new File(levelName);
			if (directory.exists()) {
				try {
					FileUtils.deleteDirectory(directory);
					LOGGER.info("World directory deleted: {}", directory);
				} catch (Exception e) {
					LOGGER.error("Failed to delete the world directory: {}", e.getMessage());
				}
			} else {
				LOGGER.error("World directory does not exist: {}", directory);
			}
		}
	}
}
