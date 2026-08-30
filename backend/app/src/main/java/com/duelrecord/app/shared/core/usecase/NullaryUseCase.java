package com.duelrecord.app.shared.core.usecase;

/**
 * Use Case contract that accepts no input and produces an output.
 *
 * @param <OUT> the output type
 */
@FunctionalInterface
public interface NullaryUseCase<OUT> {

    /**
     * Executes the use case producing an output.
     *
     * @return the execution result
     */
    OUT execute();
}
