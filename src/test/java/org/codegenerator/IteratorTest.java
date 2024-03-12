package org.codegenerator;

import org.codegenerator.generator.Generator;
import org.codegenerator.generator.Generators;
import org.codegenerator.testclasses.codegeneratorbuilder.SendingMoneyTransferWithPojo;
import org.codegenerator.testclasses.codegeneratorbuilder.User;
import org.codegenerator.testclasses.codegeneratorbuilder.UserPojo;
import org.codegenerator.testclasses.codegeneratorpojo.ClassWithManyFieldsComplex;
import org.codegenerator.testclasses.codegeneratorpojo.MultidimensionalPointArray;
import org.codegenerator.testclasses.codegeneratorpojo.Point;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IteratorTest {
    @Test
    void nullTest() throws IOException {
        final String generatedClassName = "GeneratedNullClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        checkIterator(generator, (Boolean) null, generatedClassName);
    }

    @Test
    void integerTest() throws IOException {
        final String generatedClassName = "GeneratedIntClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        int i = 42;

        checkIterator(generator, i, generatedClassName);
    }

    @Test
    void stringTest() throws IOException {
        final String generatedClassName = "GeneratedStringClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        String string = "Hello, world!";

        checkIterator(generator, string, generatedClassName);
    }

    @Test
    void stringArrayTest() throws IOException {
        final String generatedClassName = "GeneratedStringArrayClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        String[][][] array = new String[][][]{
                new String[][]{
                        new String[]{"Hello", ", ", "world", "!"}
                },
                new String[][]{
                        new String[]{"Bonsoir", ",", " ", "Elliot"}
                },
        };

        checkIterator(generator, array, generatedClassName);
    }

    @Test
    void pojoArrayTest() throws IOException {
        final String generatedClassName = "GeneratedPojoArrayClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        Point[][][] arrayOfPojo = CodeGeneratorPOJOTest.getMultidimensionalPointArray().getPoints();

        checkIterator(generator, arrayOfPojo, generatedClassName);
    }

    @Test
    void pojoWithArrayTest() throws IOException {
        final String generatedClassName = "GeneratedPojoWithArrayClass";
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);
        MultidimensionalPointArray multidimensionalPointArray = CodeGeneratorPOJOTest.getMultidimensionalPointArray();

        checkIterator(generator, multidimensionalPointArray, generatedClassName);
    }

    @Test
    void builderWithFourFieldsTest() throws IOException {
        final String generatedClassName = "GeneratedPojoSetterBuilderClass";
        Generator generator = Generators.forBuilder(SendingMoneyTransferWithPojo.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);
        User userFrom = User.builder().created(32874).age(17).name("John Doe").coins(new long[]{5, 5, 5, 5, 5}).build();

        checkIterator(generator, userFrom, generatedClassName);
    }

    @Test
    void builderWithThreeFieldsTest() throws IOException {
        final String generatedClassName = "GeneratedPojoSetterBuilderClass";
        Generator generator = Generators.forBuilder(SendingMoneyTransferWithPojo.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User userFrom = User.builder().created(102).age(18).name("John Doe").build();
        UserPojo userTo = new UserPojo("Gordon Freeman", 56, 42);
        SendingMoneyTransferWithPojo sendingMoneyTransfer = SendingMoneyTransferWithPojo.builder().setFrom(userFrom).setTo(userTo).setAmount(100).build();

        checkIterator(generator, sendingMoneyTransfer, generatedClassName);
    }

    @Test
    void pojoWithManyFieldsTest() throws IOException {
        final String generatedClassName = "GeneratedPojoWithManyFieldsTestClass";
        Generator generator = Generators.forPojo(ClassWithManyFieldsComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithManyFieldsComplex classWithManyFieldsComplex = new ClassWithManyFieldsComplex();
        classWithManyFieldsComplex.setField1("field1");
        classWithManyFieldsComplex.setField2("field2");
        classWithManyFieldsComplex.setField3("field3");
        classWithManyFieldsComplex.setField4("field4");
        classWithManyFieldsComplex.setField5("field5");
        classWithManyFieldsComplex.setField6("field6");
        classWithManyFieldsComplex.setField7("field7");
        classWithManyFieldsComplex.setField8("field8");
        classWithManyFieldsComplex.setField9("field9");
        classWithManyFieldsComplex.setField10("field10");
        classWithManyFieldsComplex.setField11("field11");
        classWithManyFieldsComplex.setField12("field12");
        classWithManyFieldsComplex.setField13("field13");
        classWithManyFieldsComplex.setField14("field14");
        classWithManyFieldsComplex.setField15("field15");
        classWithManyFieldsComplex.setField16("field16");
        classWithManyFieldsComplex.setField17("field17");
        classWithManyFieldsComplex.setField18("field18");
        classWithManyFieldsComplex.setField19("field19");
        classWithManyFieldsComplex.setField20("field20");
        classWithManyFieldsComplex.setField21("field21");
        classWithManyFieldsComplex.setField22("field22");
        classWithManyFieldsComplex.setField23("field23");
        classWithManyFieldsComplex.setField24("field24");
        classWithManyFieldsComplex.setField25("field25");
        classWithManyFieldsComplex.setField26("field26");
        classWithManyFieldsComplex.setField27("field27");
        classWithManyFieldsComplex.setField28("field28");
        classWithManyFieldsComplex.setField29("field29");
        classWithManyFieldsComplex.setField30("field30");
        classWithManyFieldsComplex.setField31("field31");
        classWithManyFieldsComplex.setField32("field32");
        classWithManyFieldsComplex.setField33("field33");

        checkIterator(generator, classWithManyFieldsComplex, generatedClassName);
    }

    private final static String FULL_NAME_NESTED_CLASS = "public static class " + NESTED_CLASS;

    @SuppressWarnings("UnusedReturnValue")
    private static <T> int checkIterator(@NotNull Generator generator, T object, String generatedClassName) throws IOException {
        int iteration = 0;
        String[] nestedClasses;
        String[] nestedClass = new String[]{NESTED_CLASS};
        String[] empty = new String[]{};
        for (String code : generator.generateIterableCode(object)) {
            if (code.contains(FULL_NAME_NESTED_CLASS)) nestedClasses = nestedClass;
            else nestedClasses = empty;

            T that = createObject(code, generatedClassName, nestedClasses);
            assertEquals(object, that);

            iteration++;
        }
        return iteration;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static <T> int checkIterator(@NotNull Generator generator, T[] object, String generatedClassName) throws IOException {
        int iteration = 0;
        String[] nestedClasses;
        String[] nestedClass = new String[]{NESTED_CLASS};
        String[] empty = new String[]{};
        for (String code : generator.generateIterableCode(object)) {
            if (code.contains(FULL_NAME_NESTED_CLASS)) nestedClasses = nestedClass;
            else nestedClasses = empty;

            T[] that = createObject(code, generatedClassName, nestedClasses);
            assertArrayEquals(object, that);

            iteration++;
        }
        return iteration;
    }

    private static <R> R createObject(String code, String className, String... nestedClasses) throws IOException {
        saveCodeToFile(code, className);
        return Common.createObject(className, nestedClasses);
    }

    private static void saveCodeToFile(@NotNull String code, String className) throws IOException {
        Path directory = Paths.get(OUTPUT_DIRECTORY).resolve(PACKAGE_NAME);
        Files.createDirectories(directory);
        Files.write(directory.resolve(className + ".java"), code.getBytes(StandardCharsets.UTF_8));
    }
}
