package com.duelrecord.app.shared.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    @DisplayName("Should convert Spring Page to PageResponse")
    void shouldConvertSpringPageToPageResponse() {
        var pageRequest = PageRequest.of(1, 2);
        var page = new PageImpl<>(List.of("apple", "banana"), pageRequest, 5);

        var response = PageResponse.from(page);

        assertEquals(List.of("apple", "banana"), response.content());
        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertFalse(response.first());
        assertFalse(response.last());
        assertFalse(response.empty());
    }

    @Test
    @DisplayName("Should convert Spring Page with mapper function")
    void shouldConvertSpringPageWithMapper() {
        var pageRequest = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(1, 2, 3), pageRequest, 3);

        var response = PageResponse.from(page, num -> "item-" + num);

        assertEquals(List.of("item-1", "item-2", "item-3"), response.content());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(3, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertFalse(response.empty());
    }

    @Test
    @DisplayName("Should map content using instance map method")
    void shouldMapContentUsingInstanceMap() {
        var response = new PageResponse<>(List.of("a", "bb"), 0, 10, 2, 1, true, true, false);

        var mapped = response.map(String::length);

        assertEquals(List.of(1, 2), mapped.content());
        assertEquals(0, mapped.page());
        assertEquals(10, mapped.size());
        assertEquals(2, mapped.totalElements());
        assertEquals(1, mapped.totalPages());
    }

    @Test
    @DisplayName("Should handle null page gracefully")
    void shouldHandleNullPage() {
        var response = PageResponse.from(null);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page());
        assertEquals(0, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertTrue(response.empty());
    }
}
