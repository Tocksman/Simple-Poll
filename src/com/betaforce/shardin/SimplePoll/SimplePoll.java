package com.betaforce.shardin.SimplePoll;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplePoll extends JavaPlugin {
    private SimplePollExecutor myExecutor;
    List<Poll> polls = new ArrayList<Poll>();
    
    @Override
    public void onEnable() {
        myExecutor = new SimplePollExecutor(this);
        getCommand("simplepoll").setExecutor(myExecutor);
    }
    
    @Override
    public void onDisable() {
        // Do nothing.
    }
}
