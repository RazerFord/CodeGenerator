package org.codegenerator;

import org.codegenerator.generator.Generator;
import org.codegenerator.generator.Generators;
import org.codegenerator.testclasses.generic.*;
import org.codegenerator.testclasses.generic.persons.BestClient;
import org.codegenerator.testclasses.generic.persons.Boss;
import org.codegenerator.testclasses.generic.persons.Client;
import org.codegenerator.testclasses.generic.persons.Person;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericClassesTest {
    @Test
    void oneGenericFieldTest() throws IOException {
        final String generatedClassName = "OneGenericFieldClass";
        Generator generator = Generators.forPojo(OneField.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneField<OneField<OneField<Integer>>> oneField0 = new OneField<>();
        OneField<OneField<Integer>> oneField1 = new OneField<>();
        oneField1.setValue(createOneFieldInteger());
        oneField0.setValue(oneField1);

        generator.generateCode(oneField0, Paths.get(OUTPUT_DIRECTORY));

        OneField<OneField<OneField<Integer>>> other = createObject(generatedClassName);

        assertEquals(oneField0, other);
    }

    @Test
    void twoGenericFieldTest() throws IOException {
        final String generatedClassName = "TwoGenericFieldClass";
        Generator generator = Generators.forPojo(TwoFields.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        TwoFields<OneField<Integer>, TwoFields<String, TwoFields<Object, String>>> twoFields0 = new TwoFields<>();
        twoFields0.setFirst(createOneFieldInteger());
        TwoFields<String, TwoFields<Object, String>> twoFields1 = new TwoFields<>();
        TwoFields<Object, String> twoFields2 = new TwoFields<>();
        twoFields2.setSecond("hello world");
        twoFields1.setFirst("John Doe");
        twoFields1.setSecond(twoFields2);
        twoFields0.setSecond(twoFields1);

        generator.generateCode(twoFields0, Paths.get(OUTPUT_DIRECTORY));

        TwoFields<OneField<Integer>, TwoFields<String, TwoFields<Object, String>>> other = createObject(generatedClassName);

        assertEquals(twoFields0, other);
    }

    @Test
    void twoGenericFieldWithBoundsTest() throws IOException {
        final String generatedClassName = "TwoGenericFieldWithBoundsClass";
        Generator generator = Generators.forPojo(TwoFieldsWithBounds.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        TwoFieldsWithBounds<Person, Boss> twoFieldWithBounds = new TwoFieldsWithBounds<>();
        twoFieldWithBounds.setFirst(new Client("Nikola Tesla"));
        twoFieldWithBounds.setSecond(new Boss("John Doe"));

        generator.generateCode(twoFieldWithBounds, Paths.get(OUTPUT_DIRECTORY));

        TwoFieldsWithBounds<Person, Boss> other = createObject(generatedClassName);

        assertEquals(twoFieldWithBounds, other);
    }

    @Test
    void genericBuilderTest() throws IOException {
        final String generatedClassName = "GenericBuilderClass";
        Generator generator = Generators.forBuilder(PointWithBuilder.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        PointWithBuilder<Integer, Double, Long> pointWithBuilder = PointWithBuilder.<Integer, Double, Long>builder()
                .setX(42)
                .setY(42.)
                .setZ(42L)
                .build();

        generator.generateCode(pointWithBuilder, Paths.get(OUTPUT_DIRECTORY));

        PointWithBuilder<Integer, Double, Long> other = createObject(generatedClassName);

        assertEquals(pointWithBuilder, other);
    }

    @Test
    void threeGenericFieldTest() throws IOException {
        final String generatedClassName = "ThreeGenericFieldClass";
        Generator generator = Generators.forPojo(ThreeFieldsWithSpecificBounds.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ThreeFieldsWithSpecificBounds<Client, BestClient, BestClient> threeFields0 = new ThreeFieldsWithSpecificBounds<>();
        threeFields0.setFirst(new Client("John Doe"));
        threeFields0.setSecond(new BestClient("Boss", 12));
        threeFields0.setThird(new BestClient("Boss", 20));

        generator.generateCode(threeFields0, Paths.get(OUTPUT_DIRECTORY));

        ThreeFieldsWithSpecificBounds<Client, BestClient, BestClient> other = createObject(generatedClassName);

        assertEquals(threeFields0, other);
    }

    private static @NotNull OneField<Integer> createOneFieldInteger() {
        OneField<Integer> oneField = new OneField<>();
        oneField.setValue(42);
        return oneField;
    }
}
