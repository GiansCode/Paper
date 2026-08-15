package io.papermc.paper.niceserver;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static net.kyori.adventure.text.Component.text;

public final class NiceServerCommand extends Command {

    public NiceServerCommand(final String name) {
        super(name);
        this.description = "Reload niceserver.yml";
        this.usageMessage = "/niceserver reload";
        this.setPermission("bukkit.command.niceserver");
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        if (args.length != 1 || !args[0].equals("reload")) {
            sender.sendMessage(text("Usage: " + this.usageMessage, NamedTextColor.RED));
            return false;
        }

        Command.broadcastCommandMessage(sender, text("Reloading niceserver.yml. Some options need a restart.", NamedTextColor.RED));
        NiceServerConfig.init();
        Command.broadcastCommandMessage(sender, text("NiceServer config reload complete.", NamedTextColor.GREEN));
        return true;
    }
}
