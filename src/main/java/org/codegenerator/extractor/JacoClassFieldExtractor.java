package org.codegenerator.extractor;

import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcClasspath;
import org.jacodb.api.JcDatabase;
import org.jacodb.api.JcField;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JacoClassFieldExtractor {
    private final String dbname;

    public JacoClassFieldExtractor(String dbname) {
        this.dbname = dbname;
    }

    public Map<JcField, Object> extract(String cls, String... classPath) {
        try (JcDatabase db = loadOrCreateDataBase()) {
            List<File> fileList = Arrays.stream(classPath).map(it -> Paths.get(it).toAbsolutePath().normalize().toFile()).collect(Collectors.toList());
            db.asyncLoad(fileList).get();

            JcClassOrInterface jcClassOrInterface = db.asyncClasspath(fileList).get().findClassOrNull(cls);

            if (jcClassOrInterface == null) {
                return Collections.emptyMap();
            }

            Map<JcField, Object> fields = new HashMap<>();
            Set<Object> visited = new HashSet<>();
            dfs(jcClassOrInterface, db.asyncClasspath(fileList).get(), fields, visited);

            return fields;
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private JcDatabase loadOrCreateDataBase() throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(InMemoryHierarchy.INSTANCE)
        ).get();
    }

    private void dfs(JcClassOrInterface jcClassOrInterface, JcClasspath jcClasspath, Map<JcField, Object> res, @NotNull Set<Object> visited) {
        if (visited.contains(jcClassOrInterface) || jcClassOrInterface == null) {
            return;
        }
        visited.add(jcClassOrInterface);

        for (JcField jcField : jcClassOrInterface.getDeclaredFields()) {
            Map<JcField, Object> map = new HashMap<>();
            res.put(jcField, map);
            jcClassOrInterface = jcClasspath.findClassOrNull(jcField.getType().getTypeName());
            dfs(jcClassOrInterface, jcClasspath, map, visited);
        }
    }
}