package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.generator.methodsequencefinders.internal.ArrayMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.PrimitiveMethodSequenceFinder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinTypesTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    void integerTypeTest() throws IOException {
        final String generatedClassName = "GeneratedIntegerClass";
        POJOGenerator<Integer> generator = new POJOGenerator<>(Integer.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(Integer.class, new PrimitiveMethodSequenceFinder());

        Integer i = 12;
        generator.generateCode(i, Paths.get(OUTPUT_DIRECTORY));

        Integer other = createObject(generatedClassName);
        assertEquals(i, other);
    }

    @Test
    void stringTypeTest() throws IOException {
        final String generatedClassName = "GeneratedStringClass";
        POJOGenerator<String> generator = new POJOGenerator<>(String.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(String.class, new PrimitiveMethodSequenceFinder());

        String str = "Hello, world!";
        generator.generateCode(str, Paths.get(OUTPUT_DIRECTORY));

        String other = createObject(generatedClassName);
        assertEquals(str, other);
    }

    @Test
    void arrayTest() throws IOException {
        final String generatedClassName = "GeneratedArrayClass";
        POJOGenerator<String[]> generator = new POJOGenerator<>(String[].class, PACKAGE_NAME, generatedClassName, METHOD_NAME);
        generator.registerFinder(String[].class, new ArrayMethodSequenceFinder());

        String[] strings = {"Hello", ",", "world!"};
        generator.generateCode(strings, Paths.get(OUTPUT_DIRECTORY));

        String[] other = createObject(generatedClassName);
        assertArrayEquals(strings, other);
    }

    <R> R createObject(String generatedClassName) {
        return generatedCodeCompiler.createObject(generatedClassName);
    }
}
