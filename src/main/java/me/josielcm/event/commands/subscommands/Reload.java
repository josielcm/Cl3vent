package me.josielcm.event.commands.subscommands;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.josielcm.event.Cl3vent;

@CommandAlias(value = "base")
public class Reload extends BaseCommand {

    @Subcommand(value = "reload")
    @CommandPermission(value = "")
    public void onStart(CommandSender sender) {
        Cl3vent.getInstance().reload();
    }

}
