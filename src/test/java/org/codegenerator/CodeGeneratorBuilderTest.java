package org.codegenerator;

import org.codegenerator.generator.BuilderGenerator;
import org.codegenerator.resourcescodegeneratorbuilder.User;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class CodeGeneratorBuilderTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";

    @Test
    public void userBuilderTest() {
        final String generatedClassName = "GeneratedUserClass";

        BuilderGenerator<User> builderGenerator = new BuilderGenerator<>(User.class);

        User user = User.builder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builderGenerator.generate(user, Paths.get(OUTPUT_DIRECTORY));
    }
}
