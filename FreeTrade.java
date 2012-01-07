
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import java.util.regex.*;
import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.*;

import info.somethingodd.bukkit.OddItem.OddItem;

class ItemQuery
{
    ItemStack itemStack;
    Logger log = Logger.getLogger("Minecraft");

    public ItemQuery(String s) {
        Pattern p = Pattern.compile("^(\\d*)([# -]?)([^/]+)/?([\\d%]*)/?([^/]*)$");
        Matcher m = p.matcher(s);
        int quantity;

        while(m.find()) {
            String quantityString = m.group(1);
            String isStackString = m.group(2);
            String nameString = m.group(3);
            String dmgString = m.group(4);
            String enchString = m.group(5);

            log.info("dmg=" + dmgString + ", ench="+enchString);

            if (quantityString.equals("")) {
                quantity = 1;
            } else {
                quantity = Integer.parseInt(quantityString);
                if (quantity < 0) {
                    throw new UsageException("Invalid quantity: " + quantity);
                }
            }

            // Lookup item name
            if (Bukkit.getServer().getPluginManager().getPlugin("OddItem") != null) {
                try {
                    itemStack = OddItem.getItemStack(nameString);
                } catch (IllegalArgumentException suggestion) {
                    throw new UsageException("No such item '" + nameString + "', did you mean '" + suggestion.getMessage() + "'?");
                }
            } else {
                // OddItem isn't installed so use Bukkit's long names (diamond_pickaxe, cumbersome)
                // Note this also means you need to manually specify damage values (wool/1, not orangewool!)
                // Therefore installing OddItem is highly recommended
                Material material = Material.matchMaterial(nameString);

                if (material == null) {
                    throw new UsageException("Unrecognized item name: " + nameString + " (please install OddItem)");
                }

                itemStack = new ItemStack(material);
            }

            if (itemStack == null) {
                throw new UsageException("Unrecognized item name: " + nameString);
            }

            // 10# = 10 stacks
            if (isStackString.equals("#")) {
                quantity *= Math.abs(itemStack.getType().getMaxStackSize());
            }

            // Damage value aka durability
            // TODO: only for tools? not overloaded 'subtypes'
            if (dmgString != null && !dmgString.equals("")) {
                short maxDurability = itemStack.getType().getMaxDurability();
                short dmg;

                if (dmgString.endsWith("%")) {
                    String percentageString = dmgString.substring(0, dmgString.length() - 1);
                    double percentage = Double.parseDouble(percentageString);

                    dmg = (short)(percentage / 100.0 * maxDurability);
                } else {
                    dmg = (short)Integer.parseInt(dmgString);
                }

                if (dmg > maxDurability) {
                    dmg = maxDurability;
                }

                if (dmg < 1) {
                    dmg = 1;
                }

                itemStack.setDurability(dmg);
                log.info("Set dmg="+dmg);
            }

            itemStack.setAmount(quantity);

            // TODO: damage, data, enchantments

            return;
        }

        throw new UsageException("Unrecognized item specification: " + s);
    }

    // Return whether the "damage"(durability) value is overloaded to mean
    // a different kind of material, instead of actual damage on a tool
    public static boolean dmgMeansSubtype(Material m) {
        // TODO: should this check be inverted, just checking for tools and returning true instead?
        switch (m) {
        // Blocks
        case SAPLING:
        case LEAVES:
        case LOG:
        case WOOL:
        case DOUBLE_STEP:
        case STEP:
        case SMOOTH_BRICK:

        // Items
        case COAL:
        case INK_SACK:
        case POTION:
        case MAP:           // for some reason not indicated on http://www.minecraftwiki.net/wiki/Data_values
        case MONSTER_EGGS:

        // Materials not legitimately acquirable in inventory, but here for completeness
        // (refer to Material enum, entries with MaterialData classes)
        // See http://www.minecraftwiki.net/wiki/Data_values#Data for their data value usage

        // Note that environmental data (age,orientation,etc.) on IDs used as both blocks
        // but not required for items does NOT cause true to be returned here.
        case WATER:                // item: WATER_BUCKET
        case STATIONARY_WATER:
        case LAVA:                 // item: LAVA_BUCKET
        case STATIONARY_LAVA:
        //case DISPENSER:          // orientation, but not relevant to item
        case BED_BLOCK:            // item: BED
        //case POWERED_RAIL:       // whether powered, but not relevant to item
        //case DETECTOR_RAIL:      // whether detecting, but not relevant to item
        case PISTON_STICKY_BASE:
        case LONG_GRASS:           // TODO: http://www.minecraftwiki.net/wiki/Data_values#Tall_Grass, can enchanted tool acquire?
        case PISTON_BASE:
        case PISTON_EXTENSION:
        //case TORCH:               // orientation, but not relevant to item
        //case WOOD_STAIRS:         // orientation, but not relevant to item   
        case REDSTONE_WIRE:         // item: REDSTONE
        case CROPS:                 // item: SEEDS
        case SOIL:                  // TODO: can silk touch acquire farmland?
        //case FURNANCE:            // orientation, but not relevant to item
        //case BURNING_FURNANCE:    // TODO: supposedly silk touch can acquire?
        case SIGN_POST:             // item: SIGN
        case WOODEN_DOOR:           // item: WOOD_DOOR (confusing!)
        //case LADDER:              // orientation, but not relevant to item
        //case RAILS:               // orientation, but not relevant to item 
        //case COBBLESTONE_STAIRS:  // orientation, but not relevant to item
        case WALL_SIGN:             // item: SIGN
        //case LEVER:               // orientation & state, but not relevant to item
        //case STONE_PLATE:         // state, but not relevant to item
        case IRON_DOOR_BLOCK:       // item: IRON_DOOR
        //case WOOD_PLATE:          // state, but not relevant to item
        case REDSTONE_TORCH_OFF:  
        //case REDSTONE_TORCH_ON:   // orientation, but not relevant to item
        //case STONE_BUTTON:        // state, but not relevant to item
        //case CACTUS:              // age, but not relevant to item
        case SUGAR_CANE_BLOCK:      // item: 338
        //case PUMPKIN:             // orientation, but not relevant to item
        //case JACK_O_LATERN:       // orientation, but not relevant to item
        case CAKE_BLOCK:            // item: CAKE
        case DIODE_BLOCK_OFF:       // item: DIODE
        case DIODE_BLOCK_ON:        // item: DIODE
        //case TRAP_DOOR:           // orientation, but not relevant to type
        //case ENDER_PORTAL_FRAME:    // TODO: has data, but no class in Bukkit? there are others
            return true;

        default:
            return false;
        }
    }
}

class Order
{
    Player player;
    ItemStack want, give;
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

        want = (new ItemQuery(wantString)).itemStack;
        give = (new ItemQuery(giveString)).itemStack;
    }

    public Order(Player p, ItemStack w, ItemStack g, boolean e) {
        player = p;
        want = w;
        give = g;
        exact = e;
    }

    public String toString() {
        return player.getDisplayName() + " wants " + want + " for " + give + (exact ? " (exact)" : "");
    }


}

class UsageException extends RuntimeException
{
    public String message;

    public UsageException(String msg) {
        message = msg;
    }

    public String toString() {
        return "UsageException: " + message;
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
        sender.sendMessage("Open orders:");

        for (int i = 0; i < orders.size(); i++) {
            sender.sendMessage(i + ". " + orders.get(i));
        }

        sender.sendMessage("To add or fulfill an order:");

        return false;
    }

    public void placeOrder(Order order) {
        if (matchOrder(order)) {
            // Executed
            return;
        }

        // Not fulfilled; add to outstanding to match with future order 
        // Broadcast to all players so they know someone wants something, then add
        Bukkit.getServer().broadcastMessage("Wanted: " + order);
        orders.add(order);
    }

    public boolean matchOrder(Order newOrder) {
        for (int i = 0; i < orders.size(); i++) {
            Order oldOrder = orders.get(i);

            // Are they giving what anyone else wants?
            if (newOrder.give.getType() == oldOrder.want.getType() &&
                newOrder.want.getType() == oldOrder.give.getType()) { 

                double newRatio = (double)newOrder.give.getAmount() / newOrder.want.getAmount();
                double oldRatio = (double)oldOrder.want.getAmount() / oldOrder.give.getAmount();
    
                // TODO: quantity check, generalize to other "betterness"
                // TODO: durability, enchantment checks
                // Offering a better or equal deal?
                log.info("ratio " + newRatio + " >= " + oldRatio);
                if (newRatio >= oldRatio) { 
                    // Is there enough?
                    if (oldOrder.give.getAmount() >= newOrder.want.getAmount()) {


                        // They get what they want!
                        newOrder.player.getInventory().addItem(newOrder.want);
                        oldOrder.player.getInventory().remove(oldOrder.give); // TODO: ensure contains()
                        Bukkit.getServer().broadcastMessage(newOrder.player.getDisplayName() + " received " + newOrder.want + " from " + oldOrder.player.getDisplayName());

                        Bukkit.getServer().broadcastMessage(oldOrder.player.getDisplayName() + " received " + newOrder.give + " from " + newOrder.player.getDisplayName());
                        oldOrder.player.getInventory().addItem(oldOrder.want);
                        newOrder.player.getInventory().remove(newOrder.give);

                        // TODO: remove oldOrder from orders, if complete, or add partial if incomplete

                        int remainingWant = oldOrder.want.getAmount() - newOrder.give.getAmount();
                        int remainingGive = oldOrder.give.getAmount() - newOrder.want.getAmount();

                        if (remainingWant <= 0) {
                            // This order is finished, old player got everything they wanted
                            // Note: remainingWant can be negative if they got more than they bargained for
                            // (other player offered a better deal than expected). Either way, done deal.
                            orders.remove(oldOrder);
                            log.info("Closed order " + oldOrder);
                        } else {
                            oldOrder.want.setAmount(remainingWant);
                            oldOrder.give.setAmount(remainingGive);

                            Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                        }
                        return true;
                    }
                }
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


