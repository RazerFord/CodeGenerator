package org.codegenerator;

import org.codegenerator.generator.BuilderGenerator;
import org.codegenerator.resourcescodegeneratorbuilder.User;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeGeneratorBuilderTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    public void userBuilderTest() {
        final String generatedClassName = "GeneratedUserClass";

        BuilderGenerator<User> builderGenerator = new BuilderGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = User.builder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builderGenerator.generate(user, Paths.get(OUTPUT_DIRECTORY));

        User that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    public <R> R createObject(String generatedClassName) {
        return generatedCodeCompiler.createObject(generatedClassName);
    }
}
