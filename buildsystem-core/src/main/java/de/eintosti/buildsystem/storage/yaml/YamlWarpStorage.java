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
package de.eintosti.buildsystem.storage.yaml;

import de.eintosti.buildsystem.BuildSystemPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class YamlWarpStorage extends AbstractYamlStorage {

    private static final String WARPS_KEY = "warps";

    public YamlWarpStorage(BuildSystemPlugin plugin) {
        super(plugin, "warps.yml");
    }

    public void saveWarps(Map<String, WarpData> warps) {
        FileConfiguration file = getFile();
        if (file == null) {
            return;
        }

        file.set(WARPS_KEY, null);
        warps.forEach((name, warp) -> {
            String path = WARPS_KEY + "." + name;
            file.set(path + ".world", warp.worldName());
            file.set(path + ".x", warp.x());
            file.set(path + ".y", warp.y());
            file.set(path + ".z", warp.z());
            file.set(path + ".yaw", warp.yaw());
            file.set(path + ".pitch", warp.pitch());
        });
        saveFile();
    }

    public Map<String, WarpData> loadWarps() {
        FileConfiguration file = getFile();
        if (file == null) {
            return Map.of();
        }

        ConfigurationSection section = file.getConfigurationSection(WARPS_KEY);
        if (section == null) {
            return Map.of();
        }

        Map<String, WarpData> warps = new HashMap<>();
        for (String name : section.getKeys(false)) {
            String path = WARPS_KEY + "." + name;
            String world = file.getString(path + ".world");
            if (world == null || world.isBlank()) {
                continue;
            }

            double x = file.getDouble(path + ".x");
            double y = file.getDouble(path + ".y");
            double z = file.getDouble(path + ".z");
            float yaw = (float) file.getDouble(path + ".yaw");
            float pitch = (float) file.getDouble(path + ".pitch");
            warps.put(name, new WarpData(world, x, y, z, yaw, pitch));
        }
        return warps;
    }

    @NullMarked
    public static class WarpData {
        private final String worldName;
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;

        public WarpData(String worldName, double x, double y, double z, float yaw, float pitch) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public static WarpData fromLocation(Location location) {
            if (location.getWorld() == null) {
                throw new IllegalArgumentException("Location world is null");
            }
            return new WarpData(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );
        }

        public String worldName() {
            return worldName;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }

        public float yaw() {
            return yaw;
        }

        public float pitch() {
            return pitch;
        }
    }
}


