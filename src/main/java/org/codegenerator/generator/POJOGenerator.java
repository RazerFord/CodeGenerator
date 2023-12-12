package org.codegenerator.generator;

import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcDatabase;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class POJOGenerator<T> {
    private final T object;
    private final Class<?> clazz;
    private final String dbname = String.format("POJO%s", System.currentTimeMillis());

    @Contract(pure = true)
    public POJOGenerator(@NotNull T object) {
        this.object = object;
        this.clazz = object.getClass();
    }

    public void generate(OutputStream outputStream) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<File> fileList = Collections.singletonList(new File(clazz.getCanonicalName()));
            db.asyncLoad(fileList).get();
            JcClassOrInterface jcClassOrInterface = db.asyncClasspath(fileList).get().findClassOrNull(object.getClass().getCanonicalName());

            // после того как нашли класс

            // извлекаем список полей объекта

            // затем провеяем методы с помощью jacodb, на наличие инструкций на присваивание значений - это будет сеттер. (this.? = ?)
            // соотносим методы. Если все поля удалось соотнести, то отлично.

            // далее просто проверяем поля объекта, и для его текущего значения генерируем вызов соответствующего сеттера на основе значения
            // и типа

        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(InMemoryHierarchy.INSTANCE)
        ).get();
    }
}
