package com.nguspawner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class NguSpawnerPlugins extends JavaPlugin
        implements Listener, TabExecutor {

    private NamespacedKey spawnerKey;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        spawnerKey = new NamespacedKey(
                this,
                "ngu_spawner_type"
        );

        getCommand("nguspawner").setExecutor(this);
        getCommand("nguspawner").setTabCompleter(this);

        getServer()
                .getPluginManager()
                .registerEvents(this, this);

        getLogger().info(
                "NguSpawnerPlugins đã bật!"
        );
    }

    @Override
    public void onDisable() {

        getLogger().info(
                "NguSpawnerPlugins đã tắt!"
        );
    }

    // =========================================================
    // COMMAND
    // =========================================================

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission(
                "nguspawner.use"
        )) {

            msg(
                    sender,
                    "&cBạn không có quyền."
            );

            return true;
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help":

                help(sender);
                return true;

            case "list":

                list(sender);
                return true;

            case "give":

                give(sender, args);
                return true;

            case "reload":

                reload(sender);
                return true;

            default:

                msg(
                        sender,
                        "&cLệnh không tồn tại."
                );

                return true;
        }
    }

    private void help(
            CommandSender sender
    ) {

        msg(
                sender,
                "&b&lNguSpawnerPlugins"
        );

        msg(
                sender,
                "&7/nguspawner help"
                        + " &f- Trợ giúp"
        );

        msg(
                sender,
                "&7/nguspawner list"
                        + " &f- Danh sách spawner"
        );

        msg(
                sender,
                "&7/nguspawner give"
                        + " <player> <spawner> [amount]"
                        + " &f- Give spawner"
        );

        if (sender.hasPermission(
                "nguspawner.admin"
        )) {

            msg(
                    sender,
                    "&7/nguspawner reload"
                            + " &f- Reload config"
            );
        }
    }

    // =========================================================
    // GIVE
    // =========================================================

    private void give(
            CommandSender sender,
            String[] args
    ) {

        if (!sender.hasPermission(
                "nguspawner.give"
        )) {

            msg(
                    sender,
                    "&cBạn không có quyền."
            );

            return;
        }

        if (args.length < 3) {

            msg(
                    sender,
                    "&c/nguspawner give"
                            + " <player> <spawner> [amount]"
            );

            return;
        }

        Player target =
                getServer()
                        .getPlayerExact(args[1]);

        if (target == null) {

            msg(
                    sender,
                    "&cKhông tìm thấy player &e"
                            + args[1]
            );

            return;
        }

        String type =
                args[2].toLowerCase();

        if (!isSpawner(type)) {

            msg(
                    sender,
                    "&cSpawner không tồn tại: &e"
                            + type
            );

            return;
        }

        int amount = 1;

        if (args.length >= 4) {

            try {

                amount =
                        Integer.parseInt(args[3]);

            } catch (NumberFormatException e) {

                msg(
                        sender,
                        "&cAmount phải là số."
                );

                return;
            }
        }

        int max =
                getConfig().getInt(
                        "settings.max-give",
                        64
                );

        if (amount < 1 ||
                amount > max) {

            msg(
                    sender,
                    "&cSố lượng phải từ &e1"
                            + " &cđến &e"
                            + max
                            + "&c."
            );

            return;
        }

        ItemStack item =
                createSpawner(
                        type,
                        amount
                );

        if (item == null) {

            msg(
                    sender,
                    "&cKhông thể tạo spawner."
            );

            return;
        }

        Map<Integer, ItemStack> leftover =
                target.getInventory()
                        .addItem(item);

        if (!leftover.isEmpty()) {

            for (ItemStack stack :
                    leftover.values()) {

                target.getWorld()
                        .dropItemNaturally(
                                target.getLocation(),
                                stack
                        );
            }

            msg(
                    sender,
                    "&eInventory đầy, item dư đã được drop."
            );
        }

        msg(
                sender,
                "&aĐã give &e"
                        + amount
                        + "x "
                        + type
                        + " &acho &e"
                        + target.getName()
                        + "&a."
        );

        if (!sender.equals(target)) {

            msg(
                    target,
                    "&aBạn nhận được &e"
                            + amount
                            + "x "
                            + type
                            + " spawner&a."
            );
        }
    }

    // =========================================================
    // LIST
    // =========================================================

    private void list(
            CommandSender sender
    ) {

        msg(
                sender,
                "&b&lNguSpawner"
        );

        List<String> list =
                getConfig()
                        .getConfigurationSection(
                                "spawners"
                        ) == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                getConfig()
                                        .getConfigurationSection(
                                                "spawners"
                                        )
                                        .getKeys(false)
                        );

        Collections.sort(list);

        for (String id : list) {

            if (!isSpawner(id)) {
                continue;
            }

            String name =
                    getConfig().getString(
                            "spawners."
                                    + id
                                    + ".name",
                            id
                    );

            msg(
                    sender,
                    "&7- &e"
                            + id
                            + " &8→ "
                            + name
            );
        }
    }

    // =========================================================
    // RELOAD
    // =========================================================

    private void reload(
            CommandSender sender
    ) {

        if (!sender.hasPermission(
                "nguspawner.admin"
        )) {

            msg(
                    sender,
                    "&cBạn không có quyền."
            );

            return;
        }

        reloadConfig();

        msg(
                sender,
                "&aConfig đã được reload."
        );
    }

    // =========================================================
    // CREATE SPAWNER
    // =========================================================

    private ItemStack createSpawner(
            String type,
            int amount
    ) {

        String path =
                "spawners." + type;

        String name =
                getConfig().getString(
                        path + ".name",
                        "&b"
                                + type
                                + " Spawner"
                );

        List<String> lore =
                getConfig().getStringList(
                        path + ".lore"
                );

        ItemStack item =
                new ItemStack(
                        Material.SPAWNER,
                        amount
                );

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(
                color(name)
        );

        List<String> coloredLore =
                new ArrayList<>();

        for (String line : lore) {

            coloredLore.add(
                    color(line)
            );
        }

        meta.setLore(coloredLore);

        meta.getPersistentDataContainer()
                .set(
                        spawnerKey,
                        PersistentDataType.STRING,
                        type
                );

        item.setItemMeta(meta);

        return item;
    }

    // =========================================================
    // CHECK SPAWNER
    // =========================================================

    private boolean isSpawner(
            String type
    ) {

        String path =
                "spawners." + type;

        if (!getConfig()
                .isConfigurationSection(path)) {

            return false;
        }

        return getConfig().getBoolean(
                path + ".enabled",
                false
        );
    }

    // =========================================================
    // GET SPAWNER TYPE FROM ITEM
    // =========================================================

    private String getSpawnerType(
            ItemStack item
    ) {

        if (item == null ||
                item.getType()
                        != Material.SPAWNER) {

            return null;
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer()
                .get(
                        spawnerKey,
                        PersistentDataType.STRING
                );
    }

    // =========================================================
    // PLACE
    // =========================================================

    @EventHandler
    public void onPlace(
            BlockPlaceEvent event
    ) {

        ItemStack item =
                event.getItemInHand();

        String type =
                getSpawnerType(item);

        if (type == null) {
            return;
        }

        /*
         * Đây là nơi sau này sẽ nối hệ thống
         * Virtual Spawner.
         */

        getLogger().info(
                event.getPlayer().getName()
                        + " placed "
                        + type
                        + " spawner."
        );
    }

    // =========================================================
    // RIGHT CLICK
    // =========================================================

    @EventHandler
    public void onInteract(
            PlayerInteractEvent event
    ) {

        if (!event.hasItem()) {
            return;
        }

        ItemStack item =
                event.getItem();

        String type =
                getSpawnerType(item);

        if (type == null) {
            return;
        }

        /*
         * Hiện tại chưa mở GUI.
         * Sẽ nối GUI Virtual Spawner ở bước tiếp theo.
         */
    }

    // =========================================================
    // TAB COMPLETE
    // =========================================================

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {

            return filter(
                    List.of(
                            "help",
                            "give",
                            "list",
                            "reload"
                    ),
                    args[0]
            );
        }

        if (args.length == 2 &&
                args[0].equalsIgnoreCase("give")) {

            List<String> players =
                    new ArrayList<>();

            for (Player player :
                    getServer()
                            .getOnlinePlayers()) {

                players.add(
                        player.getName()
                );
            }

            return filter(
                    players,
                    args[1]
            );
        }

        if (args.length == 3 &&
                args[0].equalsIgnoreCase("give")) {

            var section =
                    getConfig()
                            .getConfigurationSection(
                                    "spawners"
                            );

            if (section == null) {
                return List.of();
            }

            return filter(
                    new ArrayList<>(
                            section.getKeys(false)
                    ),
                    args[2]
            );
        }

        if (args.length == 4 &&
                args[0].equalsIgnoreCase("give")) {

            return List.of(
                    "1",
                    "2",
                    "5",
                    "10",
                    "16",
                    "32",
                    "64"
            );
        }

        return List.of();
    }

    private List<String> filter(
            List<String> values,
            String input
    ) {

        String lower =
                input.toLowerCase();

        return values.stream()
                .filter(value ->
                        value.toLowerCase()
                                .startsWith(lower)
                )
                .toList();
    }

    // =========================================================
    // MESSAGE
    // =========================================================

    private void msg(
            CommandSender sender,
            String message
    ) {

        String prefix =
                getConfig().getString(
                        "prefix",
                        "&8[&bNguSpawner&8] "
                );

        sender.sendMessage(
                color(prefix + message)
        );
    }

    private String color(
            String text
    ) {

        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
}
