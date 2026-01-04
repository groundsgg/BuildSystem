/*
 * Copyright (c) 2018-2025, Thomas Meaney
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.eintosti.buildsystem.command;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.world.WarpManager;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WarpCommand implements CommandExecutor {

    private final BuildSystemPlugin plugin;
    private final WarpManager warpManager;

    public WarpCommand(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.warpManager = plugin.getWarpManager();
        plugin.getCommand("warp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getLogger().warning(Messages.getString("sender_not_player", sender));
            return true;
        }

        switch (args.length) {
            case 0 -> {
                sendUsage(player);
                return true;
            }
            default -> {
                String sub = args[0].toLowerCase(Locale.ROOT);
                switch (sub) {
                    case "set" -> {
                        if (!player.hasPermission("buildsystem.warp.set")) {
                            Messages.sendPermissionError(player);
                            return true;
                        }
                        if (args.length != 2) {
                            Messages.sendMessage(player, "warp_admin");
                            return true;
                        }

                        String name = args[1];
                        warpManager.set(name, player.getLocation());
                        Messages.sendMessage(player, "warp_set", Map.entry("%warp%", name));
                        return true;
                    }
                    case "remove" -> {
                        if (!player.hasPermission("buildsystem.warp.remove")) {
                            Messages.sendPermissionError(player);
                            return true;
                        }
                        if (args.length != 2) {
                            Messages.sendMessage(player, "warp_admin");
                            return true;
                        }

                        String name = args[1];
                        if (!warpManager.exists(name)) {
                            Messages.sendMessage(player, "warp_not_found", Map.entry("%warp%", name));
                            return true;
                        }
                        warpManager.remove(name);
                        Messages.sendMessage(player, "warp_removed", Map.entry("%warp%", name));
                        return true;
                    }
                    case "list" -> {
                        if (!player.hasPermission("buildsystem.warp.list")) {
                            Messages.sendPermissionError(player);
                            return true;
                        }

                        if (warpManager.getWarpNames().isEmpty()) {
                            Messages.sendMessage(player, "warp_list_empty");
                            return true;
                        }

                        Messages.sendMessage(player, "warp_list_header");
                        warpManager.getWarpNames().forEach(warp ->
                                Messages.sendMessage(player, "warp_list_entry", Map.entry("%warp%", warp))
                        );
                        return true;
                    }
                    default -> {
                        if (!player.hasPermission("buildsystem.warp")) {
                            Messages.sendPermissionError(player);
                            return true;
                        }

                        String name = args[0];
                        if (!warpManager.teleport(player, name)) {
                            Messages.sendMessage(player, "warp_not_found", Map.entry("%warp%", name));
                            return true;
                        }

                        Messages.sendMessage(player, "warp_teleported", Map.entry("%warp%", name));
                        return true;
                    }
                }
            }
        }
    }

    private void sendUsage(Player player) {
        String key = (player.hasPermission("buildsystem.warp.set")
                || player.hasPermission("buildsystem.warp.remove")
                || player.hasPermission("buildsystem.warp.list"))
                ? "warp_admin"
                : "warp_usage";
        Messages.sendMessage(player, key);
    }
}


