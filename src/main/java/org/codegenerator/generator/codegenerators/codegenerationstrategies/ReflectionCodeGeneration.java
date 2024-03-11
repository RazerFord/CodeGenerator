package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.SetterUsingReflection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.*;

import static javax.lang.model.element.Modifier.*;

public class ReflectionCodeGeneration {
    private static final String NESTED_CLASS_NAME = "ReflexiveFieldSetter";
    private static final String NESTED_METHOD_NAME = "set";
    private static final String METHOD_NAME = "getFields";
    private static final String INSTANCE_NAME = StringUtils.uncapitalize(ReflectionCodeGeneration.class.getSimpleName());

    private final String nestedClassName;
    private final String nestedMethodName;
    private final String methodName;
    private final String instanceName;

    public ReflectionCodeGeneration() {
        this(NESTED_CLASS_NAME, NESTED_METHOD_NAME, METHOD_NAME, INSTANCE_NAME);
    }

    public ReflectionCodeGeneration(
            String nestedClassName,
            String nestedMethodName,
            String methodName,
            String instanceName
    ) {
        this.nestedClassName = nestedClassName;
        this.nestedMethodName = nestedMethodName;
        this.methodName = methodName;
        this.instanceName = instanceName;
    }

    public void generate(
            String variableName,
            TypeSpec.Builder typeBuilder,
            List<MethodSpec.Builder> methods,
            @NotNull MethodContext<Executable> methodContext,
            Deque<MethodContext<Executable>> stack
    ) {
        HistoryNode<Executable> historyNode = methodContext.getNode();
        MethodSpec.Builder methodBuilder = methodContext.getMethod();

        if (historyNode.getSetterUsingReflections().isEmpty()) return;

        addIfNonExists(typeBuilder);
        methodBuilder.addCode(createReflexiveSetter(variableName));

        for (SetterUsingReflection<Executable> call : historyNode.getSetterUsingReflections()) {
            UniqueMethodNameGenerator nameGenerator = new UniqueMethodNameGenerator(methods, stack);
            String value = Utils.toRepresentation(nameGenerator, call.getHistoryArg(), stack);
            methodBuilder.addCode(setField(variableName, value, call.getField()));
        }
    }

    @Contract(" -> new")
    private static @NotNull CodeBlock beginTryCatch() {
        return CodeBlock.builder()
                .beginControlFlow("try")
                .build();
    }

    private static @NotNull CodeBlock endTryCatch() {
        return CodeBlock.builder()
                .nextControlFlow("catch ($T e) ", IllegalAccessException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .build();
    }

    private @NotNull CodeBlock createReflexiveSetter(String variableName) {
        return CodeBlock.builder()
                .addStatement("$1L $2L = new $1L($3L.getClass())", nestedClassName, instanceName, variableName)
                .build();
    }

    private @NotNull CodeBlock setField(
            String variableName,
            String value,
            @NotNull Field field
    ) {
        return CodeBlock.builder()
                .addStatement("$L.set($T.class, $L, $S, $L)",
                        instanceName, field.getDeclaringClass(), variableName, field.getName(), value)
                .build();
    }

    private void addIfNonExists(TypeSpec.@NotNull Builder typeBuilder) {
        ClassName classClassName = ClassName.get(Class.class);
        TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
        ParameterizedTypeName classType = ParameterizedTypeName.get(classClassName, wildcard);
        ParameterSpec parameterSpec = ParameterSpec.builder(classType, CLAZZ).build();
        ClassName mapClassName = ClassName.get(Map.class);
        ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Field.class);
        ParameterizedTypeName mapMapType = ParameterizedTypeName.get(mapClassName, classType, mapType);

        addMethodGetFields(typeBuilder, parameterSpec, mapType, mapMapType);
        addNestedClass(typeBuilder, parameterSpec, mapMapType);
    }

    /**
     * Create and add a {@link ReflectionCodeGeneration#METHOD_NAME} method to `typeBuilder` if it doesn't exist
     * <pre>
     * {@code
     *  Map<Class < ?>, Map<String, Field>> getFields(Class<?> clazz) {
     *      Map<Class<?>, Map<String, Field>> classMapMap = new HashMap<>();
     *      for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
     *          Map<String, Field> fieldMap = new HashMap<>();
     *          for (Field field : clazz.getDeclaredFields()) {
     *              field.setAccessible(true);
     *              fieldMap.put(field.getName(), field);
     *          }
     *          classMapMap.put(clazz, fieldMap);
     *      }
     *      return classMapMap;
     *  }
     * }
     * </pre>
     */
    private void addMethodGetFields(
            TypeSpec.@NotNull Builder typeBuilder,
            ParameterSpec parameterSpec,
            ParameterizedTypeName mapType,
            ParameterizedTypeName mapMapType
    ) {
        if (typeBuilder.methodSpecs.stream().noneMatch(m -> m.name.equals(methodName) &&
                m.parameters.equals(Collections.singletonList(parameterSpec)))) {
            MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC, STATIC).returns(mapMapType)
                    .addParameter(parameterSpec)
                    .addStatement("$T classMapMap = new $T<>()", mapMapType, HashMap.class)
                    .beginControlFlow("for (; $1L != Object.class; $1L = $1L.getSuperclass())", CLAZZ)
                    .addStatement("$T fieldMap = new $T<>()", mapType, HashMap.class)
                    .beginControlFlow("for (Field field : $1L.getDeclaredFields())", CLAZZ)
                    .addStatement("field.setAccessible(true)")
                    .addStatement("fieldMap.put(field.getName(), field)")
                    .endControlFlow()
                    .addStatement("classMapMap.put($1L, fieldMap)", CLAZZ)
                    .endControlFlow()
                    .addStatement("return classMapMap")
                    .addJavadoc(GET_FIELDS_METHOD_JAVADOC)
                    .build();

            typeBuilder.addMethod(methodSpec);
        }
    }

    /**
     * Create and add a {@link ReflectionCodeGeneration#NESTED_CLASS_NAME} class to `typeBuilder` if it doesn't exist
     * <pre>
     * {@code
     *   private static class ReflexiveFieldSetter {
     *     private final Map<Class<?>, Map<String, Field>> fields;
     *
     *     ReflexiveFieldSetter(Class<?> clazz) {
     *       fields = getFields(clazz);
     *     }
     *
     *     void set(Class<?> clazz, Object object, String name, Object value) {
     *       try {
     *         fields.get(clazz).get(name).set(object, value);
     *       } catch (IllegalAccessException e)  {
     *         throw new RuntimeException(e);
     *       }
     *     }
     *   }
     * }
     * </pre>
     */
    private void addNestedClass(
            TypeSpec.@NotNull Builder typeBuilder,
            ParameterSpec parameterSpec,
            ParameterizedTypeName mapMapType
    ) {
        if (typeBuilder.typeSpecs.stream().noneMatch(c -> c.name.equals(nestedClassName))) {
            TypeSpec nestedClass = TypeSpec.classBuilder(NESTED_CLASS_NAME)
                    .addModifiers(PUBLIC, STATIC)
                    .addField(FieldSpec.builder(mapMapType, FIELD_NAME, PRIVATE, FINAL).build())
                    .addMethod(MethodSpec
                            .constructorBuilder()
                            .addParameter(parameterSpec)
                            .addStatement("$L = $L(clazz)", FIELD_NAME, METHOD_NAME)
                            .build())
                    .addMethod(MethodSpec
                            .methodBuilder(nestedMethodName)
                            .addParameter(parameterSpec)
                            .addParameter(Object.class, OBJECT)
                            .addParameter(String.class, FIELD)
                            .addParameter(Object.class, VALUE)
                            .addCode(beginTryCatch())
                            .addStatement("$L.get(clazz).get($L).set($L, $L)", FIELD_NAME, FIELD, OBJECT, VALUE)
                            .addCode(endTryCatch())
                            .addJavadoc(NESTED_METHOD_JAVADOC)
                            .build())
                    .addJavadoc(NESTED_CLASS_JAVADOC)
                    .build();

            typeBuilder.addType(nestedClass);
        }
    }

    private static final String CLAZZ = "clazz";
    private static final String FIELD_NAME = "fields";
    private static final String OBJECT = "object";
    private static final String FIELD = "name";
    private static final String VALUE = "value";
    private static final CodeBlock GET_FIELDS_METHOD_JAVADOC = buildJavaDocForGetFieldsMethod();
    private static final CodeBlock NESTED_CLASS_JAVADOC = buildJavaDocForNestedClass();
    private static final CodeBlock NESTED_METHOD_JAVADOC = buildJavaDocForNestedMethod();

    @Contract(" -> new")
    private static @NotNull CodeBlock buildJavaDocForGetFieldsMethod() {
        return CodeBlock.builder()
                .add("Gets all fields of a class and its superclasses using reflection.\n")
                .add("\n")
                .add("@param $L the class for which you want to get the fields\n", CLAZZ)
                .add("@return mapping from classes to their fields\n")
                .build();
    }

    @Contract(" -> new")
    private static @NotNull CodeBlock buildJavaDocForNestedClass() {
        return CodeBlock.builder()
                .add("Class for setting object field values using reflection.")
                .build();
    }

    @Contract(" -> new")
    private static @NotNull CodeBlock buildJavaDocForNestedMethod() {
        return CodeBlock.builder()
                .add("Sets the field value using reflection.\n")
                .add("\n")
                .add("@param clazz the class containing the field\n")
                .add("@param $L the object whose field needs to be set\n", OBJECT)
                .add("@param $L The name of the field to set\n", FIELD)
                .add("@param $L The value to set the field to\n", VALUE)
                .build();
    }
}
