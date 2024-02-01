package org.codegenerator;

import org.codegenerator.generator.BuilderGenerator;
import org.codegenerator.resourcescodegeneratorbuilder.*;
import org.codegenerator.resourcescodegeneratorbuilder.otherpackage.UserBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorBuilderTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    void userBuilderTest() throws IOException {
        final String generatedClassName = "GeneratedUserClass";

        BuilderGenerator<User> builderGenerator = new BuilderGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = User.builder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderDefaultTest() throws IOException {
        final String generatedClassName = "GeneratedUserDefaultClass";

        BuilderGenerator<User> builderGenerator = new BuilderGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = User.builder().build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderWithConstructorTest() throws IOException {
        final String generatedClassName = "GeneratedUserBuilderWithConstructorClass";

        BuilderGenerator<UserBuilderWithConstructor> builderGenerator = new BuilderGenerator<>(UserBuilderWithConstructor.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserBuilderWithConstructor user = new UserBuilderWithConstructor.UserBuilder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserBuilderWithConstructor that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderWithConstructorDefaultTest() throws IOException {
        final String generatedClassName = "GeneratedUserBuilderWithConstructorDefaultClass";

        BuilderGenerator<UserBuilderWithConstructor> builderGenerator = new BuilderGenerator<>(UserBuilderWithConstructor.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserBuilderWithConstructor user = new UserBuilderWithConstructor.UserBuilder().build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserBuilderWithConstructor that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userWithDefectTest() throws IOException {
        final String generatedClassName = "GeneratedUserWithDefectClass";

        BuilderGenerator<UserWithDefect> builderGenerator = new BuilderGenerator<>(UserWithDefect.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserWithDefect.UserBuilder userBuilder = UserWithDefect.builder()
                .name("John Doe");
        userBuilder.age(17);
        userBuilder.created(42);
        UserWithDefect user = userBuilder.build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserWithDefect that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void fieldsCanBeCreatedUsingBuildersTest() throws IOException {
        final String generatedClassName = "GeneratedFieldsCanBeCreatedUsingBuildersClass";

        BuilderGenerator<SendingMoneyTransfer> builderGenerator = new BuilderGenerator<>(SendingMoneyTransfer.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User userFrom = User.builder().created(102).age(18).name("John Doe").coins(new long[]{10, 20, 30}).build();
        User userTo = User.builder().created(56).age(42).name("Gordon Freeman").build();
        SendingMoneyTransfer sendingMoneyTransfer = SendingMoneyTransfer.builder().setFrom(userFrom).setTo(userTo).setAmount(100).build();

        builderGenerator.generateCode(sendingMoneyTransfer, Paths.get(OUTPUT_DIRECTORY));

        SendingMoneyTransfer that = createObject(generatedClassName);
        assertEquals(sendingMoneyTransfer, that);
    }

    @Test
    void fieldsCanBeCreatedUsingBuildersWithPojoTest() throws IOException {
        final String generatedClassName = "GeneratedFieldsCanBeCreatedUsingBuildersWithPojoClass";

        BuilderGenerator<SendingMoneyTransferWithPojo> builderGenerator = new BuilderGenerator<>(SendingMoneyTransferWithPojo.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User userFrom = User.builder().created(102).age(18).name("John Doe").build();
        UserPojo userTo = new UserPojo("Gordon Freeman", 56, 42);
        SendingMoneyTransferWithPojo sendingMoneyTransfer = SendingMoneyTransferWithPojo.builder().setFrom(userFrom).setTo(userTo).setAmount(100).build();

        builderGenerator.generateCode(sendingMoneyTransfer, Paths.get(OUTPUT_DIRECTORY));

        SendingMoneyTransferWithPojo that = createObject(generatedClassName);
        assertEquals(sendingMoneyTransfer, that);
    }

    @Test
    void builderInAnotherPackageTest() throws IOException {
        final String generatedClassName = "GeneratedBuilderInAnotherPackageClass";

        BuilderGenerator<UserWithBuilderInAnotherPackage> builderGenerator = new BuilderGenerator<>(UserWithBuilderInAnotherPackage.class, PACKAGE_NAME, generatedClassName, METHOD_NAME, UserBuilder.class);

        UserWithBuilderInAnotherPackage user = new UserBuilder().created(102).age(18).name("John Doe").build();

        builderGenerator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserWithBuilderInAnotherPackage that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    <R> R createObject(String generatedClassName) {
        return generatedCodeCompiler.createObject(generatedClassName);
    }
}
