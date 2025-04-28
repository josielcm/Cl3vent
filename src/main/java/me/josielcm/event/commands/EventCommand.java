package me.josielcm.event.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.manager.games.GameType;

@CommandAlias("event")
public class EventCommand extends BaseCommand {

    @Subcommand("start")
    @CommandPermission("cl3vent.command")
    public void onStart(CommandSender sender, String gameType) {
        if (gameType.equalsIgnoreCase("cakefever")) {
            Cl3vent.getInstance().getEventManager().startGame(GameType.CAKEFEVER);
            sender.sendMessage(Color.parse("<green>Game started!"));
        } else if (gameType.equalsIgnoreCase("parkour")) {
            Cl3vent.getInstance().getEventManager().startGame(GameType.BALLOONPARKOUR);
            sender.sendMessage(Color.parse("<green>Game started!"));
        } else if (gameType.equalsIgnoreCase("bs")) {
            Cl3vent.getInstance().getEventManager().startGame(GameType.BALLONSHOOTING);
            sender.sendMessage(Color.parse("<green>Game started!"));
        } else {
            sender.sendMessage(Color.parse("<red>Invalid game type!"));
        }
    }

    @Subcommand("revive")
    @CommandPermission("cl3vent.command")
    public void onRevive(CommandSender sender, String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player != null) {
            Cl3vent.getInstance().getEventManager().revivePlayer(player.getUniqueId());
            sender.sendMessage(Color.parse("<green>Player revived!"));
        } else {
            sender.sendMessage(Color.parse("<red>Player not found!"));
        }
    }

    @Subcommand("stop")
    @CommandPermission("cl3vent.command")
    public void onStop(CommandSender sender) {
        
        if (Cl3vent.getInstance().getEventManager().getActualGame() == GameType.NONE) {
            sender.sendMessage(Color.parse("<red>No game is currently running!"));
            return;
        }

        sender.sendMessage(Color.parse("<yellow>Stopping game..."));
        Cl3vent.getInstance().getEventManager().stopGame();
    }

    @CatchUnknown
    public void onUnknownCommand(CommandSender sender) {
        sender.sendMessage(Color.parse("<yellow><bold>Base</bold> <gray>v1.0</gray> <yellow>by JosielCM</yellow>"));
    }

    @HelpCommand
    public void onHelpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

}
