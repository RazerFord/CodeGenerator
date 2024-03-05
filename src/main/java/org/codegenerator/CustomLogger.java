package org.codegenerator;

import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLogger {
    private CustomLogger() {
    }

    private static final Pair<String, String> FORMAT = initFormat();
    private static final Level LEVEL = Level.ALL;
    private static final Logger MAIN_LOGGER = init();

    @Contract(value = " -> new", pure = true)
    private static @NotNull Pair<String, String> initFormat() {
        return new Pair<>("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] [%2$s] %5$s%n");
    }

    private static @NotNull Logger init() {
        if (System.getProperty(FORMAT.getKey()) == null) {
            System.setProperty(FORMAT.getKey(), FORMAT.getValue());
        }
        Logger logger = Logger.getLogger("MainLogger");
        logger.setLevel(LEVEL);
        return logger;
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static Logger getLogger() {
        return MAIN_LOGGER;
    }
}
