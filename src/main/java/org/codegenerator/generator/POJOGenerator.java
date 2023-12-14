package org.codegenerator.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcDatabase;
import org.jacodb.api.JcMethod;
import org.jacodb.api.cfg.JcAssignInst;
import org.jacodb.api.cfg.JcFieldRef;
import org.jacodb.api.cfg.JcInst;
import org.jacodb.api.cfg.JcInstList;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.codegenerator.Utils.throwIf;

public class POJOGenerator<T> {
    private static final String THIS = "this";
    private final Class<?> clazz;
    private final Constructor<?> defaultConstructor;
    private final String dbname = POJOGenerator.class.getCanonicalName();

    @Contract(pure = true)
    public POJOGenerator(Class<?> clazz) {
        this.clazz = clazz;
        defaultConstructor = throwIfNoConstructorWithoutArgs();
        throwIf(defaultConstructor == null, new RuntimeException(NO_CONSTRUCTOR_WITHOUT_ARG));
    }

    public void generate(@NotNull T object, Path path) {
        Map<String, JcMethod> setters = new HashMap<>();
        extractClassOrInterface(setters);
        Map<String, String> currentFieldValues = getCurrentFieldValues(object);
        generateCode(generateCodeBlocks(currentFieldValues, setters), path);
    }

    private @NotNull List<CodeBlock> generateCodeBlocks(@NotNull Map<String, String> currentFieldValues, Map<String, JcMethod> setters) {
        List<CodeBlock> codeBlocks = new ArrayList<>();

        codeBlocks.add(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());

        for (Map.Entry<String, String> entry : currentFieldValues.entrySet()) {
            CodeBlock codeBlock = CodeBlock.builder().add("object.$L($L)", setters.get(entry.getKey()).getName(), entry.getValue()).build();
            codeBlocks.add(codeBlock);
        }
        return codeBlocks;
    }

    private void generateCode(@NotNull List<CodeBlock> codeBlocks, Path path) {
        MethodSpec.Builder mainBuilder = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args");

        codeBlocks.forEach(mainBuilder::addStatement);

        MethodSpec main = mainBuilder.build();

        TypeSpec generatedClass = TypeSpec.classBuilder("GeneratedClass")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("generatedclass", generatedClass)
                .build();

        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractClassOrInterface(Map<String, JcMethod> setters) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<File> fileList = Collections.singletonList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            db.asyncLoad(fileList).get();
            JcClassOrInterface jcClassOrInterface = db.asyncClasspath(fileList).get().findClassOrNull(clazz.getTypeName());

            if (jcClassOrInterface == null) {
                throw new RuntimeException();
            }

            // извлекаем список полей объекта
            Set<String> fields = new HashSet<>();
            jcClassOrInterface.getDeclaredFields().forEach(it -> fields.add(String.join(".", THIS, it.getName())));

            // затем провеяем методы с помощью jacodb, на наличие инструкций на присваивание значений - это будет сеттер. (this.? = ?)
            for (JcMethod jcMethod : jcClassOrInterface.getDeclaredMethods()) {
                JcInstList<JcInst> instructions = jcMethod.getInstList();
                for (JcInst inst : instructions) {
                    if (inst instanceof JcAssignInst &&
                            fields.contains(((JcAssignInst) inst).getLhv().toString()) &&
                            ((JcAssignInst) inst).getLhv() instanceof JcFieldRef
                    ) {
                        String name = ((JcFieldRef) ((JcAssignInst) inst).getLhv()).getField().getName();
                        setters.put(name, jcMethod);
                    }
                }
            }
        } catch (IOException | ExecutionException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private <E> @NotNull Map<String, String> getCurrentFieldValues(@NotNull E object) {
        Map<String, String> currentFieldValues = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                currentFieldValues.put(field.getName(), field.get(object).toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return currentFieldValues;
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(InMemoryHierarchy.INSTANCE)
        ).get();
    }

    private @Nullable Constructor<?> throwIfNoConstructorWithoutArgs() {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

    private static final String NO_CONSTRUCTOR_WITHOUT_ARG = "There is no constructor without arguments";
}
