package org.codegenerator;

import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcDatabase;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jacodb.impl.features.Usages;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class JacoDBSimpleTest {
    @Test
    public void simpleTest() throws ExecutionException, InterruptedException {
        try (JcDatabase db = createDataBase()) {
            File file = Paths.get("./src/main/resources/").toAbsolutePath().normalize().toFile();
            db.asyncLoad(Collections.singletonList(file));
            JcClassOrInterface clzMain = db.asyncClasspath(Collections.singletonList(file)).get().findClassOrNull("org.testdir.Dir");
            assert clzMain != null;
            System.out.println(clzMain);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JcDatabase createDataBase() throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent("./db" + System.currentTimeMillis())
                .installFeatures(Usages.INSTANCE, InMemoryHierarchy.INSTANCE)
        ).get();
    }
}
