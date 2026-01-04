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
package de.eintosti.buildsystem.command.tabcomplete;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.world.WarpManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WarpTabCompleter extends ArgumentSorter implements TabCompleter {

    private final WarpManager warpManager;

    public WarpTabCompleter(BuildSystemPlugin plugin) {
        this.warpManager = plugin.getWarpManager();
        plugin.getCommand("warp").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (!(sender instanceof Player player)) {
            return arrayList;
        }

        switch (args.length) {
            case 1 -> {
                // Subcommands
                if (player.hasPermission("buildsystem.warp.set")) {
                    addArgument(args[0], "set", arrayList);
                }
                if (player.hasPermission("buildsystem.warp.remove")) {
                    addArgument(args[0], "remove", arrayList);
                }
                if (player.hasPermission("buildsystem.warp.list")) {
                    addArgument(args[0], "list", arrayList);
                }

                // Warp names
                if (player.hasPermission("buildsystem.warp")) {
                    warpManager.getWarpNames().forEach(name -> addArgument(args[0], name, arrayList));
                }
                return arrayList;
            }
            case 2 -> {
                String sub = args[0].toLowerCase(Locale.ROOT);
                switch (sub) {
                    case "remove" -> {
                        if (player.hasPermission("buildsystem.warp.remove")) {
                            warpManager.getWarpNames().forEach(name -> addArgument(args[1], name, arrayList));
                        }
                    }
                    default -> {
                        // no suggestions for set <name> (free text)
                    }
                }
                return arrayList;
            }
            default -> {
                return arrayList;
            }
        }
    }
}


