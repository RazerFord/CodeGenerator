package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesCodeGeneratorPOJO.Point;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class CodeGeneratorPOJOTest {
    @Test
    public void simpleTest() {
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class);

        Point pojoObject = new Point();
        pojoObject.setX(100);
        pojoObject.setZ(6);
        pojoObject.setZ(5);
        pojoObject.setZ(4);
        pojoObject.setZ(3);

        generator.generate(pojoObject, Paths.get("./"));
    }
}
