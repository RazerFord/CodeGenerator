package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcescodegeneratorpojo.Accumulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReflectionTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    void withoutSetterTest() throws IOException {
        final String generatedClassName = "WithoutSetterTestClass";
        POJOGenerator<Accumulator> generator = new POJOGenerator<>(Accumulator.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Accumulator accumulator = new Accumulator();
        accumulator.setA(12);
        accumulator.setB(24);
        accumulator.calculate();
        accumulator.setB(10);
        generator.generateCode(accumulator, Paths.get(OUTPUT_DIRECTORY));

        Accumulator other = generatedCodeCompiler.createObject(generatedClassName);
        assertEquals(accumulator, other);
    }
}
