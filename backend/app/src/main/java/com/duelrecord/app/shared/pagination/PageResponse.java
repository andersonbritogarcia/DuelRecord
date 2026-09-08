package com.duelrecord.app.shared.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record PageResponse<T>(List<T> content,
                              int page,
                              int size,
                              long totalElements,
                              int totalPages,
                              boolean first,
                              boolean last,
                              boolean empty) {
    public PageResponse {
        content = Objects.isNull(content) ? List.of() : List.copyOf(content);
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        if (Objects.isNull(page)) {
            return new PageResponse<>(List.of(), 0, 0, 0, 0, true, true, true);
        }
        return new PageResponse<>(page.getContent(),
                                  page.getNumber(),
                                  page.getSize(),
                                  page.getTotalElements(),
                                  page.getTotalPages(),
                                  page.isFirst(),
                                  page.isLast(),
                                  page.isEmpty());
    }

    public static <S, T> PageResponse<T> from(Page<S> page, Function<? super S, T> mapper) {
        if (Objects.isNull(page)) {
            return new PageResponse<>(List.of(), 0, 0, 0, 0, true, true, true);
        }
        Objects.requireNonNull(mapper, "mapper must not be null");
        List<T> mappedContent = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(mappedContent,
                                  page.getNumber(),
                                  page.getSize(),
                                  page.getTotalElements(),
                                  page.getTotalPages(),
                                  page.isFirst(),
                                  page.isLast(),
                                  mappedContent.isEmpty());
    }

    public <U> PageResponse<U> map(Function<? super T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        List<U> mappedContent = this.content.stream().map(mapper).toList();
        return new PageResponse<>(mappedContent,
                                  this.page,
                                  this.size,
                                  this.totalElements,
                                  this.totalPages,
                                  this.first,
                                  this.last,
                                  mappedContent.isEmpty());
    }
}
