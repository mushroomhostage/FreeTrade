
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.enchantments.*;
import org.bukkit.*;

import info.somethingodd.bukkit.OddItem.OddItem;

class ItemQuery
{
    ItemStack itemStack;
    static Logger log = Logger.getLogger("Minecraft");

    public ItemQuery(String s) {
        Pattern p = Pattern.compile("^(\\d*)([# -]?)([^/]+)/?([\\d%]*)/?([^/]*)$");
        Matcher m = p.matcher(s);
        int quantity;

        while(m.find()) {
            String quantityString = m.group(1);
            String isStackString = m.group(2);
            String nameString = m.group(3);
            String usesString = m.group(4);
            String enchString = m.group(5);

            //log.info("uses=" + usesString + ", ench="+enchString);

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
                    itemStack = OddItem.getItemStack(nameString).clone();
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

            // Quantity, shorthand 10# = 10 stacks
            if (isStackString.equals("#")) {
                quantity *= Math.abs(itemStack.getType().getMaxStackSize());
            }
            itemStack.setAmount(quantity);

            // Damage value aka durability
            // User specifies how much they want left, 100% = unused tool
            short maxDamage = itemStack.getType().getMaxDurability();
            if (usesString != null && !usesString.equals("")) {
                short damage;

                if (usesString.endsWith("%")) {
                    String percentageString = usesString.substring(0, usesString.length() - 1);
                    double percentage = Double.parseDouble(percentageString);

                    damage = (short)(maxDamage - (short)(percentage / 100.0 * maxDamage));
                } else {
                    damage = (short)(maxDamage - Short.parseShort(usesString));
                }

                if (damage > maxDamage) {
                    damage = maxDamage;     // Breaks right after one use
                }

                if (damage < 0) {
                    damage = 0;             // Completely unused
                }

                itemStack.setDurability(damage);
            } else {
                // If they didn't specify a durability, but they want a durable item, assume no damage (0)
                // TODO: only assume 0 for wants. For gives, need to use value from inventory! Underspecified
            }

            // TODO: enchantments

            return;
        }

        throw new UsageException("Unrecognized item specification: " + s);
    }

    // Return whether an item degrades when used
    public static boolean isDurable(Material m) {
        return isTool(m) || isWeapon(m) || isArmor(m);
    }


    // Return whether the "damage"(durability) value is overloaded to mean
    // a different kind of material, instead of actual damage on a tool
    public static boolean dmgMeansSubtype(Material m) {
        // TODO: should this check be inverted, just checking for tools/weapons/armor (isDurable??) and returning true instead?
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

    public static boolean isTool(Material m) {
        switch (m) {
        case WOOD_PICKAXE:  case STONE_PICKAXE: case IRON_PICKAXE:  case GOLD_PICKAXE:  case DIAMOND_PICKAXE:
        case WOOD_AXE:      case STONE_AXE:     case IRON_AXE:      case GOLD_AXE:      case DIAMOND_AXE:
        case WOOD_SPADE:    case STONE_SPADE:   case IRON_SPADE:    case GOLD_SPADE:    case DIAMOND_SPADE:
        case FISHING_ROD:
            return true;
        default:
            return false;
        }
    }

    public static boolean isWeapon(Material m) {
        switch (m) {
        case WOOD_SWORD:    case STONE_SWORD:   case IRON_SWORD:    case GOLD_SWORD: case DIAMOND_SWORD:
        case BOW:
            return true;
        default:
            return false;
        }
    }

    public static boolean isArmor(Material m) {
        switch (m) {
        case LEATHER_BOOTS:     case IRON_BOOTS:      case GOLD_BOOTS:      case DIAMOND_BOOTS:
        case LEATHER_LEGGINGS:  case IRON_LEGGINGS:   case GOLD_LEGGINGS:   case DIAMOND_LEGGINGS:
        case LEATHER_CHESTPLATE:case IRON_CHESTPLATE: case GOLD_CHESTPLATE: case DIAMOND_CHESTPLATE:
        case LEATHER_HELMET:    case IRON_HELMET:     case GOLD_HELMET:     case DIAMOND_HELMET:
        // PUMPKIN is wearable on head, but isn't really armor, it doesn't take any damage
            return true;
        default:
            return false;
        }
    }



    public static String nameStack(ItemStack itemStack) {
        String name, extra;
        Material m = itemStack.getType();
       
        // If all else fails, use generic name from Bukkit
        name = itemStack.getType().toString();

        if (isDurable(m)) {
            // Percentage remaining
            
            // Round down so '100%' always means completely unused? (1 dmg = 99%)
            //int percentage = Math.round(Math.floor((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability()))
            // but then lower percentages are always one lower..
            // So just special-case 100% to avoid misleading
            int percentage;
            if (itemStack.getDurability() == 0) {
                percentage = 100;
            } else {
                percentage = (int)((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability());
                if (percentage == 100) {
                    percentage = 99;
                }
            }

            extra = "/" + percentage + "%";
        } else {
            extra = "";
        }

        // Find canonical name of item
        // TODO: better way? OddItem can have uncommon aliases we might pick..
        if (Bukkit.getServer().getPluginManager().getPlugin("OddItem") != null) {
            List<String> names;

            // Compatibility note:
            // OddItem 0.8.1 only has String method:
            //  getAliases(java.lang.String) in info.somethingodd.bukkit.OddItem.OddItem cannot be applied to (org.bukkit.inventory.ItemStack)
            //names = OddItem.getAliases(itemStack);
            String codeName;
            if (isDurable(m)) {
                // durable items don't have unique names for each durability
                codeName = itemStack.getTypeId() + ";0";
            } else {
                // durability here actually is overloaded to mean a different item
                codeName = itemStack.getTypeId() + ";" + itemStack.getDurability();
            }
            try {
                names = OddItem.getAliases(codeName);
                name = names.get(names.size() - 1);
            } catch (Exception e) {
                log.info("OddItem doesn't know about " + codeName + ", using " + name);
            }
        } else {
            log.info("OddItem not found, no more specific name available for " + name + ";" + itemStack.getDurability());
        }

        // TODO: enchantments

        return itemStack.getAmount() + "-" + name + extra;
    }

    // Return whether two item stacks have the same item, taking into account 'subtypes'
    // stored in the damage value (ex: blue wool, only same as blue wool) - but will
    // ignore damage for durable items (ex: diamond sword, 50% = diamond sword, 100%)
    public static boolean isSameType(ItemStack a, ItemStack b) {
        if (a.getType() != b.getType()) {
            return false;
        }

        Material m = a.getType();

        if (isDurable(m)) {
            return true;
        }
    
        return a.getDurability() == b.getDurability();
    }
}

class EnchantQuery
{
    static Logger log = Logger.getLogger("Minecraft");

    public EnchantQuery(String s) {
        Map<Enchantment,Integer> enchs = new HashMap<Enchantment,Integer>();

        String[] enchStrings = s.split("[, /-]");
        for (String enchString: enchStrings) {
            Pattern p = Pattern.compile("^([a-z-]+)([IV0-9]*)$");
            Matcher m = p.matcher(s);

            if (!m.find()) {
                throw new UsageException("Invalid enchantment: " + enchString);
            }

            String baseName = m.group(1);
            String levelString = m.group(2);

            Enchantment ench = enchFromBaseName(baseName);
            int level = levelFromString(levelString);

            // Odd, what's the point of having a separate 'wrapper' class?
            // Either way, it has useful methods for us
            EnchantmentWrapper enchWrapper = new EnchantmentWrapper(ench.getId());

            if (level > enchWrapper.getMaxLevel()) {
                level = ench.getMaxLevel();
            }

            log.info("Enchantment: " + ench + ", level="+level);

            enchs.put(ench, new Integer(level));
        }
    }

    static Enchantment enchFromBaseName(String n) {
        // TODO: something like OddItem for enchantment names! hideous, this
        Enchantment ench = Enchantment.getByName(n);
        if (ench != null) {
            return ench;
        }

        // Armor
        if (n.equals("protection")) {
            return Enchantment.PROTECTION_ENVIRONMENTAL;
        } else if (n.equals("fire-protection") || n.equals("fireprotection") || n.equals("fire")) {
            return Enchantment.PROTECTION_FIRE;
        } else if (n.equals("feather-falling") || n.equals("featherfalling") || n.equals("feather") || n.equals("falling") || n.equals("fall")) {
            return Enchantment.PROTECTION_FALL;
        } else if (n.equals("blast-protection") || n.equals("blastprotection") || n.equals("blast") || n.equals("explosion-protection")) {
            return Enchantment.PROTECTION_EXPLOSIONS;
        } else if (n.equals("projectile-protection") || n.equals("projectileprotection") || n.equals("projectile")) {
            return Enchantment.PROTECTION_PROJECTILE;
        } else if (n.equals("respiration") || n.equals("oxygen")) {
            return Enchantment.OXYGEN; 
        } else if (n.equals("aqua-affinity") || n.equals("aquaaffinity") || n.equals("aqua") || n.equals("waterworker")) {
            return Enchantment.WATER_WORKER;
        // Weapons
        } else if (n.equals("sharpness") || n.equals("damage-all")) {
            return Enchantment.DAMAGE_ALL;
        } else if (n.equals("smite") || n.equals("damage-undead")) {
            return Enchantment.DAMAGE_UNDEAD;
        } else if (n.equals("bane-of-arthropods") || n.equals("bane") || n.equals("arthropods")) {
            return Enchantment.DAMAGE_ARTHROPODS;
        } else if (n.equals("knockback")) {
            return Enchantment.KNOCKBACK; 
        } else if (n.equals("fire-aspect") || n.equals("fireaspect") || n.equals("fire")) {
            return Enchantment.FIRE_ASPECT;
        } else if (n.equals("looting") || n.equals("loot") || n.equals("loot-bonus-mobs")) {
            return Enchantment.LOOT_BONUS_MOBS;
        // Tools
        } else if (n.equals("efficiency") || n.equals("dig-speed")) {
            return Enchantment.DIG_SPEED;
        } else if (n.equals("silk-touch") || n.equals("silktouch") || n.equals("silk")) {
            return Enchantment.SILK_TOUCH;
        } else if (n.equals("unbreaking") || n.equals("durability")) {
            return Enchantment.DURABILITY;
        } else if (n.equals("fortune") || n.equals("loot-bonus-blocks")) {
            return Enchantment.LOOT_BONUS_BLOCKS;
        } else {
           throw new UsageException("Unrecognized enchantment name: " + n);
        }
    }

    static int levelFromString(String s) {
        if (s.equals("") || s.equals("I")) {
            return 1;
        } else if (s.equals("II")) { 
            return 2;
        } else if (s.equals("III")) {
            return 3;
        } else if (s.equals("IV")) {
            return 4;
        } else if (s.equals("V")) {
            return 5;
        } else {
            return Integer.parseInt(s);
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
        return player.getDisplayName() + " wants " + ItemQuery.nameStack(want) + " for " + ItemQuery.nameStack(give) + (exact ? " (exact)" : "");
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

            //log.info("oldOrder: " + oldOrder);
            //log.info("newOrder: " + newOrder);

            // Are they giving what anyone else wants?
            if (!ItemQuery.isSameType(newOrder.give, oldOrder.want) ||
                !ItemQuery.isSameType(newOrder.want, oldOrder.give)) {
                log.info("Not matched, different types");
                continue;
            }

            double newRatio = (double)newOrder.give.getAmount() / newOrder.want.getAmount();
            double oldRatio = (double)oldOrder.want.getAmount() / oldOrder.give.getAmount();

            // Offering a better or equal deal? (Quantity = relative value)
            log.info("ratio " + newRatio + " >= " + oldRatio);
            if (!(newRatio >= oldRatio)) { 
                log.info("Not matched, worse relative value");
                continue;
            }

            // Is item less damaged or equally damaged than wanted? (Durability)
            if (ItemQuery.isDurable(newOrder.give.getType())) {
                if (newOrder.give.getDurability() > oldOrder.want.getDurability()) {
                    log.info("Not matched, worse damage new, " + newOrder.give.getDurability() + " < " + oldOrder.want.getDurability());
                    continue;
                }
            }
            if (ItemQuery.isDurable(oldOrder.give.getType())) {
                if (oldOrder.give.getDurability() > newOrder.want.getDurability()) {
                    log.info("Not matched, worse damage old, " + oldOrder.give.getDurability() + " < " + newOrder.want.getDurability());
                    continue;
                }
            }

            // TODO: enchantment checks
            
        
            // Generalize to "betterness"

        
            // Determine how much of the order can be fulfilled
            int remainingWant = oldOrder.want.getAmount() - newOrder.give.getAmount();
            int remainingGive = oldOrder.give.getAmount() - newOrder.want.getAmount();

            log.info("remaining want="+remainingWant+", give="+remainingGive);

            // They get what they want!
            // TODO: ensure contains() before removing

            // Calculate amount exchanged
            ItemStack exchWant = new ItemStack(oldOrder.want.getType(), Math.min(oldOrder.want.getAmount(), newOrder.give.getAmount()), newOrder.give.getDurability());
            ItemStack exchGive = new ItemStack(oldOrder.give.getType(), Math.min(oldOrder.give.getAmount(), newOrder.want.getAmount()), oldOrder.give.getDurability());

            log.info("exchWant="+ItemQuery.nameStack(exchWant));
            log.info("exchGive="+ItemQuery.nameStack(exchGive));

            oldOrder.player.getInventory().addItem(exchWant);
            newOrder.player.getInventory().remove(exchWant);
            Bukkit.getServer().broadcastMessage(oldOrder.player.getDisplayName() + " received " + 
                ItemQuery.nameStack(exchWant) + " from " + newOrder.player.getDisplayName());

            newOrder.player.getInventory().addItem(exchGive);
            oldOrder.player.getInventory().remove(exchGive);
            Bukkit.getServer().broadcastMessage(newOrder.player.getDisplayName() + " received " + 
                ItemQuery.nameStack(exchGive) + " from " + oldOrder.player.getDisplayName());

    
            // Remove oldOrder from orders, if complete, or add partial if incomplete
            if (remainingWant == 0) {
                // This order is finished, old player got everything they wanted
                // Note: remainingWant can be negative if they got more than they bargained for
                // (other player offered a better deal than expected). Either way, done deal.
                orders.remove(oldOrder);
                log.info("Closed order " + oldOrder);
                return true;
            } else if (remainingWant > 0) {
                oldOrder.want.setAmount(remainingWant);
                oldOrder.give.setAmount(remainingGive);

                Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                return true;
            } else if (remainingWant < 0) {
                orders.remove(oldOrder);
                orders.remove("Closed order " + oldOrder);

                newOrder.want.setAmount(-remainingGive);
                newOrder.give.setAmount(-remainingWant);
                log.info("Adding new partial order");
                return false;
            }

        }
        return false;
    }
}

public class FreeTrade extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");
    Market market = new Market();

    public void onEnable() {
        log.info(getDescription().getName() + " enabled");

        log.info("e=" + new EnchantQuery("fortuneII"));
    }

    public void onDisable() {
        log.info(getDescription().getName() + " disabled");
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


