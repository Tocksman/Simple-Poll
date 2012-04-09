package com.betaforce.shardin.SimplePoll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.entity.Player;


// A class that represents a poll.
public class Poll {
    private SimplePoll plugin;
    private String question;
    private HashMap<String, Set> votemap = new HashMap<String, Set>();

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
    
    public void purgeVoted() {
        Set options = votemap.keySet();
        Iterator iter = voted.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            String chosen = voted.get(name);
            if (!(options.contains(chosen))) {
                voted.remove(name);
            }
        }
    }
    
    public boolean addVoteOption(String label) {
        if (votemap.containsKey(label)) {
            return false;
        }
        
        votemap.put(label, new HashSet<String>());
        return true;
    }
    
    public boolean remVoteOption(String label) {
        if (!votemap.containsKey(label)) {
            return false;
        }
        
        totalVotes -= getNumVotesFor(label);
        votemap.remove(label);
        
        purgeVoted();
        return true;
    }
    
    public boolean voteFor(Player voter, String option) {
        if (voter == null || !votemap.containsKey(option) || hasVoted(voter)) {
            return false;
        }
        else {
            Set temp = votemap.get(option);
            temp.add(voter.getName());
            votemap.put(option, temp);
            
            voted.put(voter.getName(), option);
            
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
            Set temp = votemap.get(formerOption);
            temp.remove(voter.getName());
            votemap.put(formerOption, temp);
            
            temp = votemap.get(option);
            temp.add(voter.getName());
            votemap.put(option, temp);
            
            voted.put(voter.getName(), option);
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
    
    public Set getVotesFor(String key) {
        return votemap.get(key);
    }
    
    public int getNumVotesFor(String key) {
        return votemap.get(key).size();
    }
    
    public Set getKeys() {
        return votemap.keySet();
    }
    
    public String getOptionByNumber(int val) {
        int size = votemap.keySet().size();
        if (val > size) {
            return "";
        }
        
        Iterator iter = votemap.keySet().iterator();
        for (int i = 1; iter.hasNext(); i++) {
            String temp = (String) iter.next();
            if (i == val) {
                return temp;
            }
        }
        return "";
    }
}
