package org.codegenerator;

public class Common {
    public static final String OUTPUT_DIRECTORY = "./";
    public static final String PACKAGE_NAME = "generatedclass";
    public static final String METHOD_NAME = "generate";
    public static final String CLASS_PATH_PREFIX = "./generatedclass/";
    public static final String CLASS_NAME_PREFIX = "generatedclass.";
    public static final GeneratedCodeCompiler GENERATED_CODE_COMPILER = new GeneratedCodeCompiler(OUTPUT_DIRECTORY, CLASS_PATH_PREFIX, CLASS_NAME_PREFIX, METHOD_NAME);

    public static <R> R createObject(String generatedClassName) {
        return GENERATED_CODE_COMPILER.createObject(generatedClassName);
    }
}
