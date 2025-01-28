package org.openhab.binding.giraone.internal.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResourceLoader {

    public String loadStringResource(final String name) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(name)) {
            assert inputStream != null;
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject loadJsonResource(final String name) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(name)) {
            Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            return (JsonObject) JsonParser.parseReader(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
