package org.codegenerator;

import org.codegenerator.generator.Generator;
import org.codegenerator.generator.Generators;
import org.codegenerator.generator.methodsequencefinders.internal.ArrayMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.PrimitiveMethodSequenceFinder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinTypesTest {
    @Test
    void integerTypeTest() throws IOException {
        final String generatedClassName = "GeneratedIntegerClass";
        Generator generator = Generators.standart(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(Integer.class, new PrimitiveMethodSequenceFinder());

        Integer i = 12;
        generator.generateCode(i, Paths.get(OUTPUT_DIRECTORY));

        Integer other = createObject(generatedClassName);
        assertEquals(i, other);
    }

    @Test
    void stringTypeTest() throws IOException {
        final String generatedClassName = "GeneratedStringClass";
        Generator generator = Generators.standart(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(String.class, new PrimitiveMethodSequenceFinder());

        String str = "Hello, world!";
        generator.generateCode(str, Paths.get(OUTPUT_DIRECTORY));

        String other = createObject(generatedClassName);
        assertEquals(str, other);
    }

    @Test
    void arrayTest() throws IOException {
        final String generatedClassName = "GeneratedArrayClass";
        Generator generator = Generators.standart(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(String[].class, new ArrayMethodSequenceFinder());

        String[] strings = {"Hello", ",", "world!"};
        generator.generateCode(strings, Paths.get(OUTPUT_DIRECTORY));

        String[] other = createObject(generatedClassName);
        assertArrayEquals(strings, other);
    }
}
