package org.openhab.binding.giraone.internal.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;

public class ThingDescriptionSchemaValidationTest {

    private static Stream<Arguments> provideThingDescription() throws IOException {
        String path = Objects.requireNonNull(ThingDescriptionSchemaValidationTest.class.getResource("/OH-INF/thing"))
                .getPath();
        ArrayList<Arguments> arguments = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(path))) {
            stream.filter(Files::isRegularFile).map(Arguments::of).forEach(arguments::add);
        }
        return arguments.stream();
    }

    @DisplayName("Validate thing description against schema files")
    @ParameterizedTest
    @MethodSource("provideThingDescription")
    void shouldValidateThingDescription(Path xmlPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = factory
                    .newSchema(
                            new Source[] {
                                    new StreamSource(
                                            new File(Objects
                                                    .requireNonNull(ThingDescriptionSchemaValidationTest.class
                                                            .getResource("/schemas/thing-description-1.0.0.xsd"))
                                                    .getFile())),
                                    new StreamSource(new File(Objects
                                            .requireNonNull(ThingDescriptionSchemaValidationTest.class
                                                    .getResource("/schemas/config-description-1.0.0.xsd"))
                                            .getFile())) });

            schema.newValidator().validate(new StreamSource(xmlPath.toFile()));
        } catch (IOException | SAXException e) {
            Assertions.fail(
                    String.format("Schema validation for '%s' failed. %s", xmlPath.toFile().getName(), e.getMessage()),
                    e);
        }
    }
}
