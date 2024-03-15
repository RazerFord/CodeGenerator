# CodeGenerator ![testing workflow](https://github.com/RazerFord/CodeGenerator/actions/workflows/test.yml/badge.svg)

## Overview

This repository contains classes for generating code for a `Java` object or obtaining sequences of methods that can be
used to derive a `Java` object.
Main functionality:

- Generate a code that can be used to create an object that comes as an input parameter
- Find a sequence of methods that can be used to create an object that comes as an input parameter

## Examples

1. Generating code for a `POJO` object.
    ```java
    class Pojo {
        void example() throws IOException {

            Point point1 = new Point(); // Point{x=10, y=42, z=17}
            point1.setX(10);
            point1.setY(42);
            point1.setZ(17);
    
            Points points = new Points();
            points.setPoints(new Point[]{point1}); // Points{points=[Point{x=10, y=42, z=17}]}
    
            Generator generator = Generators.forPojo(Point.class, "generated.points", "GeneratedClass", "create");
            generator.generateCode(points, Paths.get("./examples"));
        } 
    }
    ```
   After the listing is executed, the code above will be generated.
    ```java
    public final class GeneratedClass {
        public static Points create() {
            Points object = new Points();
            object.setPoints(createArrayPoint0());
            return object;
        }
        
        public static Point[] createArrayPoint0() {
            Point[] object = new Point[1];
            object[0] = createPoint1();
            return object;
        }
        
        public static Point createPoint1() {
            Point object = new Point();
            object.setX(10);
            object.setY(42);
            object.setZ(17);
            return object;
        }
    }
    ```   

2. Generate code for the `Builder` object.
    ```java
    class Builder { 
        void example() throws IOException {
            
            User userWithCoins = User.builder()
                    .age(10)
                    .created(18)
                    .name("John Doe")
                    .coins(
                            new long[]{1L, 2L, 5L, 5L, 10L}
                    ) // UserBuilder{name='John Doe', age=10, created=18, coins=[1, 2, 5, 5, 10]}
                    .build(); // User{name='John Doe', age=10, created=18, coins=[1, 2, 5, 5, 10]}
    
            Generator generator = Generators.forBuilder(User.class, "generated.user", "GeneratedClass", "create");
            generator.generateCode(userWithCoins, Paths.get("./examples"));
        } 
    }
    ```
   After the listing is executed, the code above will be generated.
    ```java
    public final class GeneratedClass { 
        public static User create() {
            User.UserBuilder builder = User.builder();
            builder.coins(createArrayLong0());
            builder.name("John Doe");
            builder.created(18L);
            builder.age(10);
            User object = builder.build();
            return object;
        }
    
        public static long[] createArrayLong0() {
            long[] object = new long[5];
            object[0] = 1L;
            object[1] = 2L;
            object[2] = 5L;
            object[3] = 5L;
            object[4] = 10L;
            return object;
        }
    }
    ```

3. Generate code for the object. By default, method search for `Builders` will be applied first, then for `POJO`.
    ```java
    class SomethingObject  {
        void example() throws IOException {
            Accumulator accumulatorA = new Accumulator();
            accumulatorA.setA(3);
            accumulatorA.setB(10); // Sum{a=3, b=10, sum=0}

            Accumulator accumulatorB = new Accumulator();
            accumulatorB.setA(293);
            accumulatorB.setB(59); // Sum{a=293, b=59, sum=0}

            AccumulatorHolder accumulatorHolder = new AccumulatorHolder();
            accumulatorHolder.setA(accumulatorA);
            accumulatorHolder.setB(
                    accumulatorB
            ); // AccumulatorHolder{a=Sum{a=3, b=10, sum=0}, b=Sum{a=293, b=59, sum=0}, c=null}
            accumulatorHolder.calc(); // AccumulatorHolder{a=null, b=null, c=Sum{a=0, b=0, sum=365}}

            Generator generator = Generators.standard("generated.accumulator", "GeneratedClass", "create");
            generator.generateCode(accumulatorHolder, Paths.get("./examples"));
        }
    }
    ```
   After the listing is executed, the code above will be generated.
    ```java
    public final class GeneratedClass {
        /**
        * Gets all fields of a class and its superclasses using reflection.
        *
        * @param clazz the class for which you want to get the fields
        * @return mapping from classes to their fields
        */
        public static Map<Class<?>, Map<String, Field>> getFields(Class<?> clazz) {
            Map<Class<?>, Map<String, Field>> classMapMap = new HashMap<>();
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                Map<String, Field> fieldMap = new HashMap<>();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    fieldMap.put(field.getName(), field);
                }
                classMapMap.put(clazz, fieldMap);
            }
            return classMapMap;
        }

        public static AccumulatorHolder create() {
            AccumulatorHolder object = new AccumulatorHolder();
            ReflexiveFieldSetter reflectionCodeGeneration = new ReflexiveFieldSetter(object.getClass());
            reflectionCodeGeneration.set(AccumulatorHolder.class, object, "c", createAccumulator0());
            return object;
        }

        public static Accumulator createAccumulator0() {
            Accumulator object = new Accumulator();
            ReflexiveFieldSetter reflectionCodeGeneration = new ReflexiveFieldSetter(object.getClass());
            reflectionCodeGeneration.set(Accumulator.class, object, "sum", 365);
            return object;
        }

        /**
        * Class for setting object field values using reflection.
        */
        public static class ReflexiveFieldSetter {
            private final Map<Class<?>, Map<String, Field>> fields;

            ReflexiveFieldSetter(Class<?> clazz) {
                fields = getFields(clazz);
            }

            /**
            * Sets the field value using reflection.
            *
            * @param clazz the class containing the field
            * @param object the object whose field needs to be set
            * @param name The name of the field to set
            * @param value The value to set the field to
            */
            void set(Class<?> clazz, Object object, String name, Object value) {
                try {
                    fields.get(clazz).get(name).set(object, value);
                } catch (IllegalAccessException e)  {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    ```
   To find a sequence of methods, finders for the `Builder` and `POJO` will be called one by one.
   If both `finders` fail, reflection will be used

## Iterator and stream

You can iterate through sequences of methods that can be used to obtain an object

```java
public class IteratorAndStream {
    void iteratorAndStreamExample() {
        Coord coord = Coord.builder()
                .setX(100)
                .setY(42)
                .build();

        Generator generator = Generators.standard();

        // iterator usage
        generator.iterableCode(coord).forEach(System.out::println);

        // stream usage
        generator.streamCode(coord).forEach(System.out::println);
    }
}
```

As a result, the following object receiving codes can be obtained sequentially:

```java
public final class GeneratedClass {
    public static Coord create() {
        Coord.Builder builder = new Coord.Builder();
        builder.setY(42);
        builder.setX(100);
        Coord object = builder.build();
        return object;
    }
}

// or
public final class GeneratedClass {
    public static Coord create() {
        Coord.Builder builder = new Coord.Builder();
        builder.setY(42);
        Coord object = builder.build();
        object.setX(100);
        return object;
    }
}
// etc ...
```

