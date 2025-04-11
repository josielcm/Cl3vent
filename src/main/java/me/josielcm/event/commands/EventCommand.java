package me.josielcm.event.commands;

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

@CommandAlias("evento")
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
        } else {
            sender.sendMessage(Color.parse("<red>Invalid game type!"));
        }
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
