
package com.exphc.FreeTrade;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class FreeTrade extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {
        log.info("FreeTrade enabled");
    }

    public void onDisable() {
        log.info("FreeTrade disabled");
    }
}


