package com.duelrecord.app.card.core.usecase;

import com.duelrecord.app.card.core.util.ColorIdentityUtils;
import com.duelrecord.app.shared.utils.ValidationUtils;
import org.springframework.data.domain.PageRequest;

import java.util.Objects;

public record SearchCommandersInput(
        String query,
        String colorIdentity,
        Integer limit
) {
    public SearchCommandersInput {
        limit = (Objects.isNull(limit) || limit <= 0) ? 20 : Math.min(limit, 50);
    }

    public String normalizedQuery() {
        return Objects.nonNull(query) ? query : "";
    }

    public String canonicalColor() {
        return !ValidationUtils.isBlank(colorIdentity)
                ? ColorIdentityUtils.canonicalize(colorIdentity)
                : null;
    }

    public PageRequest toPageRequest() {
        return PageRequest.of(0, limit);
    }

    public String toScryfallQuery() {
        String canonical = canonicalColor();
        return Objects.nonNull(canonical)
                ? normalizedQuery() + " id=" + canonical.toLowerCase()
                : normalizedQuery();
    }
}
