
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import java.util.regex.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;

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
            //return false;
        }

        if (args.length < 2) {
            return false;
        }

        String wantedString, givingString;
        wantedString = args[0];
        if (args[1].equalsIgnoreCase("for")) {
            givingString = args[2];
        } else {
            givingString = args[1];
        }

        Pattern p = Pattern.compile("^(\\d*)(#?)(\\p{Alpha}+)(!?)$");
        Matcher m = p.matcher(wantedString);
        while(m.find()) {
            sender.sendMessage("quantity = " + m.group(1));
            sender.sendMessage("isStacks = " + m.group(2));
            sender.sendMessage("name = " + m.group(3));
            sender.sendMessage("isExact = " + m.group(4));
        }

        
        Material wanted, giving;

        // TODO: really need better material matching names, shorthands
        // diamond_pickaxe, too long. diamondpick, dpick, would be better.
        // iron_ingot, want just iron or i. shortest match: cobblestone, cobble. common: diamond, d. plural.
        wanted = Material.matchMaterial(wantedString);
        if (wanted == null) {
            sender.sendMessage("Invalid item wanted: " + wantedString);
            return false;
        }

        giving = Material.matchMaterial(givingString);
        if (giving == null) {
            sender.sendMessage("Invalid item giving: " + givingString);
            return false;
        }

        sender.sendMessage("you want " + wanted.toString() + " for " + giving.toString());

        return true;
    }

    public boolean showOutstanding(CommandSender sender) {
        sender.sendMessage("TODO: show open orders");

        return false;
    }

}


