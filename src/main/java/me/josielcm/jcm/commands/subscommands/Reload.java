package me.josielcm.jcm.commands.subscommands;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.josielcm.jcm.Base;

@CommandAlias(value = "base")
public class Reload extends BaseCommand {

    @Subcommand(value = "reload")
    @CommandPermission(value = "")
    public void onStart(CommandSender sender) {
        Base.getInstance().reload();
    }

}
