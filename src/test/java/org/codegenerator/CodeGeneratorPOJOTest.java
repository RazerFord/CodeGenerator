package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesCodeGeneratorPOJO.Point;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class CodeGeneratorPOJOTest {
    @Test
    public void simpleTest() {
        Point pojoObject = new Point();

        POJOGenerator<Point> generator = new POJOGenerator<>(pojoObject);
        OutputStream outputStream = new ByteArrayOutputStream();
        generator.generate(outputStream);

        System.out.println(0.F);
    }
}
