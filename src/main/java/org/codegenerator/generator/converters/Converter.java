package org.codegenerator.generator.converters;

public interface Converter {
    boolean canConvert(Object o);

    String convert(Object o);
}
