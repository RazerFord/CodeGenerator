package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesgeneric.OneField;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericClassesTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    void withoutSetterForOneFieldTest() throws IOException {
        final String generatedClassName = "WithoutSetterForOneFieldClass";
        POJOGenerator<OneField<Integer>> generator = new POJOGenerator<>(OneField.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneField<Integer> oneField = new OneField<>();
        oneField.setValue(42);

        generator.generateCode(oneField, Paths.get(OUTPUT_DIRECTORY));

        OneField<Integer> other = generatedCodeCompiler.createObject(generatedClassName);

        assertEquals(oneField, other);
    }
}
