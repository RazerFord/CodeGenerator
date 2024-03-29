package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcescodegeneratorpojo.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGeneratorPOJOTest {
    private static final String OUTPUT_DIRECTORY = "./";
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String METHOD_NAME = "generateCode";
    private static final String CLASS_PATH_PREFIX = "./generatedclass/";
    private static final String CLASS_NAME_PREFIX = "generatedclass.";
    private static final GeneratedCodeCompiler generatedCodeCompiler = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    @Test
    void setterPointTest() throws IOException {
        final String generatedClassName = "GeneratedPointClass";
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Point point = new Point();
        point.setX(100);
        point.setY(6);
        point.setZ(3);
        generator.generateCode(point, Paths.get(OUTPUT_DIRECTORY));

        Point other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    void defaultPointArgumentsTest() throws IOException {
        final String generatedClassName = "GeneratedPointClass";
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Point point = new Point();
        point.setX(0);
        point.setY(0);
        point.setZ(0);
        generator.generateCode(point, Paths.get(OUTPUT_DIRECTORY));

        Point other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    void setterUserTest() throws IOException {
        final String generatedClassName = "GeneratedUserClass";
        POJOGenerator<User> generator = new POJOGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = new User();
        user.setName("Alex");
        user.setAge(12);
        user.setWeight(42);
        generator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    void defaultUserArgumentsTest() throws IOException {
        final String generatedClassName = "GeneratedUserClass";
        POJOGenerator<User> generator = new POJOGenerator<>(User.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        User user = new User();
        generator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        User other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    void setterAllPrimitiveTypesTest() throws IOException {
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
        generator.generateCode(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypes other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    void setterPointComplexTest() throws IOException {
        final String generatedClassName = "GeneratedPointComplexClass";
        POJOGenerator<PointComplex> generator = new POJOGenerator<>(PointComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        PointComplex point = new PointComplex();
        point.setX(100);
        point.setY(6);
        point.setZ(3);
        generator.generateCode(point, Paths.get(OUTPUT_DIRECTORY));

        PointComplex other = createObject(generatedClassName);
        assertEquals(point, other);
    }

    @Test
    void setterUserComplexTest() throws IOException {
        final String generatedClassName = "GeneratedUserComplexClass";
        POJOGenerator<UserComplex> generator = new POJOGenerator<>(UserComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserComplex user = new UserComplex();
        user.setNameAgeWeight("Alex", 12, 42);
        generator.generateCode(user, Paths.get(OUTPUT_DIRECTORY));

        UserComplex other = createObject(generatedClassName);
        assertEquals(user, other);
    }

    @Test
    void setterAllPrimitiveTypesComplexTest() throws IOException {
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
        generator.generateCode(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypesComplex other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    void setterAllPrimitiveTypesMixedTest() throws IOException {
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
        generator.generateCode(allPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        AllPrimitiveTypesMixed other = createObject(generatedClassName);
        assertEquals(allPrimitiveTypes, other);
    }

    @Test
    void setterAllPrimitiveTypesBoxedTest() throws IOException {
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
        generator.generateCode(allBoxedTypes, Paths.get(OUTPUT_DIRECTORY));

        AllBoxedTypesMixed other = createObject(generatedClassName);
        assertEquals(allBoxedTypes, other);
    }

    @Test
    void setterBoxingUnboxingTest() throws IOException {
        final String generatedClassName = "GeneratedOneFieldBoxingUnboxingClass";
        POJOGenerator<OneFieldBoxingUnboxing> generator = new POJOGenerator<>(OneFieldBoxingUnboxing.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneFieldBoxingUnboxing oneFieldBoxingUnboxing = new OneFieldBoxingUnboxing();
        oneFieldBoxingUnboxing.setJ(42);
        generator.generateCode(oneFieldBoxingUnboxing, Paths.get(OUTPUT_DIRECTORY));

        OneFieldBoxingUnboxing other = createObject(generatedClassName);
        assertEquals(oneFieldBoxingUnboxing, other);
    }

    @Test
    void setterBoxingUnboxingWithDefaultArgsTest() throws IOException {
        final String generatedClassName = "GeneratedOneFieldBoxingUnboxingDefaultClass";
        POJOGenerator<OneFieldBoxingUnboxing> generator = new POJOGenerator<>(OneFieldBoxingUnboxing.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        OneFieldBoxingUnboxing oneFieldBoxingUnboxing = new OneFieldBoxingUnboxing();
        generator.generateCode(oneFieldBoxingUnboxing, Paths.get(OUTPUT_DIRECTORY));

        OneFieldBoxingUnboxing other = createObject(generatedClassName);
        assertEquals(oneFieldBoxingUnboxing, other);
    }

    @Test
    void setterWithArrayPrimitiveTypesTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithArrayOfPrimitiveTypesClass";
        POJOGenerator<ClassWithArrayOfPrimitiveTypes> generator = new POJOGenerator<>(ClassWithArrayOfPrimitiveTypes.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithArrayOfPrimitiveTypes classWithArrayOfPrimitiveTypes = new ClassWithArrayOfPrimitiveTypes();
        int[] array = new int[]{1, 2, 4, 2, 5};
        classWithArrayOfPrimitiveTypes.setArrayOfInt(array);
        generator.generateCode(classWithArrayOfPrimitiveTypes, Paths.get(OUTPUT_DIRECTORY));

        ClassWithArrayOfPrimitiveTypes other = createObject(generatedClassName);
        assertEquals(classWithArrayOfPrimitiveTypes, other);
    }

    @Test
    void setterWithArrayNonPrimitiveTypesTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithArrayOfNonPrimitiveTypesClass";
        POJOGenerator<Points> generator = new POJOGenerator<>(Points.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Points points = new Points();
        Point[] array = new Point[5];

        array[0] = new Point();
        array[0].setX(42);
        array[0].setY(42);

        array[2] = new Point();
        array[2].setX(14);

        array[4] = new Point();
        array[4].setX(17);

        points.setPoints(array);
        generator.generateCode(points, Paths.get(OUTPUT_DIRECTORY));

        Points other = createObject(generatedClassName);
        assertEquals(points, other);
    }

    @Test
    void setterWithOtherPOJOTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithOtherPOJOClass";
        POJOGenerator<ClassWithOtherPOJO> generator = new POJOGenerator<>(ClassWithOtherPOJO.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithOtherPOJO classWithOtherPOJO = new ClassWithOtherPOJO();
        Point point = new Point();
        point.setX(1);
        point.setY(4);
        point.setZ(9);
        classWithOtherPOJO.setPoint(point);
        generator.generateCode(classWithOtherPOJO, Paths.get(OUTPUT_DIRECTORY));

        ClassWithOtherPOJO other = createObject(generatedClassName);
        assertEquals(classWithOtherPOJO, other);
    }

    @Test
    void setterWithManyPOJOTest() throws IOException {
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

        generator.generateCode(classWithManyPOJO, Paths.get(OUTPUT_DIRECTORY));

        ClassWithManyPOJO other = createObject(generatedClassName);
        assertEquals(classWithManyPOJO, other);
    }

    @Test
    void nonDefaultConstructor() throws IOException {
        final String generatedClassName = "GeneratedUserComplexConstructorClass";
        POJOGenerator<UserComplexConstructor> generator = new POJOGenerator<>(UserComplexConstructor.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        UserComplexConstructor userComplexConstructor = new UserComplexConstructor("Anonymous");
        userComplexConstructor.setName("John Doe");
        userComplexConstructor.setAge(42);
        userComplexConstructor.setWeight(80);
        generator.generateCode(userComplexConstructor, Paths.get(OUTPUT_DIRECTORY));

        UserComplexConstructor other = createObject(generatedClassName);
        assertEquals(userComplexConstructor, other);
    }

    @Test
    void veryManySetterDefaultTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithManyDefaultFieldsClass";
        POJOGenerator<ClassWithManyFields> generator = new POJOGenerator<>(ClassWithManyFields.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithManyFields classWithManyFields = new ClassWithManyFields();

        generator.generateCode(classWithManyFields, Paths.get(OUTPUT_DIRECTORY));

        ClassWithManyFields other = createObject(generatedClassName);
        assertEquals(classWithManyFields, other);
    }

    @Test
    void veryManySetterTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithManyFieldsClass";
        POJOGenerator<ClassWithManyFields> generator = new POJOGenerator<>(ClassWithManyFields.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        ClassWithManyFields classWithManyFields = new ClassWithManyFields();
        classWithManyFields.setField1("field1");
        classWithManyFields.setField2("field2");
        classWithManyFields.setField3("field3");
        classWithManyFields.setField4("field4");
        classWithManyFields.setField5("field5");
        classWithManyFields.setField6("field6");
        classWithManyFields.setField7("field7");
        classWithManyFields.setField8("field8");
        classWithManyFields.setField9("field9");
        classWithManyFields.setField10("field10");
        classWithManyFields.setField11("field11");
        classWithManyFields.setField12("field12");
        classWithManyFields.setField13("field13");
        classWithManyFields.setField14("field14");
        classWithManyFields.setField15("field15");
        classWithManyFields.setField16("field16");
        classWithManyFields.setField17("field17");
        classWithManyFields.setField18("field18");
        classWithManyFields.setField19("field19");
        classWithManyFields.setField20("field20");
        classWithManyFields.setField21("field21");
        classWithManyFields.setField22("field22");
        classWithManyFields.setField23("field23");
        classWithManyFields.setField24("field24");
        classWithManyFields.setField25("field25");
        classWithManyFields.setField26("field26");
        classWithManyFields.setField27("field27");
        classWithManyFields.setField28("field28");
        classWithManyFields.setField29("field29");
        classWithManyFields.setField30("field30");
        classWithManyFields.setField31("field31");
        classWithManyFields.setField32("field32");
        classWithManyFields.setField33("field33");

        generator.generateCode(classWithManyFields, Paths.get(OUTPUT_DIRECTORY));

        ClassWithManyFields other = createObject(generatedClassName);
        assertEquals(classWithManyFields, other);
    }

    @Test
    @Timeout(20)
    void veryManySetterComplexTest() throws IOException {
        final String generatedClassName = "GeneratedClassWithManyFieldsComplexClass";
        POJOGenerator<ClassWithManyFieldsComplex> generator = new POJOGenerator<>(ClassWithManyFieldsComplex.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

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

        generator.generateCode(classWithManyFieldsComplex, Paths.get(OUTPUT_DIRECTORY));

        ClassWithManyFieldsComplex other = createObject(generatedClassName);
        assertEquals(classWithManyFieldsComplex, other);
    }

    @Test
    void setterMultidimensionalIntArrayTest() throws IOException {
        final String generatedClassName = "GeneratedClassMultidimensionalIntArrayClass";
        POJOGenerator<MultidimensionalIntArray> generator = new POJOGenerator<>(MultidimensionalIntArray.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        MultidimensionalIntArray multidimensionalIntArray = getMultidimensionalIntArray();

        generator.generateCode(multidimensionalIntArray, Paths.get(OUTPUT_DIRECTORY));

        MultidimensionalIntArray other = createObject(generatedClassName);
        assertEquals(multidimensionalIntArray, other);
    }

    @Test
    void setterMultidimensionalPointArrayTest() throws IOException {
        final String generatedClassName = "GeneratedClassMultidimensionalPointArrayClass";
        POJOGenerator<MultidimensionalPointArray> generator = new POJOGenerator<>(MultidimensionalPointArray.class, PACKAGE_NAME, generatedClassName, METHOD_NAME);

        MultidimensionalPointArray multidimensionalPointArray = getMultidimensionalPointArray();

        generator.generateCode(multidimensionalPointArray, Paths.get(OUTPUT_DIRECTORY));

        MultidimensionalPointArray other = createObject(generatedClassName);
        assertEquals(multidimensionalPointArray, other);
    }

    @NotNull
    private static MultidimensionalIntArray getMultidimensionalIntArray() {
        MultidimensionalIntArray multidimensionalIntArray = new MultidimensionalIntArray();

        int[][][] array = new int[][][]{
                new int[][]{
                        new int[]{
                                1, 2, 3
                        },
                        new int[]{
                                4
                        }
                },
                new int[][]{
                        new int[]{
                                5
                        }
                }
        };

        multidimensionalIntArray.setInts(array);
        return multidimensionalIntArray;
    }

    @NotNull
    private static MultidimensionalPointArray getMultidimensionalPointArray() {
        MultidimensionalPointArray multidimensionalPointArray = new MultidimensionalPointArray();

        Point p = new Point();
        p.setX(12);
        p.setY(17);
        p.setZ(42);

        Point[][][] array = new Point[][][]{
                new Point[][]{
                        new Point[]{p, p}
                },
                new Point[][]{
                        new Point[]{p}
                },
                new Point[][]{}
        };

        multidimensionalPointArray.setPoints(array);
        return multidimensionalPointArray;
    }

    <R> R createObject(String generatedClassName) {
        return generatedCodeCompiler.createObject(generatedClassName);
    }
}
