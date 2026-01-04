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
package de.eintosti.buildsystem.world;

import de.eintosti.buildsystem.BuildSystemPlugin;
import de.eintosti.buildsystem.api.storage.WorldStorage;
import de.eintosti.buildsystem.api.world.BuildWorld;
import de.eintosti.buildsystem.storage.yaml.YamlWarpStorage;
import de.eintosti.buildsystem.storage.yaml.YamlWarpStorage.WarpData;
import io.papermc.lib.PaperLib;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class WarpManager {

    private final BuildSystemPlugin plugin;
    private final WorldStorage worldStorage;
    private final YamlWarpStorage warpStorage;

    private final Map<String, WarpData> warps = new HashMap<>();

    public WarpManager(BuildSystemPlugin plugin) {
        this.plugin = plugin;
        this.worldStorage = plugin.getWorldService().getWorldStorage();
        this.warpStorage = new YamlWarpStorage(plugin);
        load();
    }

    public boolean teleport(Player player, String warpName) {
        WarpData warp = warps.get(normalizeName(warpName));
        if (warp == null) {
            return false;
        }

        String worldName = warp.worldName();
        BuildWorld buildWorld = worldStorage.getBuildWorld(worldName);
        if (buildWorld != null && !buildWorld.isLoaded()) {
            buildWorld.getLoader().loadForPlayer(player);
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (buildWorld != null) {
                buildWorld.getLoader().load();
                world = Bukkit.getWorld(worldName);
            }
            if (world == null) {
                return false;
            }
        }

        Location location = new Location(world, warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        player.setFallDistance(0);
        PaperLib.teleportAsync(player, location);
        return true;
    }

    public boolean exists(String warpName) {
        return warps.containsKey(normalizeName(warpName));
    }

    public void set(String warpName, Location location) {
        String name = normalizeName(warpName);
        if (location.getWorld() == null) {
            return;
        }

        warps.put(name, WarpData.fromLocation(location));
        save(); // fire-and-forget (async)
    }

    public void remove(String warpName) {
        String name = normalizeName(warpName);
        warps.remove(name);
        save(); // fire-and-forget (async)
    }

    public List<String> getWarpNames() {
        return warps.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public CompletableFuture<Void> save() {
        Map<String, WarpData> snapshot = new HashMap<>(warps);
        return CompletableFuture.runAsync(() -> warpStorage.saveWarps(snapshot));
    }

    private void load() {
        warps.clear();
        warps.putAll(warpStorage.loadWarps());
    }

    private String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}


