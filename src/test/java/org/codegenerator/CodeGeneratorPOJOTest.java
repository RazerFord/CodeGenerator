package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesCodeGeneratorPOJO.*;
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

    @SuppressWarnings("unchecked")
    public <R> R createObject(String generatedClassName) {
        try {
            String absolutePathToClass = Paths.get(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, generatedClassName + ".java").toAbsolutePath().normalize().toString();
            String className = CLASS_NAME_PREFIX + generatedClassName;

            GeneratedCodeExecutor generatedCodeExecutor = new GeneratedCodeExecutor();
            assertTrue(generatedCodeExecutor.compile(OUTPUT_DIRECTORY, absolutePathToClass));

            Class<?> clazz = generatedCodeExecutor.loadClass(OUTPUT_DIRECTORY, className);
            Object o = clazz.getConstructors()[0].newInstance();
            return (R) clazz.getMethod(METHOD_NAME).invoke(o);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
