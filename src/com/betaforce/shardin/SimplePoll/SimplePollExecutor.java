package com.betaforce.shardin.SimplePoll;

import java.util.Iterator;
import java.util.Set;
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
            cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll <Option> <Description>\"");
            return true;
        }
        String option = strings[0];
        if(option.equalsIgnoreCase("create")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            Poll obj;
            String description = (((strings.length - 1) > 0) ? 
                    convertArrayToString(strings, 1) : 
                    ("NewPoll" + Integer.toString(plugin.polls.size())));
            
            obj = new Poll(plugin, description);
            plugin.polls.add(obj);
            if (plugin.getConfig().getBoolean("defaults.public-on-create", false) == false) {
                obj.setHidden(true);
                cs.sendMessage(ChatColor.GREEN + "The poll was created and automatically "
                        + "set to be hidden.");
            }
            else {
                String name = (cs instanceof Player) ? (cs.getName()) : 
                        (cs instanceof ConsoleCommandSender) ? "Console" : "Unknown";
                announcePoll(name + " has just created a new poll! Type \"/simplepoll info\" "
                        + "to see it!");
            }
        }
        else if(option.equalsIgnoreCase("hide")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(strings.length != 2) {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll hide <pollID>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            if (!obj.isHidden()) {
                obj.setHidden(true);
                cs.sendMessage(ChatColor.GREEN + "Poll is now hidden.");
            }
            else {
                cs.sendMessage(ChatColor.RED + "Poll is already hidden.");
            }
        }
        else if(option.equalsIgnoreCase("unhide")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(strings.length != 2) {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll unhide <pollID>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            if (obj.isHidden()) {
                obj.setHidden(false);
                cs.sendMessage(ChatColor.GREEN + "Poll is no longer hidden.");
            }
            else {
                cs.sendMessage(ChatColor.RED + "Poll is already visible.");
            }
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
            
            String test = findOption(strings, obj);
            boolean outcome = obj.remVoteOption(test);
            if (!outcome) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll option!");
            }
            else {
                cs.sendMessage(ChatColor.GREEN + "Successfully removed vote option.");
            }
        }
        else if(option.equalsIgnoreCase("help")) {
            sendSimplePollHelp(cs);
        }
        else if(option.equalsIgnoreCase("reminder")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permision to do that!");
            }
            
            String name = (cs instanceof Player) ? (cs.getName()) : 
                    (cs instanceof ConsoleCommandSender) ? "Console" : "Unknown";
            if(strings.length < 2) {
                announcePoll(name + " wants you to vote! Type /simplepoll info "
                        + "to check which polls you have not voted on!");
            }
            else if(strings.length == 2) {
                Poll obj = matchPoll(strings[1]);
                if (obj == null) {
                    cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                    return true;
                }
                
                if (obj.isHidden()) {
                    if (canAccessPoll(cs, obj)) {
                        cs.sendMessage(ChatColor.RED + "You cannot vote on that "
                                + "poll because it is hidden!");
                    }
                    else { // don't let people know that the hidden poll exists.
                        cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                    }
                    return true;
                }
                
                remindOfPoll(obj, name + " would like to remind you to vote for "
                        + "this poll: " + obj.getName());
            }
            else {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll reminder "
                        + "<pollID>");
            }
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
                String voted, hidden;
                for(int i = 0; i < plugin.polls.size(); i++) {
                    obj = plugin.polls.get(i);
                    if (!canAccessPoll(cs, obj)) {
                        continue; // skip it, it's hidden and they aren't allowed to see.
                    }
                    
                    voted = ((cs instanceof ConsoleCommandSender) ? "N/A" : (obj.hasVoted((Player) cs) && cs instanceof Player) ? "Yes" : "No");
                    if (cs.hasPermission("SimplePoll.create")) {
                        hidden = ((obj.isHidden()) ? ("Yes") : ("No"));
                        cs.sendMessage(i + ":    " + obj.getName() + "    " + voted 
                                + "    " + "HIDDEN: " + hidden);
                    }
                    else {
                        cs.sendMessage(i + ":    " + obj.getName() + "    " + voted);
                    }
                }
            }
            else if (strings.length == 2) {
                Poll obj = matchPoll(strings[1]);
                if (obj == null || !canAccessPoll(cs, obj)) {
                    cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                    return true;
                }
                
                cs.sendMessage("POLL #" + strings[1] + ": " + obj.getName());
                Iterator temp = obj.getKeys().iterator();
                int val, i = 1;
                while(temp.hasNext()) {
                    String o = (String) temp.next();
                    val = obj.getNumVotesFor(o);
                    cs.sendMessage(String.valueOf(i) + " " + o + ": " + Integer.toString(val));
                    i++;
                }
                
                cs.sendMessage("Total votes: " + obj.getTotalVotes());
            }
            else {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll info <pollID>");
            }
        }
        else if(option.equalsIgnoreCase("optioninfo")) {
            if (!(cs.hasPermission("SimplePoll.create"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if(plugin.polls.size() < 1) {
                cs.sendMessage("There are no polls right now.");
                return true;
            }
            
            if (strings.length <= 2) {
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll optioninfo <pollID> <Option Name>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null) {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            String test = findOption(strings, obj);
            if (test.equals("")) {
                cs.sendMessage(ChatColor.RED + "Option does not exist!");
                return true;
            }
            
            Set votes = obj.getVotesFor(test);
            if (votes.size() < 1) {
                cs.sendMessage("No one has voted for this option yet.");
                return true;
            }
            
            cs.sendMessage("The following people have voted for this option: ");
            
            Iterator iter = votes.iterator();
            int count = 0;
            String temp = "", name;
            while(iter.hasNext()) {
                name = (String) iter.next();
                temp += name + " ";
                count++;
                if (count == 10) {
                    cs.sendMessage(temp);
                    temp = "";
                    count = 0;
                }
            }
            
            if (count != 0) {
                cs.sendMessage(temp); // send the last little bit
            }
            
            return true;
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
                cs.sendMessage(ChatColor.GREEN + "Successfully removed poll!");
                return true;
            }
            else {
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
        }
        else if(option.equalsIgnoreCase("vote")) {
            if (!(cs instanceof Player)) { // Only players can vote.
                cs.sendMessage("Only players can vote in polls.");
                return true;
            }
            
            if (!(cs.hasPermission("SimplePoll.vote"))) {
                cs.sendMessage("You don't have permission to do that!");
                return true;
            }
            
            if (strings.length <= 2) { // there are not two args
                cs.sendMessage(ChatColor.RED + "Usage: \"/simplepoll vote <pollID> <Option>\"");
                return true;
            }
            
            Poll obj = matchPoll(strings[1]);
            if (obj == null || !canAccessPoll(cs, obj)) { // non-existant
                cs.sendMessage(ChatColor.RED + "Cannot find that poll!");
                return true;
            }
            
            String chosen = findOption(strings, obj);
            if (chosen.equals("")) {
                cs.sendMessage(ChatColor.RED + "Option does not exist.");
                return true;
            }
            boolean outcome = obj.voteFor((Player) cs, chosen);
            if (!outcome) { // Didn't work.
                if (obj.hasVoted((Player) cs)) { // They've voted on this before.
                    outcome = obj.changeVoteFor((Player) cs, chosen);
                    if(!outcome) { // Couldn't change their vote.
                        cs.sendMessage(ChatColor.RED + "Could not change your "
                                + "vote on this poll. Does the option actually "
                                + "exist?");
                    }
                    else {
                        cs.sendMessage(ChatColor.GREEN + "Vote changed successfully!");
                    }
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
    
    // Can a certain user modify a poll?
    private boolean canAccessPoll(CommandSender plyr, Poll obj) {
        return (plyr.hasPermission("SimplePoll.create") || !obj.isHidden());
    }
    
    // This function matches a number to its poll.
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
    
    // This function converts an array to a string.
    // It assumes that each array element in a single word.
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
        cs.sendMessage("/simplepoll info <pollID> (Can be used without arg "
                + "to list all polls.)");
        cs.sendMessage("/simplepoll hide <pollID> - Hide a poll.");
        cs.sendMessage("/simplepoll unhide <pollID> - Make a hidden poll visible.");
        cs.sendMessage("/simplepoll optioninfo <pollID> <Option Name> - Get more "
                + "information about who has voted on a certain option of a poll.");
        cs.sendMessage("/simplepoll help - Display this help.");
        cs.sendMessage("/simplepoll reminder <pollID> - Reminds everyone to vote. "
                + "(Supplying a pollID reminds those who haven't voted for that poll.)");
        if (cs instanceof Player) { // Non-players can't vote.
            cs.sendMessage("/simplepoll vote <pollID> <Option> - Vote for an option "
                    + "of a poll. (Can also be used to change your vote.)");
        }
    }
    
    private void announcePoll(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("SimplePoll.vote")) { // only announce to people that can vote
                player.sendMessage(ChatColor.GREEN + message);
            }
        }
    }
    
    private void remindOfPoll(Poll obj, String msg) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("SimplePoll.vote") && !obj.hasVoted(player)) {
                player.sendMessage(ChatColor.GREEN + msg);
            }
        }
    }
    
    private String findOption(String[] strings, Poll obj) {
        if (strings.length <= 3) {
            try {
                int val = Integer.parseInt(strings[2]);
                return obj.getOptionByNumber(val);
            }
            catch (NumberFormatException e) {
                // Do nothing.
            }
        }
        
        return convertArrayToString(strings, 2);
    }
}
