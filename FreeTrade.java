
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import java.util.regex.*;
import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;

class ItemSpec
{
    int quantity;
    Material material;

    public ItemSpec(String s) {
        Pattern p = Pattern.compile("^(\\d*)([# -]?)(\\p{Alpha}+)$");
        Matcher m = p.matcher(s);
        while(m.find()) {
            String quantityString = m.group(1);
            String isStackString = m.group(2);
            String nameString = m.group(3);

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

            if (isStackString.equals("#")) {
                quantity *= material.getMaxStackSize();
            }
        }
    }

    public String toString() {
        return quantity + " " + material;
    }
}

class Order
{
    Player player;
    ItemSpec want, give;
    boolean exact;

    public Order(Player p, String wantString, String giveString) {
        player = p;

        if (wantString.contains("!")) {
            exact = true;
            wantString = wantString.replace("!", "");
        }
        if (giveString.contains("!")) {
            exact = true;
            giveString = giveString.replace("!", "");
        }

        want = new ItemSpec(wantString);
        give = new ItemSpec(giveString);
    }

    public String toString() {
        return player.getDisplayName() + " wants " + want + " for " + give + (exact ? " (exact)" : "");
    }
}

class Market
{
    ArrayList<Order> orders;
    Logger log = Logger.getLogger("Minecraft");

    public Market() {
        // TODO: load from file, save to file
        orders = new ArrayList<Order>();
    }

    public boolean showOutstanding(CommandSender sender) {
        sender.sendMessage("TODO: show open orders");

        for (int i = 0; i < orders.size(); i++) {
            sender.sendMessage(i + ". " + orders.get(i));
        }

        return false;
    }

    public void placeOrder(Order order) {
        if (matchOrder(order)) {
            // Executed
            return;
        }

        // Add to outstanding to match with future order 
        orders.add(order);
    }

    public boolean matchOrder(Order newOrder) {
        for (int i = 0; i < orders.size(); i++) {
            Order oldOrder = orders.get(i);

            // Are they giving what anyone else wants?
            // TODO: durability, enchantment checks
            if (newOrder.give.material == oldOrder.want.material &&
                newOrder.want.material == oldOrder.give.material) { 

    
                // TODO: quantity check

                // They got what they want
                log.info(newOrder.player.getDisplayName() + " received " + newOrder.want + " from " + oldOrder.player.getDisplayName());
                log.info(oldOrder.player.getDisplayName() + " received " + oldOrder.want + " from " + newOrder.player.getDisplayName());

                // TODO: actually exchange
                //newOrder.player.remove(ItemStack)..

                // TODO: remove oldOrder from orders

                return true;
            }
        }

        return false;
    }
}

public class FreeTrade extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");
    Market market = new Market();

    public void onEnable() {
        log.info("FreeTrade enabled");
    }

    public void onDisable() {
        log.info("FreeTrade disabled");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player;
        int n = 0;

        if (!cmd.getName().equalsIgnoreCase("want")) {
            return false;
        }

        // /want
        if (args.length == 0) {
            return market.showOutstanding(sender);
        }

        if (sender instanceof Player) {
            player = (Player)sender;
        } else {
            // Get player name from first argument
            player = Bukkit.getServer().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("no such player");
                return false;
            }
            n++;
        }

        if (args.length < 2+n) {
            return false;
        }

        String wantString, giveString;
        wantString = args[n];
        if (args[n+1].equalsIgnoreCase("for")) {
            giveString = args[n+2];
        } else {
            giveString = args[n+1];
        }

        Order order = new Order(player, wantString, giveString);

        sender.sendMessage(order.toString());

        market.placeOrder(order);

        return true;
    }
}


