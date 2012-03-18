package com.betaforce.shardin.SimplePoll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


// A class that represents a poll.
public class Poll {
    private SimplePoll plugin;
    private String question;
    private HashMap<String, Integer> votemap = new HashMap<String, Integer>();

    private int totalVotes;

    private List<String> voted = new ArrayList<String>();
    
    protected Poll(SimplePoll plugin, String question) {
        this.plugin = plugin;
        this.question = question;
        this.totalVotes = 0;
    }
    
    public String getName() {
        return question;
    }
    
    public void announceCreation(String senderName) {
        for (Player player: plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("SimplePoll.vote")) { // only announce to people that can vote
                player.sendMessage(ChatColor.GREEN + senderName + " has opened a "
                        + "new poll! Type \"/simplepoll list\" to see it.");
            }
        }
    }
    
    public boolean addVoteOption(String label) {
        if (votemap.containsKey(label)) {
            return false;
        }
        
        votemap.put(label, 0);
        return true;
    }
    
    public boolean remVoteOption(String label) {
        if (!votemap.containsKey(label)) {
            return false;
        }
        
        totalVotes -= votemap.get(label);
        votemap.remove(label);
        return true;
    }
    
    public boolean voteFor(Player voter, String option) {
        if (voter == null || !votemap.containsKey(option) || hasVoted(voter)) {
            return false;
        }
        else {
            voted.add(voter.getName());
            int numVotes = votemap.get(option);
            votemap.put(option, numVotes + 1);
            totalVotes++;
            return true;
        }
    }
    public boolean hasVoted(Player voter) {
        return (voted.contains(voter.getName()));
    }

    public int getTotalVotes() {
        return totalVotes;
    }
    
    public int getVotesFor(String key) {
        return votemap.get(key);
    }
    
    public Set getKeys() {
        return votemap.keySet();
    }
}
