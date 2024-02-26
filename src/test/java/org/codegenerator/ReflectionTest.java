package org.codegenerator;

import org.codegenerator.generator.Generator;
import org.codegenerator.generator.Generators;
import org.codegenerator.testclasses.codegeneratorbuilder.Sum;
import org.codegenerator.testclasses.codegeneratorpojo.Accumulator;
import org.codegenerator.testclasses.codegeneratorpojo.AccumulatorHolder;
import org.codegenerator.testclasses.codegeneratorpojo.ChildWithParentWithPrivateField;
import org.codegenerator.testclasses.codegeneratorpojo.ParentWithPrivateField;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReflectionTest {
    @Test
    void withoutSetterForOneFieldTest() throws IOException {
        final String generatedClassName = "WithoutSetterForOneFieldClass";
        Generator generator = Generators.forPojo(Accumulator.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Accumulator accumulator = new Accumulator();
        accumulator.setA(12);
        accumulator.setB(24);
        accumulator.calculate();
        accumulator.setB(10);
        generator.generateCode(accumulator, Paths.get(OUTPUT_DIRECTORY));

        Accumulator other = createObject(generatedClassName);
        assertEquals(accumulator, other);
    }

    @Test
    void withoutSetterForPojoTest() throws IOException {
        final String generatedClassName = "WithoutSetterForPojoClass";
        Generator generator = Generators.forPojo(AccumulatorHolder.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Accumulator accumulatorA = new Accumulator();
        accumulatorA.setA(3);
        accumulatorA.setB(10);
        Accumulator accumulatorB = new Accumulator();
        accumulatorB.setA(293);
        accumulatorB.setB(59);
        AccumulatorHolder accumulatorHolder = new AccumulatorHolder();
        accumulatorHolder.setA(accumulatorA);
        accumulatorHolder.setB(accumulatorB);
        accumulatorHolder.calc();

        generator.generateCode(accumulatorHolder, Paths.get(OUTPUT_DIRECTORY));

        AccumulatorHolder other = createObject(generatedClassName);
        assertEquals(accumulatorHolder, other);
    }

    @Test
    void withoutSetterForBuilderTest() throws IOException {
        final String generatedClassName = "WithoutSetterForBuilderClass";
        Generator generator = Generators.forBuilder(Sum.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Sum sum = new Sum.Builder()
                .setA(12)
                .setB(42)
                .calculate()
                .build();

        generator.generateCode(sum, Paths.get(OUTPUT_DIRECTORY));

        Sum other = createObject(generatedClassName);
        assertEquals(sum, other);
    }

    @Test
    void onlyReflectionTest() throws IOException, NoSuchFieldException, IllegalAccessException {
        final String generatedClassName = "OnlyReflectionClass";
        Generator generator = Generators.forPojo(ChildWithParentWithPrivateField.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ChildWithParentWithPrivateField child = new ChildWithParentWithPrivateField();
        Field fieldName = ParentWithPrivateField.class.getDeclaredField("name");
        fieldName.setAccessible(true);
        fieldName.set(child, "John Doe");

        generator.generateCode(child, Paths.get(OUTPUT_DIRECTORY));

        ChildWithParentWithPrivateField other = createObject(generatedClassName);
        assertEquals(child, other);
    }
}
