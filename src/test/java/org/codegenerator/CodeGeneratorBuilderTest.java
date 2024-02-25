package org.codegenerator;

import org.codegenerator.generator.BuilderGenerator;
import org.codegenerator.testclasses.codegeneratorbuilder.*;
import org.codegenerator.testclasses.codegeneratorbuilder.otherpackage.UserBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorBuilderTest {
    @Test
    void userBuilderTest() throws IOException {
        final String generatedClassName = "GeneratedUserClass";

        BuilderGenerator<User> builder = new BuilderGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = User.builder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderDefaultTest() throws IOException {
        final String generatedClassName = "GeneratedUserDefaultClass";

        BuilderGenerator<User> builder = new BuilderGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = User.builder().build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderWithConstructorTest() throws IOException {
        final String generatedClassName = "GeneratedUserBuilderWithConstructorClass";

        BuilderGenerator<UserBuilderWithConstructor> builder = new BuilderGenerator<>(UserBuilderWithConstructor.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserBuilderWithConstructor user = new UserBuilderWithConstructor.UserBuilder()
                .created(42)
                .name("John Doe")
                .age(17)
                .build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserBuilderWithConstructor that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userBuilderWithConstructorDefaultTest() throws IOException {
        final String generatedClassName = "GeneratedUserBuilderWithConstructorDefaultClass";

        BuilderGenerator<UserBuilderWithConstructor> builder = new BuilderGenerator<>(UserBuilderWithConstructor.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserBuilderWithConstructor user = new UserBuilderWithConstructor.UserBuilder().build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserBuilderWithConstructor that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void userWithDefectTest() throws IOException {
        final String generatedClassName = "GeneratedUserWithDefectClass";

        BuilderGenerator<UserWithDefect> builder = new BuilderGenerator<>(UserWithDefect.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserWithDefect.UserBuilder userBuilder = UserWithDefect.builder()
                .name("John Doe");
        userBuilder.age(17);
        userBuilder.created(42);
        UserWithDefect user = userBuilder.build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserWithDefect that = createObject(generatedClassName);
        assertEquals(user, that);
    }

    @Test
    void fieldsCanBeCreatedUsingBuildersTest() throws IOException {
        final String generatedClassName = "GeneratedFieldsCanBeCreatedUsingBuildersClass";

        BuilderGenerator<SendingMoneyTransfer> builder = new BuilderGenerator<>(SendingMoneyTransfer.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User userFrom = User.builder().created(102).age(18).name("John Doe").coins(new long[]{10, 20, 30}).build();
        User userTo = User.builder().created(56).age(42).name("Gordon Freeman").build();
        SendingMoneyTransfer sendingMoneyTransfer = SendingMoneyTransfer.builder().setFrom(userFrom).setTo(userTo).setAmount(100).build();

        builder.generateCode(sendingMoneyTransfer, Paths.get(OUTPUT_DIRECTORY));

        SendingMoneyTransfer that = createObject(generatedClassName);
        assertEquals(sendingMoneyTransfer, that);
    }

    @Test
    void fieldsCanBeCreatedUsingBuildersWithPojoTest() throws IOException {
        final String generatedClassName = "GeneratedFieldsCanBeCreatedUsingBuildersWithPojoClass";

        BuilderGenerator<SendingMoneyTransferWithPojo> builder = new BuilderGenerator<>(SendingMoneyTransferWithPojo.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User userFrom = User.builder().created(102).age(18).name("John Doe").build();
        UserPojo userTo = new UserPojo("Gordon Freeman", 56, 42);
        SendingMoneyTransferWithPojo sendingMoneyTransfer = SendingMoneyTransferWithPojo.builder().setFrom(userFrom).setTo(userTo).setAmount(100).build();

        builder.generateCode(sendingMoneyTransfer, Paths.get(OUTPUT_DIRECTORY));

        SendingMoneyTransferWithPojo that = createObject(generatedClassName);
        assertEquals(sendingMoneyTransfer, that);
    }

    @Test
    void builderInAnotherPackageTest() throws IOException {
        final String generatedClassName = "GeneratedBuilderInAnotherPackageClass";

        BuilderGenerator<UserWithBuilderInAnotherPackage> builder = new BuilderGenerator<>(UserWithBuilderInAnotherPackage.class, PACKAGE_NAME, generatedClassName, METHOD_NAME, UserBuilder.class);

        UserWithBuilderInAnotherPackage user = new UserBuilder().created(102).age(18).name("John Doe").build();

        builder.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserWithBuilderInAnotherPackage that = createObject(generatedClassName);
        assertEquals(user, that);
    }
}
