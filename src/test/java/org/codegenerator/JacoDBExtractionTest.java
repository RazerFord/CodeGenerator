package org.codegenerator;

import org.codegenerator.extractor.JacoClassFieldExtractor;
import org.jacodb.api.JcField;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class JacoDBExtractionTest {
    @Test
    public void simpleTest() {
        String className = "org.testdir.Name";
        JacoClassFieldExtractor jacoClassFieldExtractor = new JacoClassFieldExtractor("./test_db");
        Map<JcField, Object> res = jacoClassFieldExtractor.extract(className, "./src/main/resources/");
        print(res, className);
    }

    @Test
    public void simpleRecursiveTest() {
        String className = "org.testdir.Node";
        JacoClassFieldExtractor jacoClassFieldExtractor = new JacoClassFieldExtractor("./test_db");
        Map<JcField, Object> res = jacoClassFieldExtractor.extract(className, "./src/main/resources/");
        print(res, className);
    }

    @Test
    public void simpleEmptyTest() {
        String className = "org.testdir.Empty";
        JacoClassFieldExtractor jacoClassFieldExtractor = new JacoClassFieldExtractor("./test_db");
        Map<JcField, Object> res = jacoClassFieldExtractor.extract(className, "./src/main/resources/");
        print(res, className);
    }

    @Test
    public void simpleInheritorTest() {
        String className = "org.testdir.Inheritor";
        JacoClassFieldExtractor jacoClassFieldExtractor = new JacoClassFieldExtractor("./test_db");
        Map<JcField, Object> res = jacoClassFieldExtractor.extract(className, "./src/main/resources/");
        print(res, className);
    }

    private void print(Map<JcField, Object> map, String root) {
        System.out.println(root);
        print(map, 4, root);
    }

    private void print(Map<JcField, Object> map, int indent, String root) {
        if (map == null || map.isEmpty()) {
            return;
        }
        System.out.printf("%s %s\n", String.join("", Collections.nCopies(indent, ">")), root);
        for (Map.Entry<JcField, Object> entry : map.entrySet()) {
            JcField field = entry.getKey();
            System.out.printf("%s%s: %s\n", String.join("", Collections.nCopies(indent, " ")), field.getName(), field.getType());

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<JcField, Object> var = (Map<JcField, Object>) entry.getValue();
                print(var, indent + 4, field.getType().getTypeName());
            }
        }
        System.out.printf("%s %s\n", String.join("", Collections.nCopies(indent, "<")), root);
    }
}
