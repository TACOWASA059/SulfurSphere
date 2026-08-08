package com.github.tacowasa059.config;

import com.github.tacowasa059.Constants;
import com.github.tacowasa059.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The mod's client-side configuration, stored as {@code config/sulfursphere.json}.
 *
 * <p>Kept free of Minecraft types so it can be unit tested against a plain {@link Path}.</p>
 */
public final class SulfurSphereConfig {

    /** Rolling is on unless the player turns it off. */
    public static final boolean DEFAULT_ROLL = true;

    static final String ROLL_KEY = "roll";

    private static final String FILE_NAME = Constants.MOD_ID + ".json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile Path file;
    private static volatile boolean roll = DEFAULT_ROLL;

    private SulfurSphereConfig() {
    }

    /** Loads the config from the platform's config directory, writing a default file if there is none. */
    public static void load() {
        loadFrom(Services.PLATFORM.getConfigDirectory().resolve(FILE_NAME));
    }

    /** Whether the sphere rolls while the Sulfur Cube carries a block. */
    public static boolean roll() {
        return roll;
    }

    /** Sets the roll option and writes it back to disk. */
    public static void setRoll(boolean value) {
        roll = value;
        save();
    }

    static void loadFrom(Path configFile) {
        file = configFile;
        roll = read(configFile);
        save();
    }

    /** Reads the roll option, falling back to the default for a missing, empty or broken file. */
    static boolean read(Path configFile) {
        if (configFile == null || !Files.isRegularFile(configFile)) {
            return DEFAULT_ROLL;
        }
        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) {
                return DEFAULT_ROLL;
            }
            JsonElement value = json.get(ROLL_KEY);
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                return DEFAULT_ROLL;
            }
            return value.getAsBoolean();
        } catch (Exception e) {
            Constants.LOG.warn("Failed to read {}, using defaults.", configFile, e);
            return DEFAULT_ROLL;
        }
    }

    static void write(Path configFile, boolean roll) {
        if (configFile == null) {
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(ROLL_KEY, roll);
        try {
            Path parent = configFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            Constants.LOG.warn("Failed to write {}.", configFile, e);
        }
    }

    private static void save() {
        write(file, roll);
    }
}
