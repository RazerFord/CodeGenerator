package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.codegenerator.generator.codegenerators.ContextGenerator;

public interface CodeGenerationStrategy {
    CodeGenerationStrategy generate(ContextGenerator context);
}
