package com.betaforce.shardin.SimplePoll;

import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SimplePollExecutor implements CommandExecutor {
    private SimplePoll plugin;
    public SimplePollExecutor(SimplePoll plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (strings.length < 1) {
            cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll create <Description>\"");
            return true;
        }
        String option = strings[0];
        if(option.equalsIgnoreCase("create")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            Poll obj;
            String description = "";
            
            if ((strings.length - 1) > 0) {
                description = convertArrayToString(strings, 1);
                obj = new Poll(plugin, description);
                plugin.polls.add(obj);
                obj.announceCreation((cs instanceof Player) ? (cs.getName()) : (cs instanceof ConsoleCommandSender) ? "Console" : "Unknown");
                
//                cs.sendMessage(ChatColor.GREEN + "Successfully added poll.");
            }
            else {
                description = "NewPoll" + Integer.toString(plugin.polls.size()+1);
                obj = new Poll(plugin, description);
                plugin.polls.add(obj);
                
                cs.sendMessage(ChatColor.GREEN + "Successfully added poll under"
                        + " a generic name.");
            }
            return true;
        }
        else if(option.equalsIgnoreCase("addoption")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if (strings.length <= 2) {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll addoption <pollID> <Option Name>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            boolean outcome = obj.addVoteOption(convertArrayToString(strings, 2));
            if (!outcome) {
                cs.sendMessage(ChatColor.RED + "That option already exists.");
            }
            else {
                cs.sendMessage(ChatColor.GREEN + "Successfully added vote option "
                        + "to poll.");
            }
        }
        else if(option.equalsIgnoreCase("remoption")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if (strings.length <= 2) {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll remoption <pollID> <Option Name>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            boolean outcome = obj.remVoteOption(convertArrayToString(strings, 2));
            if (!outcome) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll option!");
            }
            else {
                cs.sendMessage(ChatColor.GREEN + "Successfully removed vote option.");
                cs.sendMessage(ChatColor.RED + "WARNING! Those who have already voted "
                        + "will not be able to vote again!");
            }
        }
        else if(option.equalsIgnoreCase("help")) {
            sendSimplePollHelp(cs);
        }
        else if(option.equalsIgnoreCase("info")) {
            if (!(cs.hasPermission("SimplePoll.vote"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(plugin.polls.size() < 1) {
                cs.sendMessage("There are no polls right now.");
                return true;
            }
            
            if (strings.length < 2) {
                cs.sendMessage("List of polls ---------------------");
                cs.sendMessage("<ID>:    <DESCRIPTION>    <VOTED>");
                Poll obj;
                String voted;
                for(int i = 0; i < plugin.polls.size(); i++) {
                    obj = plugin.polls.get(i);
                    voted = ((cs instanceof ConsoleCommandSender) ? "N/A" : (obj.hasVoted((Player) cs) && cs instanceof Player) ? "Yes" : "No");
                    cs.sendMessage(i + ":    " + obj.getName() + "    " + voted);
                }
            }
            else if (strings.length == 2) {
                Poll obj = matchPoll(strings[1]);
                if (obj == null) {
                    cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                    return true;
                }
                
                cs.sendMessage("POLL #" + strings[1] + ": " + obj.getName());
                Iterator temp = obj.getKeys().iterator();
                int val;
                while(temp.hasNext()) {
                    String o = (String) temp.next();
                    val = obj.getVotesFor(o);
                    cs.sendMessage(o + ": " + Integer.toString(val));
                }
                
                cs.sendMessage("Total votes: " + obj.getTotalVotes());
            }
        }
        else if(option.equalsIgnoreCase("remove")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(strings.length != 2) { // there are not two args
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll remove <pollID>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if(obj != null) {
                plugin.polls.remove(obj);
                obj = null;
                cs.sendMessage(ChatColor.GREEN + "Successfully removed poll!");
                return true;
            }
            else {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
        }
        else if(option.equalsIgnoreCase("vote")) {
            if(!(cs instanceof Player)) {
                cs.sendMessage("Only players can vote in polls.");
                return true;
            }
            
            if (!(cs.hasPermission("SimplePoll.vote"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(strings.length <= 2) { // there are not two args
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll vote <pollID> <Option>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if(obj == null) { // non-existant
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            boolean outcome = obj.voteFor((Player) cs, convertArrayToString(strings, 2));
            if (!outcome) {
                if (obj.hasVoted((Player) cs)) {
                    cs.sendMessage(ChatColor.RED + "You've already voted on this "
                            + "poll!");
                }
                else {
                    cs.sendMessage(ChatColor.RED + "That poll option does not "
                            + "exist!");
                }
            }
            else {
                cs.sendMessage(ChatColor.GREEN + "Vote added successfully!");
            }
            
        }
        else {
            cs.sendMessage(ChatColor.RED + "Unknown option! Use \"/simplepoll help\" "
                    + "for help.");
        }
        return true;
    }
    
    private Poll matchPoll(String val) {
        int num;
        Poll obj;
        
        try {
            num = Integer.parseInt(val);
            obj = plugin.polls.get(num);
        }
        catch (NumberFormatException e) {
            return null;
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
        return obj;
    }
    
    private String convertArrayToString(String[] input, int toSkip) {
        String string = "";
        for (int i = 0; i < input.length; i++) {
            if (i < toSkip) {
                continue;
            }
            
            string += input[i] + " ";
        }
        
        return string.trim();
    }
    
    private void sendSimplePollHelp(CommandSender cs) {
        cs.sendMessage("/simplepoll create <Description> - Create a poll.");
        cs.sendMessage("/simplepoll remove <pollID> - Remove a poll.");
        cs.sendMessage("/simplepoll addoption <pollID> <Option Name> - Add a "
                + "votable option to a poll.");
        cs.sendMessage("/simplepoll remoption <pollID> <Option Name> - Remove a "
                + "votable option from a poll.");
        cs.sendMessage("/simplepoll info <pollID> (Can be used with arg "
                + "to list all polls.");
        cs.sendMessage("/simplepoll help - Display this help.");
        if (cs instanceof Player) { // Non-players can't vote.
            cs.sendMessage("/simplepoll vote <pollID> <Option> - Vote for an option "
                    + "of a poll.");
        }
    }
}
