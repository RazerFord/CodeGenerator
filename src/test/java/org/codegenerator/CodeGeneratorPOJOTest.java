package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesCodeGeneratorPOJO.Point;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class CodeGeneratorPOJOTest {
    @Test
    public void simpleTest() {
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class);

        Point pojoObject = new Point();
        pojoObject.setX(100);
        pojoObject.setY(2);
        pojoObject.setZ(3);

        generator.generate(pojoObject, Paths.get("./"));
    }
}
