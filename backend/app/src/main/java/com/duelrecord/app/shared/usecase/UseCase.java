package com.duelrecord.app.shared.usecase;

/**
 * Standard Use Case contract taking an input and returning an output.
 *
 * @param <IN>  the input type
 * @param <OUT> the output type
 */
@FunctionalInterface
public interface UseCase<IN, OUT> {

    /**
     * Executes the use case with the given input.
     *
     * @param input the input payload
     * @return the execution result
     */
    OUT execute(IN input);
}
