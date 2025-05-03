package me.josielcm.event.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
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
    @CommandCompletion("cakefever parkour bs gift")
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

        } else if (gameType.equalsIgnoreCase("gift")) {
            Cl3vent.getInstance().getEventManager().startGame(GameType.GIANTGIFT);
            sender.sendMessage(Color.parse("<green>Game started!"));

        } else {
            sender.sendMessage(Color.parse("<red>Invalid game type!"));
        }
    }

    @Subcommand("revive")
    @CommandPermission("cl3vent.command")
    @CommandCompletion("@players")
    public void onRevive(CommandSender sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            if (Cl3vent.getInstance().getEventManager().getPlayers().contains(player.getUniqueId())) {
                sender.sendMessage(Color.parse("<red>Player is not dead!"));
                return;
            }

            Cl3vent.getInstance().getEventManager().revivePlayer(player.getUniqueId());
            sender.sendMessage(Color.parse("<gold>" + player.getName() + " revived!"));
        } else {
            if (playerName.equalsIgnoreCase("all")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!Cl3vent.getInstance().getEventManager().getPlayers().contains(p.getUniqueId())) {
                        Cl3vent.getInstance().getEventManager().revivePlayer(p.getUniqueId());
                        sender.sendMessage(Color.parse("<gold>" + p.getName() + " revived!"));
                    }
                }
            } else {
                sender.sendMessage(Color.parse("<red>Player not found!"));
            }
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

    @Subcommand("kill")
    @CommandPermission("cl3vent.command")
    @CommandCompletion("@players")
    public void onKill(CommandSender sender, String nickname) {
        Player player = Bukkit.getPlayer(nickname);

        if (player != null) {
            if (!Cl3vent.getInstance().getEventManager().getPlayers().contains(player.getUniqueId())) {
                sender.sendMessage(Color.parse("<red>Player is already dead!"));
                return;
            }

            Cl3vent.getInstance().getEventManager().eliminatePlayer(player.getUniqueId());
        } else {
            sender.sendMessage(Color.parse("<red>Player not found!"));
        }
    }

    @CatchUnknown
    public void onUnknownCommand(CommandSender sender) {
        sender.sendMessage(Color.parse("<aqua><bold>Cl3vent</bold> <gray>v1.0</gray> <aqua>by JosielCM</aqua>"));
    }

    @HelpCommand
    public void onHelpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

}
