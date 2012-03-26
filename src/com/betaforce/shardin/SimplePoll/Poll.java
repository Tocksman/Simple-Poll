package com.betaforce.shardin.SimplePoll;

import java.util.HashMap;
import java.util.Set;
import org.bukkit.entity.Player;


// A class that represents a poll.
public class Poll {
    private SimplePoll plugin;
    private String question;
    private HashMap<String, Integer> votemap = new HashMap<String, Integer>();

    private int totalVotes;

    private HashMap<String, String> voted = new HashMap<String, String>();
    
    protected Poll(SimplePoll plugin, String question) {
        this.plugin = plugin;
        this.question = question;
        this.totalVotes = 0;
    }
    
    public String getName() {
        return question;
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
            voted.put(voter.getName(), option);
            int numVotes = votemap.get(option);
            votemap.put(option, numVotes + 1);
            totalVotes++;
            return true;
        }
    }
    
    public boolean changeVoteFor(Player voter, String option) {
        if (voter == null || !votemap.containsKey(option) || !hasVoted(voter)) {
            return false;
        }
        else {
            String formerOption = voted.get(voter.getName());
            int formerNumVotes = votemap.get(formerOption);
            votemap.put(formerOption, formerNumVotes - 1);
            
            int numVotes = votemap.get(option);
            voted.put(voter.getName(), option);
            votemap.put(option, numVotes + 1);
            return true;
        }
    }
    
    public boolean hasVoted(Player voter) {
        String val = voted.get(voter.getName());
        if (val == null) { // hasn't even voted yet.
            return false;
        }
        else {
            if (!votemap.containsKey(val)) { // player voted, but option has
                return false;                // since been removed.
            }
            else {
                return true; // has voted and option still exists
            }
        }
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
