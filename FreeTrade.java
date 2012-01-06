
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class FreeTrade extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {
        log.info("FreeTrade enabled");
    }

    public void onDisable() {
        log.info("FreeTrade disabled");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player;
        String wanted, giving;

        if (!cmd.getName().equalsIgnoreCase("want")) {
            return false;
        }

        // /want
        if (args.length == 0) {
            return showOutstanding(sender);
        }

        if (sender instanceof Player) {
            player = (Player)sender;
        } else {
            // TODO: get player from name as first argument
            sender.sendMessage("this command can only be run by a player");
        }

        if (args.length < 2) {
            return false;
        }

        wanted = args[0];
        if (args[1].equalsIgnoreCase("for")) {
            giving = args[2];
        } else {
            giving = args[1];
        }

        sender.sendMessage("you want " + wanted + " for " + giving);

        return true;
    }

    public boolean showOutstanding(CommandSender sender) {
        sender.sendMessage("TODO: show open orders");

        return false;
    }
}


