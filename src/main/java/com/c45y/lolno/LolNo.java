package com.c45y.lolno;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LolNo extends JavaPlugin {
    // Some toggle variables.

    public boolean chat_enabled;
    public boolean command_enabled;
    public boolean join_enabled;
    public boolean block_enabled;
    public String mode;
    public List<String> mutedUsers = new ArrayList<String>();
    public List<String> allowedCommands;
    public List<String> blockedCommands;
    private final LolNoHandle loglistener = new LolNoHandle(this);
    Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(loglistener, this);
        loadConfig();
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("LolNo.admin") || sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("lolno")) {
                if (args.length == 1) {
                    if (args[0].equals("reload")) {
                        reloadConfig();
                        loadConfig();
                        sender.sendMessage("[LolNo] Reloaded");
                        return true;
                    }
                    if (args[0].equals("status")) {
                        printStatus(sender);
                        return true;
                    }
                    if (args[0].equals("help")) {
                        printHelp(sender);
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        loadConfig();
                    }
                    if (args[0].equals("chat")) {
                        chat_enabled = !chat_enabled;
                        toggleConfig("LolNo.blocks.chat");
                        printStatus(sender);
                        return true;
                    }
                    if (args[0].equals("part")) {
                        join_enabled = !join_enabled;
                        toggleConfig("LolNo.blocks.part");
                        printStatus(sender);
                        return true;
                    }
                    if (args[0].equals("blocks")) {
                        block_enabled = !block_enabled;
                        toggleConfig("LolNo.blocks.block");
                        printStatus(sender);
                        return true;
                    }
                    if (args[0].equals("commands")) {
                        command_enabled = !command_enabled;
                        toggleConfig("LolNo.blocks.command");
                        printStatus(sender);
                        return true;
                    }
                }
            }
        }
        if (sender.hasPermission("lolno.mod") || sender.isOp()) {
            if (cmd.getName().equalsIgnoreCase("mutedlist")) {
                sender.sendMessage("Muted users:");
                for (String player : mutedUsers) {
                    sender.sendMessage(ChatColor.GRAY + "  - " + player);
                }
            }
            if (cmd.getName().equalsIgnoreCase("mute")) {
                if (args.length == 1) {
                    Player mutee = getServer().getPlayer(args[0]);
                    if (mutee != null) {
                        String muteeName = mutee.getName().toLowerCase();
                        if (!mutedUsers.contains(muteeName) && mutee.isOnline()) {
                            addMuteUser(muteeName);
                            mutee.sendMessage(ChatColor.AQUA + "You have been muted by a member of staff.");
                            messageStaff(ChatColor.AQUA + sender.getName() + " has muted " + mutee.getName());
                        }
                        return true;
                    }
                }
            }
            if (cmd.getName().equalsIgnoreCase("unmute")) {
                if (args.length == 1) {
                    String muteeName = args[0].toLowerCase();

                    if (mutedUsers.contains(muteeName)) {
                        removeMuteUser(muteeName);
                        Player mutee = getServer().getPlayer(muteeName);
                        if (mutee != null) {
                            if (mutee instanceof Player) {
                                ((Player) mutee).sendMessage(ChatColor.AQUA + "You have been unmuted by a member of staff.");
                            }
                        }

                        messageStaff(ChatColor.AQUA + sender.getName() + " has unmuted " + muteeName);
                    } else {
                        sender.sendMessage(ChatColor.AQUA + "Cannot find player");
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private void addMuteUser(String player) {
        mutedUsers.add(player.toLowerCase());
        getConfig().set("muted.users", mutedUsers.toArray());
        saveConfig();
    }

    private void removeMuteUser(String player) {
        if (mutedUsers.contains(player.toLowerCase())) {
            mutedUsers.remove(player.toLowerCase());
            getConfig().set("muted.users", mutedUsers.toArray());
        }
        saveConfig();
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("LolNo.blocks.chat", false);
        getConfig().addDefault("LolNo.blocks.command", false);
        getConfig().addDefault("LolNo.blocks.part", false);
        getConfig().addDefault("LolNo.blocks.block", false);
        getConfig().addDefault("LolNo.mode", "blacklist");
        getConfig().addDefault("muted.allow.commands", new String[]{"modreq", "message", "help", "tell"});
        getConfig().addDefault("muted.block.commands", new String[]{""});

        mutedUsers.clear();
        mutedUsers.addAll(getConfig().getStringList("muted.users"));

        chat_enabled = getConfig().getBoolean("LolNo.blocks.chat");
        command_enabled = getConfig().getBoolean("LolNo.blocks.command");
        join_enabled = getConfig().getBoolean("LolNo.blocks.part");
        block_enabled = getConfig().getBoolean("LolNo.blocks.block");
        mode = getConfig().getString("LolNo.mode");
        allowedCommands = getConfig().getStringList("muted.allow.commands");
        blockedCommands = getConfig().getStringList("muted.block.commands");
    }

    private void toggleConfig(String node) {
        getConfig().set(node, !getConfig().getBoolean(node));
        saveConfig();
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Usage: /lolno [param]:");
        sender.sendMessage(ChatColor.GRAY + "    chat - Disable all chat messages.");
        sender.sendMessage(ChatColor.GRAY + "    commands - Disable the use of all commands for non staff.");
        sender.sendMessage(ChatColor.GRAY + "    part - Disable all join/part messages.");
        sender.sendMessage(ChatColor.GRAY + "    blocks - Disable all block creation/destruction.");
        sender.sendMessage(ChatColor.GRAY + "    status - Show the current status of lockdown.");
        sender.sendMessage(ChatColor.GRAY + "    reload - Reload the config.");
    }

    public void printStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Lockdown status:");
        sender.sendMessage(ChatColor.GRAY + "    Commands: " + (command_enabled ? "blocked" : "allowed"));
        sender.sendMessage(ChatColor.GRAY + "    Chat: " + (chat_enabled ? "blocked" : "allowed"));
        sender.sendMessage(ChatColor.GRAY + "    Part: " + (join_enabled ? "blocked" : "allowed"));
        sender.sendMessage(ChatColor.GRAY + "    Blocks: " + (block_enabled ? "blocked" : "allowed"));
    }

    public void messageStaff(String message) {
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("lolno.mod")) {
                p.sendMessage(message);
            }
        }
    }
}