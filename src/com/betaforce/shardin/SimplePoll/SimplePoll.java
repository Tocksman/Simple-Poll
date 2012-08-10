package com.betaforce.shardin.SimplePoll;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplePoll extends JavaPlugin {
    private SimplePollExecutor executor;
    List<Poll> polls = new ArrayList<Poll>();
    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        executor = new SimplePollExecutor(this);
        getCommand("simplepoll").setExecutor(executor);
        getCommand("sp").setExecutor(executor);
    }
    
    @Override
    public void onDisable() {
        // Do nothing.
    }
}
