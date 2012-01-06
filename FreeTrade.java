
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import java.util.regex.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;

class ItemSpec
{
    int quantity;
    boolean exact;
    Material material;

    public ItemSpec(String s) {
        Pattern p = Pattern.compile("^(\\d*)(#?)(\\p{Alpha}+)(!?)$");
        Matcher m = p.matcher(s);
        while(m.find()) {
            String quantityString = m.group(1);
            String isStackString = m.group(2);
            String nameString = m.group(3);
            String isExactString = m.group(4);


            quantity = Integer.parseInt(quantityString);
            if (quantity < 0) {
                quantity = 1;
            }

            // TODO: really need better material matching names, shorthands
            // diamond_pickaxe, too long. diamondpick, dpick, would be better.
            // iron_ingot, want just iron or i. shortest match: cobblestone, cobble. common: diamond, d. plural.
            material = Material.matchMaterial(nameString);
            if (material == null) {
                // TODO: exception?
            }

            if (isStackString.length() != 0) {
                quantity *= material.getMaxStackSize();
            }

            exact = isExactString.length() != 0;

        }
    }

    public String toString() {
        return quantity + " " + material + (exact ? " (exact) " : "");
    }
}

class Order
{
    Player player;
    ItemSpec want, give;

    public Order(Player p, String wantString, String giveString) {
        player = p;
        want = new ItemSpec(wantString);
        give = new ItemSpec(giveString);
    }

    public String toString() {
        return player + " wants " + want + " for " + give;
    }
}

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
            player = null;
            //return false;
        }

        if (args.length < 2) {
            return false;
        }

        String wantString, giveString;
        wantString = args[0];
        if (args[1].equalsIgnoreCase("for")) {
            giveString = args[2];
        } else {
            giveString = args[1];
        }

        Order order = new Order(player, wantString, giveString);

        sender.sendMessage(order.toString());

        return true;
    }

    public boolean showOutstanding(CommandSender sender) {
        sender.sendMessage("TODO: show open orders");

        return false;
    }

}


