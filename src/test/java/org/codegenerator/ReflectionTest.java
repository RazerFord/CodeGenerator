package org.codegenerator;

import org.codegenerator.generator.BuilderGenerator;
import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcescodegeneratorbuilder.Sum;
import org.codegenerator.resourcescodegeneratorpojo.Accumulator;
import org.codegenerator.resourcescodegeneratorpojo.AccumulatorHolder;
import org.codegenerator.resourcescodegeneratorpojo.ChildWithParentWithPrivateField;
import org.codegenerator.resourcescodegeneratorpojo.ParentWithPrivateField;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
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
    void withoutSetterForOneFieldTest() throws IOException {
        final String generatedClassName = "WithoutSetterForOneFieldClass";
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

    @Test
    void withoutSetterForPojoTest() throws IOException {
        final String generatedClassName = "WithoutSetterForPojoClass";
        POJOGenerator<AccumulatorHolder> generator = new POJOGenerator<>(AccumulatorHolder.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

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

        AccumulatorHolder other = generatedCodeCompiler.createObject(generatedClassName);
        assertEquals(accumulatorHolder, other);
    }

    @Test
    void withoutSetterForBuilderTest() throws IOException {
        final String generatedClassName = "WithoutSetterForBuilderClass";
        BuilderGenerator<Sum> generator = new BuilderGenerator<>(Sum.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Sum sum = new Sum.Builder().setA(12).setB(42).calculate().build();

        generator.generateCode(sum, Paths.get(OUTPUT_DIRECTORY));

        Sum other = generatedCodeCompiler.createObject(generatedClassName);
        assertEquals(sum, other);
    }

    @Test
    void onlyReflectionTest() throws IOException, NoSuchFieldException, IllegalAccessException {
        final String generatedClassName = "OnlyReflectionClass";
        POJOGenerator<ChildWithParentWithPrivateField> generator = new POJOGenerator<>(ChildWithParentWithPrivateField.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ChildWithParentWithPrivateField child = new ChildWithParentWithPrivateField();
        Field fieldName = ParentWithPrivateField.class.getDeclaredField("name");
        fieldName.setAccessible(true);
        fieldName.set(child, "John Doe");

        generator.generateCode(child, Paths.get(OUTPUT_DIRECTORY));

        ChildWithParentWithPrivateField other = generatedCodeCompiler.createObject(generatedClassName);
        assertEquals(child, other);
    }
}
