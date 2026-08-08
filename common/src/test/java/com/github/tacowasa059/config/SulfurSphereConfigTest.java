package com.github.tacowasa059.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SulfurSphereConfigTest {

    @TempDir
    Path directory;

    @Test
    void missingFileFallsBackToTheDefault() {
        assertEquals(SulfurSphereConfig.DEFAULT_ROLL, SulfurSphereConfig.read(directory.resolve("absent.json")));
    }

    @Test
    void nullPathFallsBackToTheDefault() {
        assertEquals(SulfurSphereConfig.DEFAULT_ROLL, SulfurSphereConfig.read(null));
    }

    @Test
    void directoryInsteadOfFileFallsBackToTheDefault() {
        assertEquals(SulfurSphereConfig.DEFAULT_ROLL, SulfurSphereConfig.read(directory));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",                       // empty file
            "not json at all",        // broken syntax
            "[]",                     // not an object
            "{}",                     // option missing
            "{\"roll\": \"false\"}",  // wrong type
            "{\"roll\": null}",
            "{\"unrelated\": false}", // unknown option only
    })
    void unusableContentFallsBackToTheDefault(String content) throws IOException {
        Path file = write(content);

        assertEquals(SulfurSphereConfig.DEFAULT_ROLL, SulfurSphereConfig.read(file));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void readsTheStoredValue(boolean value) throws IOException {
        Path file = write("{\"roll\": " + value + "}");

        assertEquals(value, SulfurSphereConfig.read(file));
    }

    @Test
    void unknownOptionsAreIgnored() throws IOException {
        Path file = write("{\"roll\": false, \"somethingElse\": 42}");

        assertFalse(SulfurSphereConfig.read(file));
    }

    @Test
    void writtenValuesAreReadBack() {
        Path file = directory.resolve("sulfursphere.json");

        SulfurSphereConfig.write(file, false);

        assertTrue(Files.isRegularFile(file));
        assertFalse(SulfurSphereConfig.read(file));
    }

    @Test
    void writingCreatesMissingDirectories() {
        Path file = directory.resolve("nested").resolve("deeper").resolve("sulfursphere.json");

        SulfurSphereConfig.write(file, true);

        assertTrue(Files.isRegularFile(file));
        assertTrue(SulfurSphereConfig.read(file));
    }

    @Test
    void loadingCreatesTheFileWithDefaults() {
        Path file = directory.resolve("created.json");

        SulfurSphereConfig.loadFrom(file);

        assertTrue(Files.isRegularFile(file));
        assertEquals(SulfurSphereConfig.DEFAULT_ROLL, SulfurSphereConfig.roll());
    }

    @Test
    void settingTheOptionPersistsIt() {
        Path file = directory.resolve("persisted.json");
        SulfurSphereConfig.loadFrom(file);

        SulfurSphereConfig.setRoll(false);

        assertFalse(SulfurSphereConfig.roll());
        assertFalse(SulfurSphereConfig.read(file));

        SulfurSphereConfig.loadFrom(file);
        assertFalse(SulfurSphereConfig.roll());
    }

    private Path write(String content) throws IOException {
        Path file = directory.resolve("sulfursphere.json");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}
