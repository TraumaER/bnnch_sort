package xyz.bannach.bnnch_sort.fabric.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import xyz.bannach.bnnch_sort.CommonConfig;
import xyz.bannach.bnnch_sort.ModifierKey;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("bnnch_sort.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) return;
            if (data.showSortButton != null)    CommonConfig.showSortButton    = data.showSortButton;
            if (data.lockModifierKey != null)   CommonConfig.lockModifierKey   = ModifierKey.valueOf(data.lockModifierKey);
            if (data.lockTintColor != null)     CommonConfig.lockTintColor     = parseColor(data.lockTintColor, CommonConfig.lockTintColor);
            if (data.showLockTooltip != null)   CommonConfig.showLockTooltip   = data.showLockTooltip;
            if (data.defaultSortMethod != null) CommonConfig.defaultSortMethod = SortMethod.valueOf(data.defaultSortMethod);
            if (data.defaultSortOrder != null)  CommonConfig.defaultSortOrder  = SortOrder.valueOf(data.defaultSortOrder);
        } catch (IOException | IllegalArgumentException ignored) {
            // Fall back to defaults on any read/parse error
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            ConfigData data = new ConfigData();
            data.showSortButton    = CommonConfig.showSortButton;
            data.lockModifierKey   = CommonConfig.lockModifierKey.name();
            // Store as RGBA hex to match NeoForge's config format
            int argb = CommonConfig.lockTintColor;
            data.lockTintColor = String.format("%02X%02X%02X%02X",
                (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
            data.showLockTooltip   = CommonConfig.showLockTooltip;
            data.defaultSortMethod = CommonConfig.defaultSortMethod.name();
            data.defaultSortOrder  = CommonConfig.defaultSortOrder.name();
            GSON.toJson(data, writer);
        } catch (IOException ignored) {}
    }

    /** Parses a hex RGBA color string and converts it to an ARGB integer. */
    private static int parseColor(String hex, int fallback) {
        try {
            long rgba = Long.parseLong(hex, 16);
            int r = (int) ((rgba >> 24) & 0xFF);
            int g = (int) ((rgba >> 16) & 0xFF);
            int b = (int) ((rgba >> 8) & 0xFF);
            int a = (int) (rgba & 0xFF);
            return (a << 24) | (r << 16) | (g << 8) | b;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static class ConfigData {
        Boolean showSortButton;
        String  lockModifierKey;
        String  lockTintColor;
        Boolean showLockTooltip;
        String  defaultSortMethod;
        String  defaultSortOrder;
    }
}
