package me.josielcm.jcm.commands;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import me.josielcm.jcm.api.formats.Color;

@CommandAlias("dick")
public class DickMain extends BaseCommand {

    @CatchUnknown
    public void onUnknownCommand(CommandSender sender) {
        sender.sendMessage(Color.parse("<yellow><bold>Base</bold> <gray>v1.0</gray> <yellow>by JosielCM</yellow>"));
    }

    @HelpCommand
    public void onHelpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

}
