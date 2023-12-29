package org.codegenerator;

import org.codegenerator.generator.POJOGenerator;
import org.codegenerator.resourcesCodeGeneratorPOJO.Point;
import org.codegenerator.resourcesCodeGeneratorPOJO.PointComplex;
import org.codegenerator.resourcesCodeGeneratorPOJO.User;
import org.codegenerator.resourcesCodeGeneratorPOJO.UserComplex;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class CodeGeneratorPOJOTest {
    @Test
    public void setterPointTest() {
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class);

        Point point = new Point();
        point.setX(100);
        point.setY(6);
        point.setZ(3);

        generator.generate(point, Paths.get("./"));
    }

    @Test
    public void defaultPointArgumentsTest() {
        POJOGenerator<Point> generator = new POJOGenerator<>(Point.class);

        Point point = new Point();
        point.setX(0);
        point.setY(0);
        point.setZ(0);

        generator.generate(point, Paths.get("./"));
    }

    @Test
    public void setterUserTest() {
        POJOGenerator<User> generator = new POJOGenerator<>(User.class);

        User user = new User();
        user.setName("Alex");
        user.setAge(12);
        user.setWeight(42);

        generator.generate(user, Paths.get("./"));
    }

    @Test
    public void defaultUserArgumentsTest() {
        POJOGenerator<User> generator = new POJOGenerator<>(User.class);

        User user = new User();

        generator.generate(user, Paths.get("./"));
    }

    @Test
    public void setterPointComplexTest() {
        POJOGenerator<PointComplex> generator = new POJOGenerator<>(PointComplex.class);

        PointComplex point = new PointComplex();
        point.setX(100);
        point.setY(6);
        point.setZ(3);

        generator.generate(point, Paths.get("./"));
    }

    @Test
    public void setterUserComplexTest() {
        POJOGenerator<UserComplex> generator = new POJOGenerator<>(UserComplex.class);

        UserComplex user = new UserComplex();
        user.setNameAgeWeight("Alex", 12, 42);

        generator.generate(user, Paths.get("./"));
    }
}
