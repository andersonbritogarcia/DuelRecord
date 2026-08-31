package com.duelrecord.app.shared.usecase;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCaseContractsTest {

    @Test
    void shouldExecuteStandardUseCase() {
        UseCase<String, Integer> stringLengthUseCase = String::length;
        Integer result = stringLengthUseCase.execute("DuelRecord");
        assertEquals(10, result);
    }

    @Test
    void shouldExecuteUnitUseCase() {
        AtomicBoolean executed = new AtomicBoolean(false);
        UnitUseCase<String> consumerUseCase = input -> executed.set(input != null);
        consumerUseCase.execute("test");
        assertTrue(executed.get());
    }

    @Test
    void shouldExecuteNullaryUseCase() {
        NullaryUseCase<String> supplierUseCase = () -> "DuelRecord";
        String result = supplierUseCase.execute();
        assertEquals("DuelRecord", result);
    }
}
