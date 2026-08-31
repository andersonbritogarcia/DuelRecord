package com.duelrecord.app.shared.usecase;

/**
 * Use Case contract that accepts an input and produces no output (void).
 *
 * @param <IN> the input type
 */
@FunctionalInterface
public interface UnitUseCase<IN> {

    /**
     * Executes the use case with the given input without returning a result.
     *
     * @param input the input payload
     */
    void execute(IN input);
}
