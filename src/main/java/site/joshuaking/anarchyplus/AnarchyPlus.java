package site.joshuaking.anarchyplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerToggleGlideEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AnarchyPlus extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = ChatColor.DARK_GRAY + "Anarchy+";
    private static final String CONFIRM_TITLE = ChatColor.DARK_RED + "Confirm action";
    private static final int SIZE = 27;
    private final Map<UUID, Boolean> pendingRestart = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Anarchy+ enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Anarchy+ disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be used in-game.");
            return true;
        }
        if (!player.isOp() && !player.hasPermission("anarchyplus.admin")) {
            player.sendMessage(color(getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            player.sendMessage(color("&aAnarchy+ configuration reloaded."));
            return true;
        }
        openMenu(player);
        return true;
    }

    private void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, MENU_TITLE);
        inv.setItem(10, item(Material.IRON_SWORD, "&cPvP", state("pvp-enabled"),
                "&7Toggle player-versus-player combat."));
        inv.setItem(12, item(Material.ELYTRA, "&bElytra", state("elytra-enabled"),
                "&7Toggle Elytra use."));
        inv.setItem(14, item(Material.END_CRYSTAL, "&dEnd Crystals", state("end-crystals-enabled"),
                "&7Toggle End Crystal damage."));
        inv.setItem(16, item(Material.BARRIER, "&cNo anti-cheat", "&aNo hack restrictions added",
                "&7Anarchy+ does not block client modifications."));
        inv.setItem(21, item(Material.REDSTONE_BLOCK, "&6Restart server", "&7Broadcast and restart",
                "&7Uses the configured console restart command."));
        inv.setItem(23, item(Material.BARRIER, "&4Stop server", "&7Broadcast and stop",
                "&7Shuts the server down."));
        inv.setItem(26, item(Material.OAK_DOOR, "&fClose", "&7Close this menu"));
        player.openInventory(inv);
    }

    private void openConfirm(Player player, boolean restart) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_TITLE);
        inv.setItem(11, item(Material.LIME_CONCRETE, "&aConfirm", "&7Yes, continue"));
        inv.setItem(15, item(Material.RED_CONCRETE, "&cCancel", "&7Go back"));
        inv.setItem(13, item(Material.PAPER, restart ? "&6Restart server?" : "&4Stop server?",
                "&7All players will be disconnected."));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(MENU_TITLE) && !title.equals(CONFIRM_TITLE)) return;
        event.setCancelled(true);
        if (!player.isOp() && !player.hasPermission("anarchyplus.admin")) {
            player.closeInventory();
            return;
        }
        if (event.getClickedInventory() == null
                || event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getRawSlot();

        if (title.equals(CONFIRM_TITLE)) {
            if (slot == 11) {
                player.closeInventory();
                boolean restart = pendingRestart.remove(player.getUniqueId());
                String key = restart ? "messages.restart" : "messages.stop";
                broadcast(color(getConfig().getString(key)));
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (restart) {
                        String cmd = getConfig().getString("messages.restart-command", "restart");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    } else {
                        Bukkit.shutdown();
                    }
                }, 200L);
            } else if (slot == 15) {
                pendingRestart.remove(player.getUniqueId());
                openMenu(player);
            }
            return;
        }

        switch (slot) {
            case 10 -> toggle(player, "pvp-enabled");
            case 12 -> toggle(player, "elytra-enabled");
            case 14 -> toggle(player, "end-crystals-enabled");
            case 21 -> {
                pendingRestart.put(player.getUniqueId(), true);
                openConfirm(player, true);
                return;
            }
            case 23 -> {
                pendingRestart.put(player.getUniqueId(), false);
                openConfirm(player, false);
                return;
            }
            case 26 -> {
                player.closeInventory();
                return;
            }
            default -> { return; }
        }
        openMenu(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(MENU_TITLE) || title.equals(CONFIRM_TITLE)) event.setCancelled(true);
    }

    private void toggle(Player player, String key) {
        boolean next = !getConfig().getBoolean(key);
        getConfig().set(key, next);
        saveConfig();
        player.sendMessage(color("&a" + key + " set to " + next + "."));
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player
                && !getConfig().getBoolean("pvp-enabled", true)) {
            event.setCancelled(true);
        }
        if (event.getDamager() instanceof EnderCrystal
                && !getConfig().getBoolean("end-crystals-enabled", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlide(PlayerToggleGlideEvent event) {
        if (event.isGliding() && !getConfig().getBoolean("elytra-enabled", true)) {
            event.setCancelled(true);
        }
    }

    private void broadcast(String message) {
        if (message != null && !message.isBlank()) Bukkit.broadcastMessage(message);
    }

    private String state(String key) {
        return getConfig().getBoolean(key, true) ? "&aON" : "&cOFF";
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(java.util.Arrays.stream(lore).map(this::color).toList());
        stack.setItemMeta(meta);
        return stack;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
