package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcescodegeneratorpojo.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodeGeneratorPOJOTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generate";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";

    @Test
    public void setterPointTest() {
        final String generatedClassName = "GeneratedPointClass";
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Point point = new Point();
        point.setX(100);
        point.setY(6);
        point.setZ(3);
        generator.generate(point, Paths.get(OUTPUT_DIRECTORY));

        Point other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    public void defaultPointArgumentsTest() {
        final String generatedClassName = "GeneratedPointClass";
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Point point = new Point();
        point.setX(0);
        point.setY(0);
        point.setZ(0);
        generator.generate(point, Paths.get(OUTPUT_DIRECTORY));

        Point other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    public void setterUserTest() {
        final String generatedClassName = "GeneratedUserClass";
        POJOGenerator<User> generator = new POJOGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = new User();
        user.setName("Alex");
        user.setAge(12);
        user.setWeight(42);
        generator.generate(user, Paths.get(OUTPUT_DIRECTORY));

        User other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    public void defaultUserArgumentsTest() {
        final String generatedClassName = "GeneratedUserClass";
        POJOGenerator<User> generator = new POJOGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = new User();
        generator.generate(user, Paths.get(OUTPUT_DIRECTORY));

        User other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    public void setterAllPrimitiveTypesTest() {
        final String generatedClassName = "GeneratedAllPrimitiveTypesClass";
        POJOGenerator<AllPrimitiveTypes> generator = new POJOGenerator<>(AllPrimitiveTypes.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        AllPrimitiveTypes allPrimitiveTypes = new AllPrimitiveTypes();
        allPrimitiveTypes.setByte((byte) 1);
        allPrimitiveTypes.setShort((short) 14);
        allPrimitiveTypes.setInt(42);
        allPrimitiveTypes.setLong(123);
        allPrimitiveTypes.setFloat(42.0F);
        allPrimitiveTypes.setDouble(42.0);
        allPrimitiveTypes.setChar('b');
        generator.generate(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypes other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    public void setterPointComplexTest() {
        final String generatedClassName = "GeneratedPointComplexClass";
        POJOGenerator<PointComplex> generator = new POJOGenerator<>(PointComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        PointComplex point = new PointComplex();
        point.setX(100);
        point.setY(6);
        point.setZ(3);
        generator.generate(point, Paths.get(OUTPUT_DIRECTORY));

        PointComplex other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    public void setterUserComplexTest() {
        final String generatedClassName = "GeneratedUserComplexClass";
        POJOGenerator<UserComplex> generator = new POJOGenerator<>(UserComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserComplex user = new UserComplex();
        user.setNameAgeWeight("Alex", 12, 42);
        generator.generate(user, Paths.get(OUTPUT_DIRECTORY));

        UserComplex other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    public void setterAllPrimitiveTypesComplexTest() {
        final String generatedClassName = "GeneratedAllPrimitiveTypesComplexClass";
        POJOGenerator<AllPrimitiveTypesComplex> generator = new POJOGenerator<>(AllPrimitiveTypesComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        AllPrimitiveTypesComplex allPrimitiveTypes = new AllPrimitiveTypesComplex();
        allPrimitiveTypes.setByte((byte) 1);
        allPrimitiveTypes.setShort((short) 14);
        allPrimitiveTypes.setInt(42);
        allPrimitiveTypes.setLong(123);
        allPrimitiveTypes.setFloat(42.0F);
        allPrimitiveTypes.setDouble(42.0);
        allPrimitiveTypes.setChar('b');
        generator.generate(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypesComplex other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    public void setterAllPrimitiveTypesMixedTest() {
        final String generatedClassName = "GeneratedAllPrimitiveTypesMixedClass";
        POJOGenerator<AllPrimitiveTypesMixed> generator = new POJOGenerator<>(AllPrimitiveTypesMixed.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        AllPrimitiveTypesMixed allPrimitiveTypes = new AllPrimitiveTypesMixed();
        allPrimitiveTypes.setByte((byte) 1);
        allPrimitiveTypes.setShort((short) 14);
        allPrimitiveTypes.setInt(42);
        allPrimitiveTypes.setLong(123);
        allPrimitiveTypes.setFloat(42.0F);
        allPrimitiveTypes.setDouble(42.0);
        allPrimitiveTypes.setChar('b');
        allPrimitiveTypes.setBoolean(true);
        generator.generate(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypesMixed other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    public void setterAllPrimitiveTypesBoxedTest() {
        final String generatedClassName = "GeneratedAllBoxedTypesMixedClass";
        POJOGenerator<AllBoxedTypesMixed> generator = new POJOGenerator<>(AllBoxedTypesMixed.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        AllBoxedTypesMixed allBoxedTypes = new AllBoxedTypesMixed();
        allBoxedTypes.setByte((byte) 1);
        allBoxedTypes.setShort((short) 14);
        allBoxedTypes.setInt(42);
        allBoxedTypes.setLong(123);
        allBoxedTypes.setFloat(42.0F);
        allBoxedTypes.setDouble(42.0);
        allBoxedTypes.setChar('b');
        allBoxedTypes.setBoolean(true);
        generator.generate(allBoxedTypes, Paths.get(OUTPUT_DIRECTORY));

        AllBoxedTypesMixed other = createObject(generatedClassName);
        assertEquals(allBoxedTypes, other);
    }

    @Test
    public void setterBoxingUnboxingTest() {
        final String generatedClassName = "GeneratedOneFieldBoxingUnboxingClass";
        POJOGenerator<OneFieldBoxingUnboxing> generator = new POJOGenerator<>(OneFieldBoxingUnboxing.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneFieldBoxingUnboxing oneFieldBoxingUnboxing = new OneFieldBoxingUnboxing();
        oneFieldBoxingUnboxing.setJ(42);
        generator.generate(oneFieldBoxingUnboxing, Paths.get(OUTPUT_DIRECTORY));

        OneFieldBoxingUnboxing other = createObject(generatedClassName);
        assertEquals(oneFieldBoxingUnboxing, other);
    }

    @Test
    public void setterBoxingUnboxingWithDefaultArgsTest() {
        final String generatedClassName = "GeneratedOneFieldBoxingUnboxingClass";
        POJOGenerator<OneFieldBoxingUnboxing> generator = new POJOGenerator<>(OneFieldBoxingUnboxing.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneFieldBoxingUnboxing oneFieldBoxingUnboxing = new OneFieldBoxingUnboxing();
        generator.generate(oneFieldBoxingUnboxing, Paths.get(OUTPUT_DIRECTORY));

        OneFieldBoxingUnboxing other = createObject(generatedClassName);
        assertEquals(oneFieldBoxingUnboxing, other);
    }

    @Test
    public void setterWithArrayTest() {
        final String generatedClassName = "GeneratedClassWithArrayOfPrimitiveTypesClass";
        POJOGenerator<ClassWithArrayOfPrimitiveTypes> generator = new POJOGenerator<>(ClassWithArrayOfPrimitiveTypes.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithArrayOfPrimitiveTypes classWithArrayOfPrimitiveTypes = new ClassWithArrayOfPrimitiveTypes();
        int[] array = new int[]{1, 2, 4, 2, 5};
        classWithArrayOfPrimitiveTypes.setArrayOfInt(array);
        generator.generate(classWithArrayOfPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        ClassWithArrayOfPrimitiveTypes other = createObject(generatedClassName);
        assertEquals(classWithArrayOfPrimitiveTypes, other);
    }

    @Test
    public void setterWithOtherPOJOTest() {
        final String generatedClassName = "GeneratedClassWithOtherPOJOClass";
        POJOGenerator<ClassWithOtherPOJO> generator = new POJOGenerator<>(ClassWithOtherPOJO.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithOtherPOJO classWithOtherPOJO = new ClassWithOtherPOJO();
        Point point = new Point();
        point.setX(1);
        point.setY(4);
        point.setZ(9);
        classWithOtherPOJO.setPoint(point);
        generator.generate(classWithOtherPOJO, Paths.get(OUTPUT_DIRECTORY));

        ClassWithOtherPOJO other = createObject(generatedClassName);
        assertEquals(classWithOtherPOJO, other);
    }

    @Test
    public void setterWithManyPOJOTest() {
        final String generatedClassName = "GeneratedClassWithManyPOJOClass";
        POJOGenerator<ClassWithManyPOJO> generator = new POJOGenerator<>(ClassWithManyPOJO.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithManyPOJO classWithManyPOJO = new ClassWithManyPOJO();

        Point point = new Point();
        point.setX(1);
        point.setY(4);
        point.setZ(9);
        ClassWithOtherPOJO classWithOtherPOJO = new ClassWithOtherPOJO();
        classWithOtherPOJO.setPoint(point);
        classWithManyPOJO.setClassWithOtherPOJO(classWithOtherPOJO);

        User user = new User();
        user.setAge(13);
        user.setName("John Doe");
        user.setWeight(43);
        classWithManyPOJO.setUser(user);

        AllPrimitiveTypes allPrimitiveTypes = new AllPrimitiveTypes();
        allPrimitiveTypes.setBoolean(true);
        allPrimitiveTypes.setChar('c');
        allPrimitiveTypes.setInt(42);
        classWithManyPOJO.setAllPrimitiveTypes(allPrimitiveTypes);

        generator.generate(classWithManyPOJO, Paths.get(OUTPUT_DIRECTORY));

        ClassWithManyPOJO other = createObject(generatedClassName);
        assertEquals(classWithManyPOJO, other);
    }

    @SuppressWarnings("unchecked")
    public <R> R createObject(String generatedClassName) {
        try {
            String absolutePathToClass = Paths.get(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, generatedClassName + ".java").toAbsolutePath().normalize().toString();
            String className = CLASS_NAME_PREFIX + generatedClassName;

            GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler();
            assertTrue(generatedCodeCompiler.compile(OUTPUT_DIRECTORY, absolutePathToClass));

            Class<?> clazz = generatedCodeCompiler.loadClass(OUTPUT_DIRECTORY, className);
            Object o = clazz.getConstructors()[0].newInstance();
            return (R) clazz.getMethod(METHOD_NAME).invoke(o);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
