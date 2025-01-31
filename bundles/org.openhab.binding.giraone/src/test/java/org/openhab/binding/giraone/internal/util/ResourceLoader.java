package org.openhab.binding.giraone.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class ResourceLoader {

    public static String loadStringResource(final String name) {
        try (InputStream inputStream = ResourceLoader.class.getResourceAsStream(name)) {
            assert inputStream != null;
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonObject loadJsonResource(final String name) {
        try (InputStream inputStream = ResourceLoader.class.getResourceAsStream(name)) {
            Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            return (JsonObject) JsonParser.parseReader(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
